package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.*;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.pojo.RaddTransactionStatusEnum;
import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.utils.DateUtils;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import it.pagopa.pn.radd.utils.log.PnRaddAltAuditLog;
import it.pagopa.pn.radd.utils.log.PnRaddAltLogContext;
import it.pagopa.pn.radd.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

import java.util.List;
import java.util.function.Predicate;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.RETRY_AFTER;
import static it.pagopa.pn.radd.mapper.StartTransactionResponseMapper.getDownloadUrls;
import static it.pagopa.pn.radd.utils.Const.*;
import static it.pagopa.pn.radd.utils.OperationTypeEnum.AOR;

@Slf4j
@Service
public class AorService extends BaseService {
    private final PnDeliveryPushClient pnDeliveryPushClient;
    private final TransactionDataMapper transactionDataMapper;
    private final PnRaddFsuConfig pnRaddFsuConfig;
    private final Predicate<Throwable> isStartTransactionAcceptedException = ex -> ex instanceof TransactionAlreadyExistsException || ex instanceof PaperNotificationFailedEmptyException;

    public AorService(PnDeliveryPushClient pnDeliveryPushClient, PnDataVaultClient pnDataVaultClient, PnSafeStorageClient pnSafeStorageClient,
                      TransactionDataMapper transactionDataMapper, RaddTransactionDAO raddTransactionDAO, PnRaddFsuConfig pnRaddFsuConfig) {
        super(pnDataVaultClient, raddTransactionDAO, pnSafeStorageClient);
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.transactionDataMapper = transactionDataMapper;
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }

    public Mono<AORInquiryResponse> aorInquiry(String uid, String recipientTaxId, String recipientType, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId) {
        PnRaddAltAuditLog pnRaddAltAuditLog = PnRaddAltAuditLog.builder()
                .eventType(PnAuditLogEventType.AUD_RADD_AORINQUIRY)
                .msg(START_AOR_INQUIRY)
                .context(new PnRaddAltLogContext()
                        .addUid(uid)
                        .addCxId(xPagopaPnCxId)
                        .addCxType(xPagopaPnCxType.toString())
                )
                .build()
                .log();

        if (StringUtils.isBlank(recipientTaxId)) {
            throw new PnInvalidInputException("Il campo codice fiscale non è valorizzato");
        }
        return this.getEnsureFiscalCode(recipientTaxId, recipientType)
                .doOnNext(recipientInternalId -> pnRaddAltAuditLog.getContext().addRecipientInternalId(recipientInternalId))
                .flatMap(ensureFiscalCode ->
                        this.getIunFromPaperNotificationFailed(ensureFiscalCode)
                                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF)))
                                .collectList()
                                .doOnNext(list -> pnRaddAltAuditLog.getContext().addIuns(list).addAarFilekeys(list))
                                .doOnNext(list -> log.info("End of AORInquiry with documents list size {}", list.size()))
                                .map(list -> AorInquiryResponseMapper.fromResult())
                )
                .doOnNext(aorInquiryResponse -> {
                    pnRaddAltAuditLog.getContext().addResponseResult(aorInquiryResponse.getResult()).addResponseStatus(aorInquiryResponse.getStatus().toString());
                    pnRaddAltAuditLog.generateSuccessWithContext(END_AOR_INQUIRY);
                })
                .onErrorResume(RaddGenericException.class, ex ->
                        Mono.just(AorInquiryResponseMapper.fromException(ex))
                                .doOnNext(aorInquiryResponse -> {
                                    pnRaddAltAuditLog.getContext().addResponseStatus(aorInquiryResponse.getStatus().toString());
                                    pnRaddAltAuditLog.generateFailure(END_AOR_INQUIRY_WITH_ERROR, ex.getMessage(), ex);
                                })
                );
    }


    public Mono<CompleteTransactionResponse> completeTransaction(String uid, CompleteTransactionRequest completeTransactionRequest, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId) {
        PnRaddAltAuditLog pnRaddAltAuditLog = PnRaddAltAuditLog.builder()
                .eventType(PnAuditLogEventType.AUD_RADD_AORTRAN)
                .msg(START_AOR_COMPLETE_TRANSACTION)
                .context(new PnRaddAltLogContext()
                        .addUid(uid)
                        .addCxId(xPagopaPnCxId)
                        .addCxType(xPagopaPnCxType.toString())
                        .addOperationId(completeTransactionRequest.getOperationId())
                )
                .build().log();

        return this.validateCompleteRequest(completeTransactionRequest)
                .zipWhen(req -> this.getAndCheckStatusTransaction(req.getOperationId(), xPagopaPnCxType, xPagopaPnCxId))
                .map(reqAndEntity -> {
                    RaddTransactionEntity entity = reqAndEntity.getT2();
                    entity.setOperationEndDate(DateUtils.formatDate(reqAndEntity.getT1().getOperationDate()));
                    entity.setUid(uid);
                    return entity;
                })
                .doOnNext(entity -> log.debug("[uid={} - operationId={}] Updating transaction entity with status {}", entity.getUid(), entity.getOperationId(), entity.getStatus()))
                .flatMap(entity -> raddTransactionDAO.updateStatus(entity, RaddTransactionStatusEnum.COMPLETED))
                .map(entity -> CompleteTransactionResponseMapper.fromResult())
                .doOnNext(response -> {
                            pnRaddAltAuditLog.getContext().addResponseStatus(response.getStatus().toString());
                            pnRaddAltAuditLog.generateSuccessWithContext(END_AOR_COMPLETE_TRANSACTION);
                        }
                )
                .onErrorResume(RaddGenericException.class, ex ->
                        Mono.just(CompleteTransactionResponseMapper.fromException(ex))
                                .doOnNext(completeTransactionResponse -> {
                                    pnRaddAltAuditLog.getContext().addResponseStatus(completeTransactionResponse.getStatus().toString());
                                    pnRaddAltAuditLog.generateFailure(END_AOR_COMPLETE_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                                })
                );
    }

    public Mono<StartTransactionResponse> startTransaction(String uid, AorStartTransactionRequest request, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId) {
        PnRaddAltAuditLog pnRaddAltAuditLog = PnRaddAltAuditLog.builder()
                .eventType(PnAuditLogEventType.AUD_RADD_AORTRAN)
                .msg(START_AOR_START_TRANSACTION)
                .context(new PnRaddAltLogContext()
                        .addUid(uid)
                        .addCxType(xPagopaPnCxType.toString())
                        .addCxId(xPagopaPnCxId)
                        .addOperationId(request.getOperationId())
                        .addRequestFileKey(request.getFileKey())
                )
                .build()
                .log();

        return validationAorStartTransaction(uid, request, xPagopaPnCxType, xPagopaPnCxId)
                .flatMap(this::getEnsureRecipientAndDelegate)
                .doOnNext(transactionData -> pnRaddAltAuditLog.getContext().addRecipientInternalId(transactionData.getEnsureRecipientId())
                        .addDelegateInternalId(transactionData.getEnsureDelegateId()))
                .flatMap(transactionData -> setIunsOfNotificationFailed(transactionData, pnRaddAltAuditLog))
                .flatMap(transaction -> this.createAorTransaction(uid, transaction))
                .flatMap(this::verifyCheckSum)
                .flatMap(transactionData ->
                        this.getPresignedUrls(transactionData.getUrls()).sequential().collectList()
                                .map(urls -> {
                                    transactionData.setUrls(urls);
                                    return transactionData;
                                })
                )
                .doOnNext(transactionData -> log.debug("Update file metadata"))
                .flatMap(this::updateFileMetadata)
                .doOnNext(transactionData -> log.debug("End AOR startTransaction"))
                .map(data -> {
                    List<DownloadUrl> downloadUrls = getDownloadUrls(data.getUrls());
                    pnRaddAltAuditLog.getContext().addTransactionId(data.getTransactionId()).addDownloadFilekeys(downloadUrls);
                    return StartTransactionResponseMapper.fromResult(downloadUrls, AOR.name(), data.getOperationId(), pnRaddFsuConfig.getApplicationBasepath(), pnRaddFsuConfig.getDocumentTypeEnumFilter());
                })
                .doOnNext(startTransactionResponse -> {
                    pnRaddAltAuditLog.getContext().addResponseStatus(startTransactionResponse.getStatus().toString());
                    pnRaddAltAuditLog.generateSuccessWithContext(END_AOR_START_TRANSACTION);
                })
                .onErrorResume(isStartTransactionAcceptedException, ex ->
                        Mono.just(StartTransactionResponseMapper.fromException((RaddGenericException) ex))
                                .doOnNext(startTransactionResponse -> {
                                    pnRaddAltAuditLog.getContext().addResponseStatus(startTransactionResponse.getStatus().toString());
                                    pnRaddAltAuditLog.generateFailure(END_AOR_START_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                                })
                )
                .onErrorResume(RaddGenericException.class, ex -> this.settingErrorReason(ex, request.getOperationId(), OperationTypeEnum.AOR, xPagopaPnCxType, xPagopaPnCxId)
                                .map(entity -> StartTransactionResponseMapper.fromException(ex))
                                .doOnNext(startTransactionResponse -> {
                                    pnRaddAltAuditLog.getContext().addResponseStatus(startTransactionResponse.getStatus().toString());
                                    pnRaddAltAuditLog.generateFailure(END_AOR_START_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                                })
                );
    }

    public ParallelFlux<String> getPresignedUrls(List<String> listFileKey) {
        return Flux.fromStream(listFileKey.stream())
                .flatMap(this.safeStorageClient::getFile)
                .parallel()
                .map(file -> {
                    if (file.getDownload() != null && file.getDownload().getRetryAfter() != null && file.getDownload().getRetryAfter().intValue() != 0) {
                        log.info("Found document with retry after {}", file.getDownload().getRetryAfter());
                        throw new RaddGenericException(RETRY_AFTER, file.getDownload().getRetryAfter());
                    }
                    if (file.getDownload() != null) {
                        log.info("File : {}", file.getVersionId());
                        log.info("URL : {}", file.getDownload().getUrl());
                        return file.getDownload().getUrl();
                    }
                    return "";
                });
    }

    private Mono<TransactionData> createAorTransaction(String uid, TransactionData transaction) {
        RaddTransactionEntity entity = transactionDataMapper.toEntity(uid, transaction);
        List<OperationsIunsEntity> operations = transactionDataMapper.toOperationsIuns(transaction);
        log.debug("Create new Transaction entity iun={}, status={}", entity.getIun(), entity.getStatus());
        return this.raddTransactionDAO.createRaddTransaction(entity, operations).map(ent -> transaction);
    }

    private Mono<CompleteTransactionRequest> validateCompleteRequest(CompleteTransactionRequest req) {
        if (StringUtils.isEmpty(req.getOperationId())) {
            throw new PnInvalidInputException("Operation id non valorizzato");
        }
        return Mono.just(req);
    }


    public Mono<AbortTransactionResponse> abortTransaction(String uid, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, AbortTransactionRequest abortTransactionRequest) {
        PnRaddAltAuditLog pnRaddAltAuditLog = PnRaddAltAuditLog.builder()
                .eventType(PnAuditLogEventType.AUD_RADD_AORTRAN)
                .msg(START_AOR_ABORT_TRANSACTION)
                .context(new PnRaddAltLogContext()
                        .addUid(uid)
                        .addCxType(xPagopaPnCxType.toString())
                        .addCxId(xPagopaPnCxId)
                        .addOperationId(abortTransactionRequest.getOperationId())
                )
                .build()
                .log();

        if (StringUtils.isEmpty(abortTransactionRequest.getOperationId()) || StringUtils.isEmpty(abortTransactionRequest.getReason()) || abortTransactionRequest.getOperationDate() == null) {
            log.error("Missing input parameters");
            return Mono.error(new PnInvalidInputException("Alcuni parametri come operazione id o data di operazione non sono valorizzate"));
        }
        return raddTransactionDAO.getTransaction(String.valueOf(xPagopaPnCxType), xPagopaPnCxId, abortTransactionRequest.getOperationId(), AOR)
                .map(entity -> {
                    checkTransactionStatus(entity);
                    entity.setUid(uid);
                    entity.setErrorReason(abortTransactionRequest.getReason());
                    entity.setOperationEndDate(DateUtils.formatDate(abortTransactionRequest.getOperationDate()));
                    return entity;
                })
                .flatMap(entity -> raddTransactionDAO.updateStatus(entity, RaddTransactionStatusEnum.ABORTED))
                .map(result -> AbortTransactionResponseMapper.fromResult())
                .doOnNext(raddTransaction -> {
                    pnRaddAltAuditLog.getContext().addResponseStatus(raddTransaction.getStatus().toString());
                    pnRaddAltAuditLog.generateSuccessWithContext(END_AOR_ABORT_TRANSACTION);
                })
                .onErrorResume(RaddGenericException.class, ex ->
                        Mono.just(AbortTransactionResponseMapper.fromException(ex))
                                .doOnNext(abortTransactionResponse -> {
                                    pnRaddAltAuditLog.getContext().addResponseStatus(abortTransactionResponse.getStatus().toString());
                                    pnRaddAltAuditLog.generateFailure(END_AOR_ABORT_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                                })
                );
    }

    private Mono<TransactionData> validationAorStartTransaction(String uid, AorStartTransactionRequest req, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId) {
        if (Strings.isBlank(req.getOperationId())) {
            return Mono.error(new PnInvalidInputException("Id operazione non valorizzato"));
        }
        if (Strings.isBlank(req.getRecipientTaxId())) {
            return Mono.error(new PnInvalidInputException("Codice fiscale non valorizzato"));
        }
        if (!Utils.checkPersonType(req.getRecipientType().getValue())) {
            return Mono.error(new PnInvalidInputException("Recipient Type non valorizzato correttamente"));
        }
        return Mono.just(this.transactionDataMapper.toTransaction(uid, req, xPagopaPnCxType, xPagopaPnCxId));
    }

    private Flux<ResponsePaperNotificationFailedDtoDto> getIunFromPaperNotificationFailed(String recipientTaxId) {
        return this.pnDeliveryPushClient.getPaperNotificationFailed(recipientTaxId)
                .filter(item -> StringUtils.equalsIgnoreCase(recipientTaxId, item.getRecipientInternalId()))
                .onErrorResume(NullPointerException.class, ex -> Mono.error(new RaddGenericException(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED)));
    }

    private Mono<RaddTransactionEntity> getAndCheckStatusTransaction(String operationId, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId) {
        return raddTransactionDAO.getTransaction(String.valueOf(xPagopaPnCxType), xPagopaPnCxId, operationId, AOR)
                .doOnNext(raddTransaction -> log.debug("[{}] Check status entity : {}", operationId, raddTransaction.getStatus()))
                .doOnNext(this::checkTransactionStatus);
    }


    private Mono<TransactionData> setIunsOfNotificationFailed(TransactionData transaction, PnRaddAltAuditLog pnRaddAltAuditLog) {
        return this.getIunFromPaperNotificationFailed(transaction.getEnsureRecipientId())
                .doOnNext(item -> {
                    log.debug("Retrieved IUN : {}", item.getIun());
                    transaction.getUrls().add(item.getAarUrl());
                    transaction.getIuns().add(item.getIun());
                }).collectList()
                .doOnNext(list -> pnRaddAltAuditLog.getContext().addIuns(list))
                .map(item -> transaction);
    }

}
