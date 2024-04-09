package it.pagopa.pn.radd.utils;

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
        registryEntity.setStartValidity(raddRegistryOriginalRequest.getStartValidity());
        registryEntity.setEndValidity(raddRegistryOriginalRequest.getEndValidity());

        return registryEntity;
    }

    public Mono<RaddRegistryEntity> constructRaddRegistryEntity(PnAddressManagerEvent.NormalizedAddress normalizedAddress, RaddRegistryRequestEntity registryRequest) {
        return Mono.fromCallable(() -> {
            RaddRegistryOriginalRequest raddRegistryOriginalRequest = objectMapperUtil.toObject(registryRequest.getOriginalRequest(), RaddRegistryOriginalRequest.class);

            return getRaddRegistryEntity(normalizedAddress, registryRequest, raddRegistryOriginalRequest);
        });
    }

    private static RaddRegistryEntity getRaddRegistryEntity(PnAddressManagerEvent.NormalizedAddress normalizedAddress, RaddRegistryRequestEntity registryRequest, RaddRegistryOriginalRequest raddRegistryOriginalRequest) {
        RaddRegistryEntity registryEntity = new RaddRegistryEntity();

        registryEntity.setRegistryId(registryRequest.getRegistryId());
        registryEntity.setCxId(registryRequest.getCxId());
        registryEntity.setNormalizedAddress(mapNormalizedAddressEntityToNormalizedAddress(normalizedAddress));
        registryEntity.setRequestId(registryRequest.getRequestId());
        // Metadata from originalRequest
        registryEntity.setDescription(raddRegistryOriginalRequest.getDescription());
        registryEntity.setPhoneNumber(raddRegistryOriginalRequest.getPhoneNumber());
        registryEntity.setGeoLocation(raddRegistryOriginalRequest.getGeoLocation());
        registryEntity.setZipCode(normalizedAddress.getCap());
        registryEntity.setOpeningTime(raddRegistryOriginalRequest.getOpeningTime());
        registryEntity.setStartValidity(raddRegistryOriginalRequest.getStartValidity());
        registryEntity.setEndValidity(raddRegistryOriginalRequest.getEndValidity());

        return registryEntity;
    }

    public Mono<PnAddressManagerEvent.ResultItem> getRelativeItemFromAddressManagerEvent(List<PnAddressManagerEvent.ResultItem> resultItems, String id) {
        Optional<PnAddressManagerEvent.ResultItem> resultItemOptional = resultItems.stream().filter(item -> StringUtils.equals(item.getId(), id)).findFirst();
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
            request.setId(entity.getPk());
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

    public RequestResponse prepareGlobalResult(List<RaddRegistryRequestEntity> queryResult,
                                               boolean moreResults,
                                               int limit) {
        RequestResponse result = new RequestResponse();
        result.setNextPagesKey(new ArrayList<>());

        if(queryResult != null) {
            result.setItems(queryResult.stream()
                    .limit(limit)
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
        result.setMoreResult(moreResults);

        for (int i = 1; i <= pnRaddFsuConfig.getMaxPageNumber(); i++){
            int index = limit * i;
            if (queryResult.size() <= index) {
                break;
            }
            RaddRegistryRequestEntity keyEntity = queryResult.get(index - 1);
            PnLastEvaluatedKey pageLastEvaluatedKey = computeLastEvaluatedKey(keyEntity);
            result.getNextPagesKey().add(pageLastEvaluatedKey.getExternalLastEvaluatedKey());
        }

        return result;
    }

    private PnLastEvaluatedKey computeLastEvaluatedKey(RaddRegistryRequestEntity keyEntity) {
        PnLastEvaluatedKey pageLastEvaluatedKey = new PnLastEvaluatedKey();
        pageLastEvaluatedKey.setExternalLastEvaluatedKey(keyEntity.getCxId());
        pageLastEvaluatedKey.setInternalLastEvaluatedKey(Map.of(
                RaddRegistryRequestEntity.COL_PK, AttributeValue.builder().s(keyEntity.getPk()).build(),
                RaddRegistryRequestEntity.COL_CX_ID, AttributeValue.builder().s(keyEntity.getCxId()).build(),
                RaddRegistryRequestEntity.COL_REGISTRY_ID, AttributeValue.builder().s(keyEntity.getRegistryId()).build()
        ));
        return pageLastEvaluatedKey;
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
