package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.VerifyRequestResponse;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.msclient.PnAddressManagerClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.middleware.queue.consumer.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.middleware.queue.consumer.event.PnRaddAltNormalizeRequestEvent;
import it.pagopa.pn.radd.pojo.*;
import it.pagopa.pn.radd.utils.RaddRegistryUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DUPLICATE_REQUEST;
import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.PENDING_REQUEST;
import static it.pagopa.pn.radd.utils.Const.ERROR_DUPLICATE;

@Service
@RequiredArgsConstructor
@CustomLog
public class RegistryImportService {
    private final RaddRegistryRequestDAO raddRegistryRequestDAO;
    private final RaddRegistryDAO raddRegistryDAO;
    private final RaddRegistryImportDAO raddRegistryImportDAO;
    private final PnSafeStorageClient pnSafeStorageClient;
    private final RaddRegistryUtils raddRegistryUtils;
    private final PnAddressManagerClient pnAddressManagerClient;

    public Mono<RegistryUploadResponse> uploadRegistryRequests(String xPagopaPnCxId, Mono<RegistryUploadRequest> registryUploadRequest) {
        String requestId = UUID.randomUUID().toString();
        return registryUploadRequest.flatMap(request ->
                raddRegistryImportDAO.getRegistryImportByCxId(xPagopaPnCxId)
                        .collectList()
                        .flatMap(entities -> checkImportRequest(request, entities))
                        .flatMap(o -> pnSafeStorageClient.createFile(raddRegistryUtils.getFileCreationRequestDto(), request.getChecksum()))
                        .flatMap(fileCreationResponseDto -> saveImportRequest(xPagopaPnCxId, request, fileCreationResponseDto, requestId).thenReturn(fileCreationResponseDto))
                        .map(fileCreationResponseDto -> mapUploadResponse(fileCreationResponseDto, requestId))
                        .doOnError(throwable -> log.error("Error uploading registry requests for cxId: {} ->", xPagopaPnCxId, throwable))
        );
    }


    private Mono<RegistryUploadRequest> checkImportRequest(RegistryUploadRequest request, List<RaddRegistryImportEntity> entities) {
        for (RaddRegistryImportEntity entity : entities) {
            if (request.getChecksum().equalsIgnoreCase(entity.getChecksum()) &&
                    (RaddRegistryImportStatus.PENDING.name().equalsIgnoreCase(entity.getStatus())
                            || (RaddRegistryImportStatus.TO_PROCESS.name().equalsIgnoreCase(entity.getStatus()) && Instant.now().isAfter(entity.getFileUploadDueDate())))) {
                return Mono.error(new RaddGenericException(ExceptionTypeEnum.valueOf(DUPLICATE_REQUEST.name()), HttpStatus.CONFLICT));
            } else if (RaddRegistryImportStatus.PENDING.name().equalsIgnoreCase(entity.getStatus())
                    || (RaddRegistryImportStatus.TO_PROCESS.name().equalsIgnoreCase(entity.getStatus()) && Instant.now().isAfter(entity.getFileUploadDueDate()))) {
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

    private Mono<RaddRegistryImportEntity> saveImportRequest(String xPagopaPnCxId, RegistryUploadRequest request, FileCreationResponseDto fileCreationResponseDto, String requestId) {
        RaddRegistryImportEntity pnRaddRegistryImportEntity = raddRegistryUtils.getPnRaddRegistryImportEntity(xPagopaPnCxId, request, fileCreationResponseDto, requestId);
        return raddRegistryImportDAO.putRaddRegistryImportEntity(pnRaddRegistryImportEntity);
    }

    public Mono<Void> handleAddressManagerEvent(PnAddressManagerEvent message) {
        return processMessage(message.getPayload().getResultItems(), message.getPayload().getCorrelationId());
    }

    private Mono<Void> processMessage(List<PnAddressManagerEvent.ResultItem> resultItems, String correlationId) {
        String id = resultItems.get(0).getId();
        String cxId = PnAddressManagerEvent.ResultItem.retrieveCxIdFromId(id);
        String requestId = PnAddressManagerEvent.ResultItem.retrieveRequestIdFromId(id);
        return raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(cxId, requestId, ImportStatus.PENDING)
                .switchIfEmpty(Mono.error(new RaddGenericException(String.format("No pending import request found for cxId: [%s] and requestId: [%s] ", cxId, requestId))))
                .flatMap(registryImport -> raddRegistryRequestDAO.findByCorrelationIdWithStatus(correlationId, ImportStatus.PENDING)
                        .switchIfEmpty(Mono.error(new RaddGenericException("No pending items found for correlationId " + correlationId)))
                        .flatMap(raddRegistryRequest -> processAddressForRegistryRequest(resultItems, raddRegistryRequest)))
                .doOnError(throwable -> log.error("Error processing addressManager event: {}", throwable.getMessage(), throwable))
                .onErrorResume(RaddGenericException.class, e -> Mono.empty())
                .then();
    }

    private Mono<RaddRegistryRequestEntity> processAddressForRegistryRequest(List<PnAddressManagerEvent.ResultItem> resultItems, RaddRegistryRequestEntity raddRegistryRequest) {
        return raddRegistryUtils.getRelativeItemFromAddressManagerEvent(resultItems, raddRegistryRequest.getPk())
                .flatMap(item -> {
                    String error = item.getError();
                    if (StringUtils.isNotBlank(error)) {
                        log.warn("Id {} error not empty", item.getError());
                        return raddRegistryRequestDAO.updateStatusAndError(raddRegistryRequest, ImportStatus.REJECTED, error);
                    }
                    return handleRegistryUpdate(raddRegistryRequest, item);
                });
    }

    private Mono<RaddRegistryRequestEntity> handleRegistryUpdate(RaddRegistryRequestEntity raddRegistryRequestEntity, PnAddressManagerEvent.ResultItem resultItem) {
        UUID registryId = UUID.nameUUIDFromBytes(resultItem.getNormalizedAddress().toString().getBytes());

        return raddRegistryDAO.find(registryId.toString(), raddRegistryRequestEntity.getCxId())
                .flatMap(entity -> updateRegistryRequestEntity(raddRegistryRequestEntity, entity))
                .switchIfEmpty(createNewRegistryEntity(raddRegistryRequestEntity, resultItem))
                .onErrorResume(throwable -> {
                    if (throwable instanceof RaddGenericException ex && ERROR_DUPLICATE.equals(ex.getMessage())) {
                        return raddRegistryRequestDAO.updateStatusAndError(
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
            return raddRegistryRequestDAO.updateStatusAndError(newRegistryRequestEntity, ImportStatus.REJECTED, ERROR_DUPLICATE);
        } else {
            return raddRegistryUtils.mergeNewRegistryEntity(preExistingRegistryEntity, newRegistryRequestEntity)
                    .flatMap(updatedEntity -> raddRegistryDAO.updateRegistryEntity(updatedEntity)
                            .flatMap(unused -> raddRegistryRequestDAO.updateRegistryRequestStatus(newRegistryRequestEntity, RegistryRequestStatus.ACCEPTED)));
        }
    }

    private Mono<RaddRegistryRequestEntity> createNewRegistryEntity(RaddRegistryRequestEntity raddRegistryRequestEntity, PnAddressManagerEvent.ResultItem resultItem) {
        return raddRegistryUtils.constructRaddRegistryEntity(resultItem.getNormalizedAddress(), raddRegistryRequestEntity)
                .flatMap(item -> this.raddRegistryDAO.putItemIfAbsent(item)
                        .onErrorResume(ConditionalCheckFailedException.class, ex -> Mono.error(new RaddGenericException(ERROR_DUPLICATE))))
                .flatMap(unused -> raddRegistryRequestDAO.updateRegistryRequestStatus(raddRegistryRequestEntity, RegistryRequestStatus.ACCEPTED));

    }
    public Mono<VerifyRequestResponse> verifyRegistriesImportRequest(String xPagopaPnCxId, String requestId) {
        log.info("start verifyRegistriesImportRequest for cxId: {} and requestId: {}", xPagopaPnCxId, requestId);
        return raddRegistryImportDAO.getRegistryImportByCxIdAndRequestId(xPagopaPnCxId, requestId)
                .switchIfEmpty(Mono.error(new RaddGenericException(String.format("No import request found for cxId: [%s] and requestId: [%s] ", xPagopaPnCxId, requestId))))
                .map(this::createVerifyRequestResponse)
                .doOnError(throwable -> log.error("Error during verify registries import request for cxId: [{}] and requestId: [{}]", xPagopaPnCxId, requestId, throwable));
    }
    private VerifyRequestResponse createVerifyRequestResponse(RaddRegistryImportEntity entity) {
        VerifyRequestResponse response = new VerifyRequestResponse();
/*
        response.setRequestId(entity.getRequestId());
*/
        response.setStatus(entity.getStatus());
        response.setError(entity.getError());
        return response;
    }

    public Mono<Void> handleNormalizeRequestEvent(PnRaddAltNormalizeRequestEvent.Payload payload) {
        AddressManagerRequest request = new AddressManagerRequest();
        request.setCorrelationId(payload.getCorrelationId());

        return raddRegistryRequestDAO.getAllFromCorrelationId(payload.getCorrelationId(), RegistryRequestStatus.NOT_WORKED.name())
                .collectList()
                .zipWhen(entities -> Mono.just(raddRegistryUtils.getRequestAddressFromOriginalRequest(entities)))
                .flatMap(tuple -> {
                    request.setAddresses(tuple.getT2());
                    String addressManagerApiKey = raddRegistryUtils.retrieveAddressManagerApiKey();
                    return pnAddressManagerClient.normalizeAddresses(request, addressManagerApiKey).thenReturn(tuple);
                })
                .flatMap(tuple -> raddRegistryRequestDAO.updateRecordsInPending(tuple.getT1()));
    }

}
