package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.exception.TransactionAlreadyExistsException;
import it.pagopa.pn.radd.mapper.DocumentUploadResponseMapper;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.NotificationRecipientV21Dto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.SentNotificationV21Dto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadResponse;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import it.pagopa.pn.radd.utils.PdfGenerator;
import it.pagopa.pn.radd.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HexFormat;
import java.util.Optional;

import static it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity.ITEMS_SEPARATOR;
import static it.pagopa.pn.radd.utils.Const.*;

@Service
@Slf4j
public class DocumentOperationsService {

    private final PnDeliveryClient pnDeliveryClient;
    private final RaddTransactionDAO raddTransactionDAO;
    private final PnSafeStorageClient pnSafeStorageClient;
    private final PdfGenerator pdfGenerator;

    public DocumentOperationsService(PnDeliveryClient pnDeliveryClient, RaddTransactionDAO raddTransactionDAO, PdfGenerator pdfGenerator, PnSafeStorageClient pnSafeStorageClient) {
        this.pnDeliveryClient = pnDeliveryClient;
        this.raddTransactionDAO = raddTransactionDAO;
        this.pdfGenerator = pdfGenerator;
        this.pnSafeStorageClient = pnSafeStorageClient;
    }

    public Mono<byte[]> documentDownload(String operationType, String operationId, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId) {
        return validateOperationTypeAndOperationId(operationType, operationId)
                .flatMap(isValid -> checkTransactionIsAlreadyExistsInCompletedErrorOrAborted(transactionIdBuilder(xPagopaPnCxType, xPagopaPnCxId, operationId), operationId))
                .flatMap(raddTansactionEntity -> pnDeliveryClient.getNotifications(raddTansactionEntity.getIun())
                        .zipWith(Mono.just(raddTansactionEntity)))
                .map(this::checkRecipientIdAndCreatePdf)
                .doOnError(throwable -> {
                    throw new RaddGenericException(throwable.getMessage());
                });
    }

    private byte @NotNull [] checkRecipientIdAndCreatePdf(Tuple2<SentNotificationV21Dto, RaddTransactionEntity> notificationEntityTuple) {
        Optional<NotificationRecipientV21Dto> recipient = notificationEntityTuple.getT1().getRecipients().stream()
                .filter(s -> notificationEntityTuple.getT2().getRecipientId().equals(s.getInternalId()))
                .findFirst();
        return generateCoverFile(recipient);
    }

    private byte @NotNull [] generateCoverFile(Optional<NotificationRecipientV21Dto> recipient) {
        if (recipient.isPresent()) {
            return generatePdf(recipient);
        }
        throw new RaddGenericException(ERROR_NO_RECIPIENT);

    }

    @NotNull
    private byte[] generatePdf(Optional<NotificationRecipientV21Dto> recipient) {
        try {
            byte[] byteArray = pdfGenerator.generateCoverFile(recipient.get().getDenomination());
            return HexFormat.of().parseHex(Hex.encodeHexString(byteArray));
        } catch (IOException e) {
            throw new RaddGenericException(e.getMessage());
        }
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

    private String transactionIdBuilder(CxTypeAuthFleet cxTypeAuthFleet, String xPagopaPnCxId, String operationId) {
        return cxTypeAuthFleet.getValue() + ITEMS_SEPARATOR + xPagopaPnCxId + ITEMS_SEPARATOR + operationId;
    }


    public Mono<DocumentUploadResponse> createFile(String uid, Mono<DocumentUploadRequest> documentUploadRequest) {
        if (documentUploadRequest==null){
            log.error(MISSING_INPUT_PARAMETERS);
            return Mono.error( new PnInvalidInputException("Body non valido") );
        }
        // retrieve presigned url
        return documentUploadRequest
                .flatMap(value -> pnSafeStorageClient.createFile(CONTENT_TYPE_ZIP, value.getChecksum()))
                .map(item -> {
                    log.info("Response presigned url : {}", item.getUploadUrl());
                    return DocumentUploadResponseMapper.fromResult(item);
                }).onErrorResume(RaddGenericException.class, ex -> Mono.just(DocumentUploadResponseMapper.fromException(ex)));
    }
}
