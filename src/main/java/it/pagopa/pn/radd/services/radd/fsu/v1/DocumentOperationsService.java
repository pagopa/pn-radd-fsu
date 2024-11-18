package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.NotificationRecipientV23Dto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV24Dto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DocumentUploadRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DocumentUploadResponse;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.DocumentUploadResponseMapper;
import it.pagopa.pn.radd.middleware.db.OperationsIunsDAO;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;

import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.DocumentDownloadClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HexFormat;
import java.util.Optional;

import static it.pagopa.pn.radd.utils.Const.*;
import static it.pagopa.pn.radd.utils.OperationTypeEnum.AOR;
import static it.pagopa.pn.radd.utils.RaddRole.RADD_UPLOADER;
import static it.pagopa.pn.radd.utils.Utils.transactionIdBuilder;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentOperationsService {

    private final PnDeliveryClient pnDeliveryClient;
    private final RaddTransactionDAO raddTransactionDAO;
    private final PnSafeStorageClient pnSafeStorageClient;
    private final DocumentDownloadClient documentDownloadClient;
    private final PdfGenerator pdfGenerator;
    private final OperationsIunsDAO operationsIunsDAO;
    private final PnRaddFsuConfig pnRaddFsuConfig;


    public Mono<byte[]> documentDownload(String operationType, String operationId, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String attachmentId) {
        return validateOperationTypeAndOperationId(operationType, operationId)
                .flatMap(isValid -> checkTransactionIsAlreadyExistsInCompletedErrorOrAborted(transactionIdBuilder(xPagopaPnCxType, xPagopaPnCxId, operationId), operationType))
                .flatMap(raddTansactionEntity -> {
                    if (StringUtils.hasText(attachmentId)) {
                        return getPdfInZipAttachment(attachmentId, raddTansactionEntity);
                    } else {
                        return checkOperationType(operationType, raddTansactionEntity)
                                .flatMap(iun -> createCoverFile(raddTansactionEntity, iun));
                    }
                })
                .map(DocumentOperationsService::getHexBytes);
    }

    private Mono<String> checkOperationType(String operationType, RaddTransactionEntity raddTansactionEntity) {
        if (AOR.name().equals(operationType)) {
            return operationsIunsDAO.getAllIunsFromTransactionId(raddTansactionEntity.getTransactionId())
                    .next()
                    .map(OperationsIunsEntity::getIun);
        }
        return Mono.just(raddTansactionEntity.getIun());
    }


    @NotNull
    private Mono<byte[]> getPdfInZipAttachment(String attachmentId, RaddTransactionEntity raddTansactionEntity) {
        if (raddTansactionEntity.getZipAttachments() != null &&
                StringUtils.hasText(raddTansactionEntity.getZipAttachments().get(attachmentId))) {
            String zipUrl = raddTansactionEntity.getZipAttachments().get(attachmentId);
            return documentDownloadClient.downloadContent(zipUrl)
                    .map(ZipUtils::extractPdfFromZip);
        }
        throw new ZipAttachmentNotFoundException();
    }

    @NotNull
    private Mono<byte[]> createCoverFile(RaddTransactionEntity raddTansactionEntity, String iun) {
        return pnDeliveryClient.getNotifications(iun)
                .map(sentNotificationV23Dto -> checkRecipientIdAndCreatePdf(sentNotificationV23Dto, raddTansactionEntity.getRecipientId()));
    }

    private byte @NotNull [] checkRecipientIdAndCreatePdf(SentNotificationV24Dto sentNotificationV23Dto, String internalId) {
        Optional<NotificationRecipientV23Dto> recipient = sentNotificationV23Dto.getRecipients().stream()
                .filter(notificationRecipient -> internalId.equals(notificationRecipient.getInternalId()))
                .findFirst();
        if (recipient.isPresent()) {
            return generatePdf(recipient.get());
        }
        throw new RaddGenericException(ERROR_NO_RECIPIENT);
    }

    private byte @NotNull [] generatePdf(NotificationRecipientV23Dto recipient) {
        try {
            return pdfGenerator.generateCoverFile(recipient.getDenomination());
        } catch (IOException e) {
            throw new RaddGenericException(e.getMessage());
        }
    }

    private static byte[] getHexBytes(byte[] byteArray) {
        return HexFormat.of().parseHex(Hex.encodeHexString(byteArray));
    }

    private Mono<Boolean> validateOperationTypeAndOperationId(String operationType, String operationId) {
        if (!StringUtils.hasText(operationType) || !Utils.checkOperationType(operationType)) {
            log.error(MISSING_INPUT_PARAMETERS);
            return Mono.error(new PnInvalidInputException("OperationType non valorizzato correttamente"));
        }
        if (!StringUtils.hasText(operationId)) {
            log.error(MISSING_INPUT_PARAMETERS);
            return Mono.error(new PnInvalidInputException("OperationId non valorizzato correttamente"));
        }
        return Mono.just(true);
    }

    private Mono<RaddTransactionEntity> checkTransactionIsAlreadyExistsInCompletedErrorOrAborted(String transactionId, String operationType) {
        return this.raddTransactionDAO.getTransaction(transactionId, OperationTypeEnum.valueOf(operationType))
                .map(raddTransactionEntity -> {
                    if (raddTransactionEntity.getStatus().equals(Const.ABORTED) ||
                            raddTransactionEntity.getStatus().equals(Const.COMPLETED) ||
                            raddTransactionEntity.getStatus().equals(Const.ERROR)) {
                        throw new TransactionAlreadyExistsException();
                    }
                    return raddTransactionEntity;
                });
    }


    public Mono<DocumentUploadResponse> createFile(Mono<DocumentUploadRequest> documentUploadRequest, String xPagopaPnCxRole) {
        if (documentUploadRequest == null) {
            log.error(MISSING_INPUT_PARAMETERS);
            return Mono.error(new PnInvalidInputException("Body non valido"));
        }
        checkRole(xPagopaPnCxRole);
        FileCreationRequestDto request = getFileCreationRequestDto();
        // retrieve presigned url
        return documentUploadRequest
                .flatMap(value -> pnSafeStorageClient.createFile(request, value.getChecksum()))
                .map(item -> {
                    log.info("Response presigned url : {}", item.getUploadUrl());
                    return DocumentUploadResponseMapper.fromResult(item);
                }).onErrorResume(RaddGenericException.class, ex -> Mono.just(DocumentUploadResponseMapper.fromException(ex)));
    }

    private static void checkRole(String xPagopaPnCxRole) {
        if (!String.valueOf(RADD_UPLOADER).equals(xPagopaPnCxRole)) {
            log.error("Access denied for role: {}", xPagopaPnCxRole);
            throw new PnRaddForbiddenException("Accesso negato.", HttpStatus.FORBIDDEN.value());
        }
    }

    @NotNull
    private FileCreationRequestDto getFileCreationRequestDto() {
        FileCreationRequestDto request = new FileCreationRequestDto();
        request.setStatus(Const.PRELOADED);
        request.setContentType(CONTENT_TYPE_ZIP);
        request.setDocumentType(this.pnRaddFsuConfig.getSafeStorageDocType());
        return request;
    }
}
