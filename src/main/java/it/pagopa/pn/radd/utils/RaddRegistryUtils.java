package it.pagopa.pn.radd.utils;

import it.pagopa.pn.api.dto.events.PnAttachmentsConfigEventItem;
import it.pagopa.pn.api.dto.events.PnAttachmentsConfigEventPayload;
import it.pagopa.pn.api.dto.events.PnEvaluatedZipCodeEvent;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.AnalogAddressDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.NormalizeItemsRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.NormalizeRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.entities.NormalizedAddressEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.queue.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.pojo.*;
import it.pagopa.pn.radd.services.radd.fsu.v1.SecretService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static it.pagopa.pn.radd.pojo.RaddRegistryImportStatus.TO_PROCESS;

@Component
@RequiredArgsConstructor
@CustomLog
public class RaddRegistryUtils {

    private final ObjectMapperUtil objectMapperUtil;
    private final PnRaddFsuConfig pnRaddFsuConfig;
    private final SecretService secretService;



    public Mono<RaddRegistryEntity> mergeNewRegistryEntity(RaddRegistryEntity preExistingRegistryEntity, RaddRegistryRequestEntity newRegistryRequestEntity) {
        return Mono.fromCallable(() -> {
            RaddRegistryOriginalRequest raddRegistryOriginalRequest = objectMapperUtil.toObject(newRegistryRequestEntity.getOriginalRequest(), RaddRegistryOriginalRequest.class);

            return getRaddRegistryEntity(preExistingRegistryEntity, newRegistryRequestEntity, raddRegistryOriginalRequest);
        });
    }

    private static RaddRegistryEntity getRaddRegistryEntity(RaddRegistryEntity preExistingRegistryEntity, RaddRegistryRequestEntity newRegistryRequestEntity, RaddRegistryOriginalRequest raddRegistryOriginalRequest) {
        RaddRegistryEntity registryEntity = new RaddRegistryEntity();

        registryEntity.setRegistryId(preExistingRegistryEntity.getRegistryId());
        registryEntity.setCxId(preExistingRegistryEntity.getCxId());
        registryEntity.setNormalizedAddress(preExistingRegistryEntity.getNormalizedAddress());
        registryEntity.setRequestId(newRegistryRequestEntity.getRequestId());
        // Metadata from originalRequest
        registryEntity.setDescription(raddRegistryOriginalRequest.getDescription());
        registryEntity.setPhoneNumber(raddRegistryOriginalRequest.getPhoneNumber());
        registryEntity.setGeoLocation(raddRegistryOriginalRequest.getGeoLocation());
        registryEntity.setZipCode(newRegistryRequestEntity.getZipCode());
        registryEntity.setOpeningTime(raddRegistryOriginalRequest.getOpeningTime());
        registryEntity.setCapacity(raddRegistryOriginalRequest.getCapacity());
        if(StringUtils.isNotBlank(raddRegistryOriginalRequest.getStartValidity())) {
            LocalDate localDate = LocalDate.parse(raddRegistryOriginalRequest.getStartValidity());
            registryEntity.setStartValidity(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
        }
        if(StringUtils.isNotBlank(raddRegistryOriginalRequest.getEndValidity())) {
            LocalDate localDate = LocalDate.parse(raddRegistryOriginalRequest.getEndValidity());
            registryEntity.setEndValidity(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
        }

        return registryEntity;
    }

    public Mono<RaddRegistryEntity> constructRaddRegistryEntity(String registryId, PnAddressManagerEvent.NormalizedAddress normalizedAddress, RaddRegistryRequestEntity registryRequest) {
        String normalizedAddressString = objectMapperUtil.toJson(normalizedAddress);
        RaddRegistryOriginalRequest raddRegistryOriginalRequest = objectMapperUtil.toObject(registryRequest.getOriginalRequest(), RaddRegistryOriginalRequest.class);

        return Mono.just(getRaddRegistryEntity(registryId, normalizedAddress, registryRequest, normalizedAddressString, raddRegistryOriginalRequest));
    }

    private static RaddRegistryEntity getRaddRegistryEntity(String registryId, PnAddressManagerEvent.NormalizedAddress normalizedAddress, RaddRegistryRequestEntity registryRequest, String normalizedAddressString, RaddRegistryOriginalRequest raddRegistryOriginalRequest) {
        RaddRegistryEntity registryEntity = new RaddRegistryEntity();

        registryEntity.setRegistryId(registryId);
        registryEntity.setCxId(registryRequest.getCxId());
        registryEntity.setNormalizedAddress(mapNormalizedAddressEntityToNormalizedAddress(normalizedAddress));
        registryEntity.setRequestId(registryRequest.getRequestId());
        // Metadata from originalRequest
        registryEntity.setDescription(raddRegistryOriginalRequest.getDescription());
        registryEntity.setPhoneNumber(raddRegistryOriginalRequest.getPhoneNumber());
        registryEntity.setGeoLocation(raddRegistryOriginalRequest.getGeoLocation());
        registryEntity.setZipCode(normalizedAddress.getCap());
        registryEntity.setOpeningTime(raddRegistryOriginalRequest.getOpeningTime());
        registryEntity.setCapacity(raddRegistryOriginalRequest.getCapacity());
        if(StringUtils.isNotBlank(raddRegistryOriginalRequest.getStartValidity())) {
            LocalDate localDate = LocalDate.parse(raddRegistryOriginalRequest.getStartValidity());
            registryEntity.setStartValidity(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
        }
        if(StringUtils.isNotBlank(raddRegistryOriginalRequest.getEndValidity())) {
            LocalDate localDate = LocalDate.parse(raddRegistryOriginalRequest.getEndValidity());
            registryEntity.setEndValidity(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
        }

        return registryEntity;
    }

    public Mono<PnAddressManagerEvent.ResultItem> getRelativeItemFromAddressManagerEvent(List<PnAddressManagerEvent.ResultItem> resultItems, String id) {
        Optional<PnAddressManagerEvent.ResultItem> resultItemOptional = resultItems.stream()
                .filter(item -> StringUtils.equals(item.getId(), RaddRegistryRequestEntity.retrieveIndexFromPk(id))).findFirst();

        if (resultItemOptional.isEmpty()) {
            log.warn("Item with id {} not found or not in event list", id);
            return Mono.empty();
        }
        return Mono.just(resultItemOptional.get());
    }

    public RaddRegistryImportEntity getPnRaddRegistryImportEntity(String xPagopaPnCxId, RegistryUploadRequest request, FileCreationResponseDto fileCreationResponseDto, String requestId) {
        RaddRegistryImportEntity pnRaddRegistryImportEntity = new RaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setRequestId(requestId);
        pnRaddRegistryImportEntity.setStatus(TO_PROCESS.name());
        pnRaddRegistryImportEntity.setChecksum(request.getChecksum());
        pnRaddRegistryImportEntity.setCxId(xPagopaPnCxId);
        pnRaddRegistryImportEntity.setFileKey(fileCreationResponseDto.getKey());
        pnRaddRegistryImportEntity.setCreatedAt(Instant.now());
        pnRaddRegistryImportEntity.setUpdatedAt(Instant.now());
        pnRaddRegistryImportEntity.setFileUploadDueDate(Instant.now().plus(pnRaddFsuConfig.getRegistryImportUploadFileTtl(), ChronoUnit.SECONDS));

        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDeleteRole(pnRaddFsuConfig.getRegistryDefaultDeleteRule());
        raddRegistryImportConfig.setDefaultEndValidity(pnRaddFsuConfig.getRegistryDefaultEndValidity());
        pnRaddRegistryImportEntity.setConfig(objectMapperUtil.toJson(raddRegistryImportConfig));

        return pnRaddRegistryImportEntity;
    }

    public FileCreationRequestDto getFileCreationRequestDto() {
        FileCreationRequestDto request = new FileCreationRequestDto();
        request.setStatus(Const.SAVED);
        request.setContentType(Const.CONTENT_TYPE_TEXT_CSV);
        request.setDocumentType(this.pnRaddFsuConfig.getRegistrySafeStorageDocType());
        return request;
    }

    public List<AddressManagerRequestAddress> getRequestAddressFromOriginalRequest(List<RaddRegistryRequestEntity> entities) {
        return entities.stream().map(entity -> {
            AddressManagerRequestAddress request = objectMapperUtil.toObject(entity.getOriginalRequest(), AddressManagerRequestAddress.class);
            request.setId(RaddRegistryRequestEntity.retrieveIndexFromPk(entity.getPk()));
            return request;
        }).toList();
    }

    public NormalizeItemsRequestDto getNormalizeRequestDtoFromAddressManagerRequest(AddressManagerRequest request) {
        NormalizeItemsRequestDto requestDto = new NormalizeItemsRequestDto();
        requestDto.setCorrelationId(request.getCorrelationId());
        List<NormalizeRequestDto> listDto = request.getAddresses().stream().map(address -> {
            NormalizeRequestDto dto = new NormalizeRequestDto();
            dto.setId(address.getId());
            AnalogAddressDto addressDto = new AnalogAddressDto();
            addressDto.setAddressRow(address.getAddressRow());
            addressDto.setCap(address.getCap());
            addressDto.setCity(address.getCity());
            addressDto.setPr(address.getPr());
            addressDto.setCountry(address.getCountry());
            dto.setAddress(addressDto);
            return dto;
        }).toList();
        requestDto.setRequestItems(listDto);

        return requestDto;
    }

    public String retrieveAddressManagerApiKey() {
        return secretService.getSecret(pnRaddFsuConfig.getAddressManagerApiKeySecret());
    }
    public PnEvaluatedZipCodeEvent mapToEventMessage(Set<TimeInterval> timeIntervals, String zipCode) {
        return PnEvaluatedZipCodeEvent.builder().detail(
                PnAttachmentsConfigEventPayload
                        .builder()
                        .configKey(zipCode)
                        .configType(pnRaddFsuConfig.getEvaluatedZipCodeConfigType())
                        .configs(getConfigEntries(timeIntervals))
                        .build()
        ).build();
    }

    private List<PnAttachmentsConfigEventItem> getConfigEntries(Set<TimeInterval> timeIntervals) {
        return timeIntervals.stream()
                .map(timeInterval -> PnAttachmentsConfigEventItem.builder()
                        .startValidity(timeInterval.getStart())
                        .endValidity(timeInterval.getEnd() == Instant.MAX ? null : timeInterval.getEnd())
                        .build()).toList();
    }

    public List<TimeInterval> getOfficeIntervals(List<RaddRegistryEntity> raddRegistryEntities) {
        return raddRegistryEntities.stream()
                .map(raddRegistryEntity -> {
                    if (raddRegistryEntity.getEndValidity() == null) {
                        return new TimeInterval(raddRegistryEntity.getStartValidity(), Instant.MAX);
                    } else {
                        return new TimeInterval(raddRegistryEntity.getStartValidity(), raddRegistryEntity.getEndValidity());
                    }
                }).toList();
    }

    public Set<TimeInterval> findActiveIntervals(List<TimeInterval> timeIntervals) {

        TimeInterval[] timeIntervalArray = timeIntervals.toArray(new TimeInterval[0]);
        Set<Set<TimeInterval>> result = new HashSet<>();

        combinations(timeIntervalArray, new ArrayList<>(), result, pnRaddFsuConfig.getEvaluatedZipCodeConfigNumber(), 0);

        Set<TimeInterval> activeIntervals = new HashSet<>();

        for (Set<TimeInterval> intervalSet : result) {
            TimeInterval timeInterval = findIntersection(intervalSet.stream().toList());
            if (timeInterval != null) {
                activeIntervals.add(timeInterval);
            }
        }

        TimeInterval[] activeIntervalsArray = new TimeInterval[0];
        return mergeIntervals(activeIntervals.toArray(activeIntervalsArray));
    }

    public static void combinations(TimeInterval[] values, List<TimeInterval> current, Set<Set<TimeInterval>> accumulator, int size, int pos) {
        if (current.size() == size) {
            Set<TimeInterval> toAdd = new HashSet<>(current);
            if (accumulator.contains(toAdd)) {
                throw new RuntimeException("Duplicated value " + current);
            }
            accumulator.add(toAdd);
            return;
        }
        for (int i = pos; i <= values.length - size + current.size(); i++) {
            current.add(values[i]);
            combinations(values, current, accumulator, size, i + 1);
            current.remove(current.size() - 1);
        }
    }

    static TimeInterval findIntersection(List<TimeInterval> intervals) {
        Instant start = intervals.get(0).getStart();
        Instant end = intervals.get(0).getEnd();

        for (int i = 1; i < intervals.size(); i++) {
            if (intervals.get(i).getStart().isAfter(end) || intervals.get(i).getEnd().isBefore(start)) {
                return null;
            } else {
                if (start.isBefore(intervals.get(i).getStart()))
                    start = intervals.get(i).getStart();
                if (end.isAfter(intervals.get(i).getEnd()))
                    end = intervals.get(i).getEnd();
            }
        }
        return new TimeInterval(start, end);
    }

    public static Set<TimeInterval> mergeIntervals(TimeInterval[] timeIntervals)
    {
        if (timeIntervals.length <= 0) {
            return Set.of();
        }
        Arrays.sort(timeIntervals, Comparator.comparing(TimeInterval::getStart));

        Stack<TimeInterval> stack = new Stack<>();
        stack.push(timeIntervals[0]);

        for (int i = 1; i < timeIntervals.length; i++) {
            TimeInterval top = stack.peek();

            if (top.getEnd().isBefore(timeIntervals[i].getStart()))
                stack.push(timeIntervals[i]);
            else if (top.getEnd().isBefore(timeIntervals[i].getEnd())) {
                top.setEnd(timeIntervals[i].getEnd());
                stack.pop();
                stack.push(top);
            }
        }

        TimeInterval[] activeIntervals = new TimeInterval[0];
        return actualizePastIntervals(Set.of(stack.toArray(activeIntervals)));
    }

    private static Set<TimeInterval> actualizePastIntervals(Set<TimeInterval> timeIntervals) {
        /* arrivati a questo punto dovremmo avere solo intervalli attivi che vanno da prima di oggi fino ad un tempo futuro indefinito.
         Se l'intervallo ha un inizio precedente ad oggi, lo aggiorniamo con la data odierna. */
        Instant now = getStartOfTodayInstant();
        for (TimeInterval timeInterval : timeIntervals) {
            if (timeInterval.getStart().isBefore(now)) {
                timeInterval.setStart(now);
            }
        }

        return timeIntervals;
    }

    private static Instant getStartOfTodayInstant() {
        return LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public RequestResponse mapToRequestResponse(ResultPaginationDto<RaddRegistryRequestEntity, String> resultPaginationDto) {
        RequestResponse result = new RequestResponse();
        if(resultPaginationDto.getResultsPage() != null) {
            result.setItems(resultPaginationDto.getResultsPage().stream()
                    .map(raddRegistryRequestEntity -> {
                        RegistryRequestResponse registryRequestResponse = new RegistryRequestResponse();
                        registryRequestResponse.setRegistryId(raddRegistryRequestEntity.getRegistryId());
                        registryRequestResponse.setRequestId(raddRegistryRequestEntity.getRequestId());
                        registryRequestResponse.setError(raddRegistryRequestEntity.getError());
                        registryRequestResponse.setCreatedAt(raddRegistryRequestEntity.getCreatedAt().toString());
                        registryRequestResponse.setUpdatedAt(raddRegistryRequestEntity.getUpdatedAt().toString());
                        registryRequestResponse.setStatus(raddRegistryRequestEntity.getStatus());
                        OriginalRequest originalRequest = objectMapperUtil.toObject(raddRegistryRequestEntity.getOriginalRequest(), OriginalRequest.class);
                        registryRequestResponse.setOriginalRequest(originalRequest);

                        return registryRequestResponse;
                    })
                    .toList());
        }
        result.setNextPagesKey(resultPaginationDto.getNextPagesKey());
        result.setMoreResult(resultPaginationDto.isMoreResult());
        return result;
    }

    public RegistriesResponse prepareRaddRegistrySelfResult(List<RaddRegistryEntity> queryResult,
                                                            boolean moreResults,
                                                            int limit) {
        RegistriesResponse result = new RegistriesResponse();
        result.setNextPagesKey(new ArrayList<>());

        if(queryResult != null) {
            result.setRegistries(queryResult.stream()
                    .limit(limit)
                    .map(raddRegistryEntity -> {
                        RegistryRequestResponse registryRequestResponse = new RegistryRequestResponse();
                        return mapRegistryEntityToRegistry(raddRegistryEntity);
                    })
                    .toList());
        }
        result.setMoreResult(moreResults);

        for (int i = 1; i <= pnRaddFsuConfig.getMaxPageNumber(); i++){
            int index = limit * i;
            if (queryResult.size() <= index) {
                break;
            }
            RaddRegistryEntity keyEntity = queryResult.get(index - 1);
            PnLastEvaluatedKey pageLastEvaluatedKey = computeLastEvaluatedKey(keyEntity);
            result.getNextPagesKey().add(pageLastEvaluatedKey.getExternalLastEvaluatedKey());
        }

        return result;
    }

    private Registry mapRegistryEntityToRegistry(RaddRegistryEntity entity) {
        Registry registry = new Registry();
        registry.setRegistryId(entity.getRegistryId());
        registry.setRequestId(entity.getRequestId());
        registry.setAddress(mapNormalizedAddressToAddress(entity.getNormalizedAddress()));
        registry.setDescription(entity.getDescription());
        registry.setPhoneNumber(entity.getPhoneNumber());
        String[] geoLocationArray = entity.getGeoLocation().split(",");
        RegistryGeoLocation geoLocation = new RegistryGeoLocation();
        geoLocation.setLatitude(geoLocationArray[0]);
        geoLocation.setLongitude(geoLocationArray[1]);
        registry.setGeoLocation(geoLocation);
        registry.setOpeningTime(entity.getOpeningTime());
        registry.setStartValidity(Date.from(entity.getStartValidity()));
        registry.setEndValidity(Date.from(entity.getEndValidity()));
        return registry;
    }

    private PnLastEvaluatedKey computeLastEvaluatedKey(RaddRegistryEntity keyEntity) {
        PnLastEvaluatedKey pageLastEvaluatedKey = new PnLastEvaluatedKey();
        pageLastEvaluatedKey.setExternalLastEvaluatedKey(keyEntity.getRegistryId());
        pageLastEvaluatedKey.setInternalLastEvaluatedKey(Map.of(
                RaddRegistryRequestEntity.COL_CX_ID, AttributeValue.builder().s(keyEntity.getCxId()).build()
        ));
        return pageLastEvaluatedKey;
    }

    private Address mapNormalizedAddressToAddress(NormalizedAddressEntity normalizedAddress) {
        Address address = new Address();
        address.addressRow(normalizedAddress.getAddressRow());
        address.cap(normalizedAddress.getCap());
        address.pr(normalizedAddress.getPr());
        address.city(normalizedAddress.getCity());
        address.country(normalizedAddress.getCountry());
        return address;
    }

    private static NormalizedAddressEntity mapNormalizedAddressEntityToNormalizedAddress(PnAddressManagerEvent.NormalizedAddress normalizedAddress) {
        NormalizedAddressEntity address = new NormalizedAddressEntity();
        address.setAddressRow(normalizedAddress.getAddressRow());
        address.setCap(normalizedAddress.getCap());
        address.setPr(normalizedAddress.getPr());
        address.setCity(normalizedAddress.getCity());
        address.setCountry(normalizedAddress.getCountry());
        return address;
    }

}
