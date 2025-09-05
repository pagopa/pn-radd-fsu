package it.pagopa.pn.radd.utils;

import it.pagopa.pn.api.dto.events.PnAttachmentsConfigEventItem;
import it.pagopa.pn.api.dto.events.PnAttachmentsConfigEventPayload;
import it.pagopa.pn.api.dto.events.PnEvaluatedZipCodeEvent;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.AnalogAddressDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.NormalizeItemsRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.NormalizeRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.AddressV2;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.CreateRegistryRequestV2;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.UpdateRegistryRequestV2;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.entities.*;
import it.pagopa.pn.radd.middleware.queue.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.pojo.*;
import it.pagopa.pn.radd.services.radd.fsu.v1.AwsGeoService;
import it.pagopa.pn.radd.services.radd.fsu.v1.SecretService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.geoplaces.model.AddressComponentMatchScores;
import software.amazon.awssdk.services.geoplaces.model.ComponentMatchScores;
import software.amazon.awssdk.services.geoplaces.model.MatchScoreDetails;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import static it.pagopa.pn.radd.pojo.RaddRegistryImportStatus.TO_PROCESS;
import static it.pagopa.pn.radd.utils.DateUtils.*;

@Component
@RequiredArgsConstructor
@CustomLog
public class RaddRegistryUtils {

    private final ObjectMapperUtil objectMapperUtil;
    private final PnRaddFsuConfig pnRaddFsuConfig;
    private final SecretService secretService;
    private final static String PARTNER_ID_REGEX = "^([0-9]{11})$";

    private final static Function<Map<String, AttributeValue>, PnLastEvaluatedKey> STORE_REGISTRY_LAST_EVALUATED_KEY = (stringAttributeValueMap) -> {
        PnLastEvaluatedKey pageLastEvaluatedKey = new PnLastEvaluatedKey();
        pageLastEvaluatedKey.setExternalLastEvaluatedKey(stringAttributeValueMap.get(RaddRegistryEntity.COL_CXID).s());
        pageLastEvaluatedKey.setInternalLastEvaluatedKey(Map.of(
                RaddRegistryEntity.COL_REGISTRY_ID, AttributeValue.builder().s(stringAttributeValueMap.get(RaddRegistryEntity.COL_REGISTRY_ID).s()).build(),
                RaddRegistryEntity.COL_CXID, AttributeValue.builder().s(stringAttributeValueMap.get(RaddRegistryEntity.COL_CXID).s()).build()
        ));
        return pageLastEvaluatedKey;
    };


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
        registryEntity.setZipCode(preExistingRegistryEntity.getZipCode());
        registryEntity.setOpeningTime(raddRegistryOriginalRequest.getOpeningTime());
        registryEntity.setCapacity(raddRegistryOriginalRequest.getCapacity());
        registryEntity.setExternalCode(raddRegistryOriginalRequest.getExternalCode());
        if(StringUtils.isNotBlank(raddRegistryOriginalRequest.getStartValidity())) {
            registryEntity.setStartValidity(Instant.parse(raddRegistryOriginalRequest.getStartValidity()));
        }
        if(StringUtils.isNotBlank(raddRegistryOriginalRequest.getEndValidity())) {
            registryEntity.setEndValidity(Instant.parse(raddRegistryOriginalRequest.getEndValidity()));
        }

        return registryEntity;
    }

    public Mono<RaddRegistryEntity> constructRaddRegistryEntity(String registryId, PnAddressManagerEvent.NormalizedAddress normalizedAddress, RaddRegistryRequestEntity registryRequest) {
        RaddRegistryOriginalRequest raddRegistryOriginalRequest = objectMapperUtil.toObject(registryRequest.getOriginalRequest(), RaddRegistryOriginalRequest.class);

        return Mono.just(getRaddRegistryEntity(registryId, normalizedAddress, registryRequest, raddRegistryOriginalRequest));
    }

    public static RaddRegistryEntityV2 buildRaddRegistryEntity(String partnerId, String locationId, String uid, CreateRegistryRequestV2 request, AwsGeoService.CoordinatesResult coordinatesResult) {
        RaddRegistryEntityV2 raddRegistryEntityV2 = new RaddRegistryEntityV2();

        raddRegistryEntityV2.setPartnerId(partnerId);
        raddRegistryEntityV2.setLocationId(locationId);
        raddRegistryEntityV2.setDescription(request.getDescription());
        raddRegistryEntityV2.setPhoneNumbers(request.getPhoneNumbers());
        raddRegistryEntityV2.setOpeningTime(request.getOpeningTime());
        raddRegistryEntityV2.setExternalCodes(request.getExternalCodes());
        raddRegistryEntityV2.setStartValidity(request.getStartValidity() != null ? convertDateToInstantAtStartOfDay(request.getStartValidity()) : getStartOfDayToday());
        raddRegistryEntityV2.setEndValidity(request.getEndValidity() != null ? convertDateToInstantAtStartOfDay(request.getEndValidity()) : null);
        raddRegistryEntityV2.setEmail(request.getEmail());
        raddRegistryEntityV2.setAppointmentRequired(request.getAppointmentRequired());
        raddRegistryEntityV2.setWebsite(request.getWebsite());
        raddRegistryEntityV2.setPartnerType(request.getPartnerType());
        raddRegistryEntityV2.setCreationTimestamp(Instant.now());
        raddRegistryEntityV2.setUpdateTimestamp(Instant.now());
        raddRegistryEntityV2.setUid(uid);

        NormalizedAddressEntity normalizedAddress = buildNormalizedAddressEntity(coordinatesResult);

        AddressV2 inputAddress = request.getAddress();
        AddressEntity address = new AddressEntity();
        address.setAddressRow(inputAddress.getAddressRow());
        address.setCity(inputAddress.getCity());
        address.setCap(inputAddress.getCap());
        address.setProvince(inputAddress.getProvince());
        address.setCountry(inputAddress.getCountry());

        raddRegistryEntityV2.setNormalizedAddress(normalizedAddress);
        raddRegistryEntityV2.setAddress(address);
        raddRegistryEntityV2.setModifiedAddress(!areAddressesEquivalent(address, normalizedAddress));

        return raddRegistryEntityV2;
    }

    private static NormalizedAddressEntity buildNormalizedAddressEntity(AwsGeoService.CoordinatesResult coordinatesResult) {
        NormalizedAddressEntity normalizedAddress = new NormalizedAddressEntity();
        normalizedAddress.setAddressRow(coordinatesResult.getAwsAddressRow());
        normalizedAddress.setCity(coordinatesResult.getAwsLocality());
        normalizedAddress.setCap(coordinatesResult.getAwsPostalCode());
        normalizedAddress.setProvince(coordinatesResult.getAwsSubRegion());
        normalizedAddress.setCountry(coordinatesResult.getAwsCountry());
        normalizedAddress.setLongitude(coordinatesResult.getAwsLongitude());
        normalizedAddress.setLatitude(coordinatesResult.getAwsLatitude());
        normalizedAddress.setBiasPoint(buildBiasPointEntity(coordinatesResult.getAwsMatchScore()));
        return normalizedAddress;
    }

    private static BiasPointEntity buildBiasPointEntity(MatchScoreDetails matchScoreDetails) {
        BiasPointEntity biasPoint = new BiasPointEntity();

        if (matchScoreDetails == null) {
            return biasPoint;
        }

        setBigDecimalIfNotNull(biasPoint::setOverall, matchScoreDetails.overall());

        AddressComponentMatchScores addressComponents =
                Optional.ofNullable(matchScoreDetails.components())
                        .map(ComponentMatchScores::address)
                        .orElse(null);

        if (addressComponents != null) {
            setBigDecimalIfNotNull(biasPoint::setCountry, addressComponents.country());
            setBigDecimalIfNotNull(biasPoint::setAddressNumber, addressComponents.addressNumber());
            setBigDecimalIfNotNull(biasPoint::setLocality, addressComponents.locality());
            setBigDecimalIfNotNull(biasPoint::setPostalCode, addressComponents.postalCode());
            setBigDecimalIfNotNull(biasPoint::setSubRegion, addressComponents.subRegion());
        }

        return biasPoint;
    }

    private static void setBigDecimalIfNotNull(Consumer<BigDecimal> setter, Double value) {
        if (value != null) {
            setter.accept(BigDecimal.valueOf(value));
        }
    }

    private static boolean areAddressesEquivalent(AddressEntity address, NormalizedAddressEntity normalizedAddress) {
        return isAddressRowEquivalent(address.getAddressRow(), normalizedAddress.getAddressRow()) &&
                StringUtils.equals(address.getCap(), normalizedAddress.getCap()) &&
                StringUtils.equals(address.getCity(), normalizedAddress.getCity()) &&
                StringUtils.equals(address.getProvince(), normalizedAddress.getProvince()) &&
                StringUtils.equals(address.getCountry(), normalizedAddress.getCountry());
    }

    private static boolean isAddressRowEquivalent(String addressRow, String normalizedAddressRow) {
        String[] parts = normalizedAddressRow.split(",");
        String addressAndNumber = normalizedAddressRow.trim();
        if (parts.length >= 2) {
            addressAndNumber = parts[0].trim() + " " + parts[1].trim();
        }
        return StringUtils.equals(addressRow, addressAndNumber);
    }

    public static RaddRegistryEntityV2 mapFieldToUpdate(RaddRegistryEntityV2 registryEntity, UpdateRegistryRequestV2 request, String uid) {
        if (StringUtils.isNotBlank(request.getDescription())) {
            registryEntity.setDescription(request.getDescription());
        }
        if (StringUtils.isNotBlank(request.getEmail())) {
            registryEntity.setEmail(request.getEmail());
        }

        if (StringUtils.isNotBlank(request.getOpeningTime())) {
            registryEntity.setOpeningTime(request.getOpeningTime());
        }
        if (!CollectionUtils.isEmpty(request.getExternalCodes())) {
            registryEntity.setExternalCodes(request.getExternalCodes());
        }
        if (!CollectionUtils.isEmpty(request.getPhoneNumbers())) {
            registryEntity.setPhoneNumbers(request.getPhoneNumbers());
        }

        if (StringUtils.isNotBlank(request.getWebsite())) {
            registryEntity.setWebsite(request.getWebsite());
        }
        if (StringUtils.isNotBlank(request.getEmail())) {
            registryEntity.setEmail(request.getEmail());
        }

        if (request.getAppointmentRequired() != null) {
            registryEntity.setAppointmentRequired(request.getAppointmentRequired());
        }

        if (request.getEndValidity() != null) {
            registryEntity.setEndValidity(validateEndDate(registryEntity.getStartValidity(), request.getEndValidity()));
        }
        registryEntity.setUpdateTimestamp(Instant.now());
        registryEntity.setUid(uid);

        return registryEntity;
    }

    private static RaddRegistryEntity getRaddRegistryEntity(String registryId, PnAddressManagerEvent.NormalizedAddress normalizedAddress, RaddRegistryRequestEntity registryRequest, RaddRegistryOriginalRequest raddRegistryOriginalRequest) {
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
        registryEntity.setExternalCode(raddRegistryOriginalRequest.getExternalCode());
        if(StringUtils.isNotBlank(raddRegistryOriginalRequest.getStartValidity())) {
            registryEntity.setStartValidity(Instant.parse(raddRegistryOriginalRequest.getStartValidity()));
        }
        if(StringUtils.isNotBlank(raddRegistryOriginalRequest.getEndValidity())) {
            registryEntity.setEndValidity(Instant.parse(raddRegistryOriginalRequest.getEndValidity()));
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
                log.info("No intersection found for intervals: {} and {}", intervals.get(0), intervals.get(i));
                return null;
            } else {
                if (start.isBefore(intervals.get(i).getStart()))
                    start = intervals.get(i).getStart();
                if (end.isAfter(intervals.get(i).getEnd()))
                    end = intervals.get(i).getEnd();
            }
        }
        TimeInterval intersection = new TimeInterval(start, end);
        log.info("Intersection found: {}", intersection);
        return intersection;
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
                        RaddRegistryOriginalRequest originalRequest = objectMapperUtil.toObject(raddRegistryRequestEntity.getOriginalRequest(), RaddRegistryOriginalRequest.class);
                        registryRequestResponse.setOriginalRequest(convertToOriginalRequest(originalRequest));

                        return registryRequestResponse;
                    })
                    .toList());
        }
        result.setNextPagesKey(resultPaginationDto.getNextPagesKey());
        result.setMoreResult(resultPaginationDto.isMoreResult());
        return result;
    }

    private OriginalRequest convertToOriginalRequest(RaddRegistryOriginalRequest raddRegistryOriginalRequest) {
        OriginalRequest originalRequest = new OriginalRequest();

        if(raddRegistryOriginalRequest == null) {
            return originalRequest;
        }

        originalRequest.setOriginalAddress(convertToAddress(raddRegistryOriginalRequest));
        originalRequest.setDescription(raddRegistryOriginalRequest.getDescription());
        originalRequest.setPhoneNumber(raddRegistryOriginalRequest.getPhoneNumber());
        try {
            GeoLocation geoLocation = new GeoLocation();
            if (StringUtils.isNotBlank(raddRegistryOriginalRequest.getGeoLocation())) {
                geoLocation=objectMapperUtil.toObject(raddRegistryOriginalRequest.getGeoLocation(), GeoLocation.class);
            }
            originalRequest.setGeoLocation(geoLocation);
        }
        catch (PnInternalException e) {
            log.debug("There are no valid geolocation data for this registry request.");
        }
        originalRequest.setOpeningTime(raddRegistryOriginalRequest.getOpeningTime());
        if (StringUtils.isNotBlank(raddRegistryOriginalRequest.getStartValidity())) {
            Instant instant = Instant.parse(raddRegistryOriginalRequest.getStartValidity());
            originalRequest.setStartValidity(Date.from(instant));
        }
        if (StringUtils.isNotBlank(raddRegistryOriginalRequest.getEndValidity())) {
            Instant instant = Instant.parse(raddRegistryOriginalRequest.getEndValidity());
            originalRequest.setEndValidity(Date.from(instant));
        }
        originalRequest.setCapacity(raddRegistryOriginalRequest.getCapacity());
        originalRequest.setExternalCode(raddRegistryOriginalRequest.getExternalCode());
        return originalRequest;
    }

    private Address convertToAddress(RaddRegistryOriginalRequest raddRegistryOriginalRequest) {
        Address address = new Address();
        address.setAddressRow(raddRegistryOriginalRequest.getAddressRow());
        address.setCap(raddRegistryOriginalRequest.getCap());
        address.setCity(raddRegistryOriginalRequest.getCity());
        address.setPr(raddRegistryOriginalRequest.getPr());
        address.setCountry(raddRegistryOriginalRequest.getCountry());
        return address;
    }

    public RegistriesResponse mapRegistryEntityToRegistry(ResultPaginationDto<RaddRegistryEntity, String> resultPaginationDto) {
        RegistriesResponse result = new RegistriesResponse();
        if(resultPaginationDto.getResultsPage() != null) {
            result.setRegistries(resultPaginationDto.getResultsPage().stream()
                    .map(entity -> {
                        Registry registry = new Registry();
                        registry.setRegistryId(entity.getRegistryId());
                        registry.setRequestId(entity.getRequestId());
                        registry.setAddress(mapNormalizedAddressToAddress(entity.getNormalizedAddress()));
                        registry.setDescription(entity.getDescription());
                        registry.setPhoneNumber(entity.getPhoneNumber());
                        try {
                            if(StringUtils.isNotBlank(entity.getGeoLocation())) {
                                GeoLocation geoLocation = objectMapperUtil.toObject(entity.getGeoLocation(), GeoLocation.class);
                                geoLocation.setLatitude(geoLocation.getLatitude());
                                geoLocation.setLongitude(geoLocation.getLongitude());
                                registry.setGeoLocation(geoLocation);
                            }
                        } catch (PnInternalException e) {
                            log.debug("Registry with cxId = {} and registryId = {} has not valid geoLocation", entity.getCxId(), entity.getRegistryId(), e);
                        }
                        registry.setOpeningTime(entity.getOpeningTime());
                        if(entity.getStartValidity() != null) {
                            registry.setStartValidity(Date.from(entity.getStartValidity()));
                        }
                        if (entity.getEndValidity() != null) {
                            registry.setEndValidity(Date.from(entity.getEndValidity()));
                        }
                        registry.setExternalCode(entity.getExternalCode());
                        registry.setCapacity(entity.getCapacity());
                        return registry;
                    })
                    .toList());
        }
        result.setNextPagesKey(resultPaginationDto.getNextPagesKey());
        result.setMoreResult(resultPaginationDto.isMoreResult());
        return result;
    }
    private Address mapNormalizedAddressToAddress(NormalizedAddressEntity normalizedAddress) {
        Address address = new Address();
        if (Objects.nonNull(normalizedAddress)) {
            address.addressRow(normalizedAddress.getAddressRow());
            address.cap(normalizedAddress.getCap());
            address.pr(normalizedAddress.getProvince());
            address.city(normalizedAddress.getCity());
            address.country(normalizedAddress.getCountry());
        }
        return address;
    }

    private static NormalizedAddressEntity mapNormalizedAddressEntityToNormalizedAddress(PnAddressManagerEvent.NormalizedAddress normalizedAddress) {
        NormalizedAddressEntity address = new NormalizedAddressEntity();
        address.setAddressRow(normalizedAddress.getAddressRow());
        address.setCap(normalizedAddress.getCap());
        address.setProvince(normalizedAddress.getPr());
        address.setCity(normalizedAddress.getCity());
        address.setCountry(normalizedAddress.getCountry());
        return address;
    }

    public static void validatePartnerId(String partnerId) {
        if (!Pattern.matches(PARTNER_ID_REGEX, partnerId)) {
            throw new RaddGenericException(ExceptionTypeEnum.INVALID_PARTNER_ID, HttpStatus.BAD_REQUEST);
        }
    }
}
