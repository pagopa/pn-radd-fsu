package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadResponse;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.RegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.entities.PnRaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.middleware.queue.consumer.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.pojo.ImportStatus;
import it.pagopa.pn.radd.pojo.OriginalRequest;
import it.pagopa.pn.radd.pojo.RaddRegistryImportConfig;
import it.pagopa.pn.radd.pojo.RaddRegistryImportStatus;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DUPLICATE_REQUEST;
import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.PENDING_REQUEST;
import static it.pagopa.pn.radd.pojo.RaddRegistryImportStatus.TO_PROCESS;
import static it.pagopa.pn.radd.utils.Const.ERROR_DUPLICATE;

@Service
@RequiredArgsConstructor
@CustomLog
public class RegistryService {

    public static final String TEXT_CSV = "text/csv";

    private final RegistryImportDAO registryImportDAO;
    private final PnSafeStorageClient pnSafeStorageClient;
    private final PnRaddFsuConfig pnRaddFsuConfig;
    private final ObjectMapperUtil objectMapper;

    public Mono<RegistryUploadResponse> uploadRegistryRequests(String xPagopaPnCxId, Mono<RegistryUploadRequest> registryUploadRequest) {
        String requestId = UUID.randomUUID().toString();
        return registryUploadRequest.flatMap(request ->
                registryImportDAO.getRegistryImportByCxId(xPagopaPnCxId)
                        .collectList()
                        .flatMap(entities -> checkImportRequest(request, entities))
                        .flatMap(o -> pnSafeStorageClient.createFile(getFileCreationRequestDto(), request.getChecksum()))
                        .flatMap(fileCreationResponseDto -> saveImportRequest(xPagopaPnCxId, request, fileCreationResponseDto, requestId).thenReturn(fileCreationResponseDto))
                        .map(fileCreationResponseDto -> mapUploadResponse(fileCreationResponseDto, requestId))
                        .doOnError(throwable -> log.error("Error uploading registry requests for cxId: {} ->", xPagopaPnCxId, throwable))
        );
    }

    @NotNull
    private FileCreationRequestDto getFileCreationRequestDto() {
        FileCreationRequestDto request = new FileCreationRequestDto();
        request.setStatus(Const.SAVED);
        request.setContentType(TEXT_CSV);
        request.setDocumentType(this.pnRaddFsuConfig.getRegistrySafeStorageDocType());
        return request;
    }

    private Mono<RegistryUploadRequest> checkImportRequest(RegistryUploadRequest request, List<PnRaddRegistryImportEntity> entities) {
        for (PnRaddRegistryImportEntity entity : entities) {
            if (request.getChecksum().equalsIgnoreCase(entity.getChecksum()) &&
                    (RaddRegistryImportStatus.PENDING.name().equalsIgnoreCase(entity.getStatus())
                            || (RaddRegistryImportStatus.TO_PROCESS.name().equalsIgnoreCase(entity.getStatus()) && Instant.now().isBefore(entity.getFileUploadDueDate())))) {
                return Mono.error(new RaddGenericException(ExceptionTypeEnum.valueOf(DUPLICATE_REQUEST.name()), HttpStatus.CONFLICT));
            } else if (RaddRegistryImportStatus.PENDING.name().equalsIgnoreCase(entity.getStatus())
                    || (RaddRegistryImportStatus.TO_PROCESS.name().equalsIgnoreCase(entity.getStatus()) && Instant.now().isBefore(entity.getFileUploadDueDate()))) {
                return Mono.error(new RaddGenericException(ExceptionTypeEnum.valueOf(PENDING_REQUEST.name()), HttpStatus.BAD_REQUEST));
            }
        }
        return Mono.just(request);
    }

    private RegistryUploadResponse mapUploadResponse(FileCreationResponseDto fileCreationResponseDto, String requestId) {
        RegistryUploadResponse registryUploadResponse = new RegistryUploadResponse();
        registryUploadResponse.setRequestId(requestId);
        registryUploadResponse.setFileKey(fileCreationResponseDto.getKey());
        registryUploadResponse.setUrl(fileCreationResponseDto.getUploadUrl());
        registryUploadResponse.setSecret(fileCreationResponseDto.getSecret());
        return registryUploadResponse;
    }

    private Mono<PnRaddRegistryImportEntity> saveImportRequest(String xPagopaPnCxId, RegistryUploadRequest request, FileCreationResponseDto fileCreationResponseDto, String requestId) {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = getPnRaddRegistryImportEntity(xPagopaPnCxId, request, fileCreationResponseDto, requestId);
        return registryImportDAO.putRaddRegistryImportEntity(pnRaddRegistryImportEntity);
    }

    private PnRaddRegistryImportEntity getPnRaddRegistryImportEntity(String xPagopaPnCxId, RegistryUploadRequest request, FileCreationResponseDto fileCreationResponseDto, String requestId) {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
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
        pnRaddRegistryImportEntity.setConfig(objectMapper.toJson(raddRegistryImportConfig));

        return pnRaddRegistryImportEntity;
    }

    public Mono<Void> handleMessage(PnAddressManagerEvent message) {
        return processMessage(message.getPayload().getResultItems(), message.getPayload().getCorrelationId());
    }
    private Mono<Void> processMessage(List<PnAddressManagerEvent.ResultItem> resultItems, String correlationId) {
        return pnRaddRegistryRequestDAO.findByCorrelationIdWithStatus(correlationId, ImportStatus.PENDING)
                .switchIfEmpty(Flux.error(new RaddGenericException("No pending items found for correlationId " + correlationId)))
                .flatMap(raddRegistryRequest -> processAddressForRegistryRequest(resultItems, raddRegistryRequest))
                .doOnError(
                        RaddGenericException.class,
                        exception -> log.warn("correlationId {} not found or not in PENDING", correlationId)
                )
                .then();
    }

    private Mono<RaddRegistryRequestEntity> processAddressForRegistryRequest(List<PnAddressManagerEvent.ResultItem> resultItems, RaddRegistryRequestEntity raddRegistryRequest) {
        return getRelativeItemFromAddressManagerEvent(resultItems, raddRegistryRequest.getPk())
                .flatMap(item -> {
                    String error = item.getError();
                    if (StringUtils.isNotBlank(error)) {
                        log.warn("Id {} error not empty", item.getError());
                        return pnRaddRegistryRequestDAO.updateStatusAndError(raddRegistryRequest, ImportStatus.REJECTED, error);
                    }
                    return handleRegistryUpdate(raddRegistryRequest, item);
                });
    }

    private Mono<RaddRegistryRequestEntity> handleRegistryUpdate(RaddRegistryRequestEntity raddRegistryRequestEntity, PnAddressManagerEvent.ResultItem resultItem) {
        UUID registryId = UUID.nameUUIDFromBytes(resultItem.getNormalizedAddress().toString().getBytes());

        return pnRaddRegistryDAO.find(registryId.toString(), raddRegistryRequestEntity.getCxId())
                .flatMap(entity -> updateRegistryRequestEntity(raddRegistryRequestEntity, entity))
                .switchIfEmpty(createNewRegistryEntity(raddRegistryRequestEntity, resultItem))
                .onErrorResume(throwable -> {
                    if (throwable instanceof RaddGenericException ex && ERROR_DUPLICATE.equals(ex.getMessage())) {
                        return pnRaddRegistryRequestDAO.updateStatusAndError(
                                raddRegistryRequestEntity,
                                ImportStatus.REJECTED,
                                ERROR_DUPLICATE
                        );
                    }
                    return Mono.error(throwable);
                });
    }

    private Mono<RaddRegistryRequestEntity> updateRegistryRequestEntity(RaddRegistryRequestEntity newRegistryRequestEntity, RaddRegistryEntity preExistingRegistryEntity) {
        if (StringUtils.equals(preExistingRegistryEntity.getRequestId(), newRegistryRequestEntity.getRequestId())) {
            return pnRaddRegistryRequestDAO.updateStatusAndError(newRegistryRequestEntity, ImportStatus.REJECTED, ERROR_DUPLICATE);
        } else {
            return mergeNewRegistryEntity(preExistingRegistryEntity, newRegistryRequestEntity)
                    .flatMap(updatedEntity -> pnRaddRegistryDAO.updateRegistryEntity(updatedEntity)
                            .flatMap(unused -> pnRaddRegistryRequestDAO.updateRegistryRequestStatus(newRegistryRequestEntity, RegistryRequestStatus.ACCEPTED)));
        }
    }

    private Mono<RaddRegistryRequestEntity> createNewRegistryEntity(RaddRegistryRequestEntity raddRegistryRequestEntity, PnAddressManagerEvent.ResultItem resultItem) {
        return constructRaddRegistryEntity(resultItem.getNormalizedAddress(), raddRegistryRequestEntity)
                .flatMap(item -> this.pnRaddRegistryDAO.putItemIfAbsent(item)
                        .onErrorResume(ConditionalCheckFailedException.class, ex -> Mono.error(new RaddGenericException(ERROR_DUPLICATE))))
                .flatMap(unused -> pnRaddRegistryRequestDAO.updateRegistryRequestStatus(raddRegistryRequestEntity, RegistryRequestStatus.ACCEPTED));

    }

    public Mono<RaddRegistryEntity> mergeNewRegistryEntity(RaddRegistryEntity preExistingRegistryEntity, RaddRegistryRequestEntity newRegistryRequestEntity) {
        return Mono.fromCallable(() -> {
            OriginalRequest originalRequest = objectMapper.readValue(newRegistryRequestEntity.getOriginalRequest(), OriginalRequest.class);

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

    private Mono<RaddRegistryEntity> constructRaddRegistryEntity(PnAddressManagerEvent.NormalizedAddress normalizedAddress, RaddRegistryRequestEntity registryRequest) {
        return Mono.fromCallable(() -> {
            String normalizedAddressString = objectMapper.writeValueAsString(normalizedAddress);
            OriginalRequest originalRequest = objectMapper.readValue(registryRequest.getOriginalRequest(), OriginalRequest.class);

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

    private Mono<PnAddressManagerEvent.ResultItem> getRelativeItemFromAddressManagerEvent(List<PnAddressManagerEvent.ResultItem> resultItems, String id) {
        Optional<PnAddressManagerEvent.ResultItem> resultItemOptional = resultItems.stream().filter(item -> StringUtils.equals(item.getId(), id)).findFirst();
        if (resultItemOptional.isEmpty()) {
            log.warn("Item with id {} not found or not in event list", id);
            return Mono.empty();
        }
        return Mono.just(resultItemOptional.get());
    }
}
