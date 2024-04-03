package it.pagopa.pn.radd.utils;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadRequest;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.queue.consumer.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.pojo.OriginalRequest;
import it.pagopa.pn.radd.pojo.RaddRegistryImportConfig;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static it.pagopa.pn.radd.pojo.RaddRegistryImportStatus.TO_PROCESS;

@Component
@RequiredArgsConstructor
@CustomLog
public class RaddRegistryUtils {

    private final ObjectMapperUtil objectMapperUtil;
    private final PnRaddFsuConfig pnRaddFsuConfig;

    public Mono<RaddRegistryEntity> mergeNewRegistryEntity(RaddRegistryEntity preExistingRegistryEntity, RaddRegistryRequestEntity newRegistryRequestEntity) {
        return Mono.fromCallable(() -> {
            OriginalRequest originalRequest = objectMapperUtil.toObject(newRegistryRequestEntity.getOriginalRequest(), OriginalRequest.class);

            return getRaddRegistryEntity(preExistingRegistryEntity, newRegistryRequestEntity, originalRequest);
        });
    }

    private static RaddRegistryEntity getRaddRegistryEntity(RaddRegistryEntity preExistingRegistryEntity, RaddRegistryRequestEntity newRegistryRequestEntity, OriginalRequest originalRequest) {
        RaddRegistryEntity registryEntity = new RaddRegistryEntity();

        registryEntity.setRegistryId(preExistingRegistryEntity.getRegistryId());
        registryEntity.setCxId(preExistingRegistryEntity.getCxId());
        registryEntity.setNormalizedAddress(preExistingRegistryEntity.getNormalizedAddress());
        registryEntity.setRequestId(newRegistryRequestEntity.getRequestId());
        // Metadata from originalRequest
        registryEntity.setDescription(originalRequest.getDescription());
        registryEntity.setPhoneNumber(originalRequest.getPhoneNumber());
        registryEntity.setGeoLocation(originalRequest.getGeoLocation());
        registryEntity.setZipCode(newRegistryRequestEntity.getZipCode());
        registryEntity.setOpeningTime(originalRequest.getOpeningTime());
        registryEntity.setStartValidity(originalRequest.getStartValidity());
        registryEntity.setEndValidity(originalRequest.getEndValidity());

        return registryEntity;
    }

    public Mono<RaddRegistryEntity> constructRaddRegistryEntity(PnAddressManagerEvent.NormalizedAddress normalizedAddress, RaddRegistryRequestEntity registryRequest) {
        return Mono.fromCallable(() -> {
            String normalizedAddressString = objectMapperUtil.toJson(normalizedAddress);
            OriginalRequest originalRequest = objectMapperUtil.toObject(registryRequest.getOriginalRequest(), OriginalRequest.class);

            return getRaddRegistryEntity(normalizedAddress, registryRequest, normalizedAddressString, originalRequest);
        });
    }

    private static RaddRegistryEntity getRaddRegistryEntity(PnAddressManagerEvent.NormalizedAddress normalizedAddress, RaddRegistryRequestEntity registryRequest, String normalizedAddressString, OriginalRequest originalRequest) {
        RaddRegistryEntity registryEntity = new RaddRegistryEntity();

        registryEntity.setRegistryId(registryRequest.getRegistryId());
        registryEntity.setCxId(registryRequest.getCxId());
        registryEntity.setNormalizedAddress(normalizedAddressString);
        registryEntity.setRequestId(registryRequest.getRequestId());
        // Metadata from originalRequest
        registryEntity.setDescription(originalRequest.getDescription());
        registryEntity.setPhoneNumber(originalRequest.getPhoneNumber());
        registryEntity.setGeoLocation(originalRequest.getGeoLocation());
        registryEntity.setZipCode(normalizedAddress.getCap());
        registryEntity.setOpeningTime(originalRequest.getOpeningTime());
        registryEntity.setStartValidity(originalRequest.getStartValidity());
        registryEntity.setEndValidity(originalRequest.getEndValidity());

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
        pnRaddRegistryImportEntity.setFileUploadDueDate(Instant.now().plus(pnRaddFsuConfig.getRegitryImportUploadFileTtl(), ChronoUnit.SECONDS));

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

}
