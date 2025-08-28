package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.NotificationPaymentItemDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV25Dto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactCategoryV20Dto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactDownloadMetadataWithContentTypeResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactListElementV20Dto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.dto.NotificationStatusV26Dto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.*;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.pojo.*;
import it.pagopa.pn.radd.utils.DateUtils;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import it.pagopa.pn.radd.utils.Utils;
import it.pagopa.pn.radd.utils.log.PnRaddAltAuditLog;
import it.pagopa.pn.radd.utils.log.PnRaddAltLogContext;
import lombok.CustomLog;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.*;
import static it.pagopa.pn.radd.pojo.NotificationAttachment.AttachmentType.*;
import static it.pagopa.pn.radd.utils.Const.*;
import static it.pagopa.pn.radd.utils.Utils.getDocumentDownloadUrl;
import static org.springframework.util.StringUtils.hasText;

@Service
@CustomLog
public class ActService extends BaseService {
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;
    private final TransactionDataMapper transactionDataMapper;
    private final PnRaddFsuConfig pnRaddFsuConfig;
    private final Predicate<Throwable> isStartTransactionAcceptedException = ex -> ex instanceof IunAlreadyExistsException
            || ex instanceof TransactionAlreadyExistsException;

    public ActService(RaddTransactionDAO raddTransactionDAO, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient, PnDataVaultClient pnDataVaultClient, PnSafeStorageClient safeStorageClient, TransactionDataMapper transactionDataMapper, PnRaddFsuConfig pnRaddFsuConfig) {
        super(pnDataVaultClient, raddTransactionDAO, safeStorageClient);
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.transactionDataMapper = transactionDataMapper;
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }

    public Mono<ActInquiryResponse> actInquiry(String uid, String xPagopaPnCxId, CxTypeAuthFleet xPagopaPnCxType, String recipientTaxId, String recipientType, String qrCode, String iun) {
        PnRaddAltAuditLog raddAltAuditLog = PnRaddAltAuditLog.builder()
                .eventType(PnAuditLogEventType.AUD_RADD_ACTINQUIRY)
                .msg(START_ACT_INQUIRY)
                .context(new PnRaddAltLogContext()
                        .addUid(uid)
                        .addCxId(xPagopaPnCxId)
                        .addCxType(xPagopaPnCxType.toString())
                )
                .build()
                .log();

        return validateRecTaxIdRecTypeQrCodeIun(recipientTaxId, recipientType, qrCode, iun)
                .doOnNext(isValid -> log.trace("ACT INQUIRY TICK {}", new Date().getTime()))
                .flatMap(isValid -> getEnsureFiscalCode(recipientTaxId, recipientType))
                .doOnNext(recipientInternalId -> raddAltAuditLog.getContext().addRecipientInternalId(recipientInternalId))
                .flatMap(recCode -> checkQrCodeOrIun(recipientType, qrCode, iun, recCode))
                .doOnNext(usedIun -> raddAltAuditLog.getContext().addIun(usedIun))
                .flatMap(this::hasNotificationsCancelled)
                .flatMap(this::hasDocumentsAvailable)
                .doOnNext(nothing -> log.trace("ACT INQUIRY TOCK {}", new Date().getTime()))
                .map(item -> ActInquiryResponseMapper.fromResult())
                .doOnNext(response -> {
                    raddAltAuditLog.getContext().addResponseResult(response.getResult()).addResponseStatus(response.getStatus().toString());
                    raddAltAuditLog.generateSuccessWithContext(END_ACT_INQUIRY);
                })
                .onErrorMap(PnRaddException.class, e -> new RaddGenericException(e.getMessage()))
                .onErrorResume(RaddGenericException.class, ex ->
                        Mono.just(ActInquiryResponseMapper.fromException(ex))
                                .doOnNext(response -> {
                                    raddAltAuditLog.getContext().addResponseStatus(response.getStatus().toString());
                                    raddAltAuditLog.generateFailure(END_ACT_INQUIRY_WITH_ERROR, ex.getMessage(), ex);
                                })
                );
    }

    @NotNull
    private Mono<String> checkQrCodeOrIun(String recipientType, String qrCode, String iun, String recipientId) {
        if (hasText(qrCode)) {
            return checkQrCode(recipientType, qrCode, recipientId);
        } else {
            return checkIun(iun, recipientId, recipientId);
        }
    }

    @NotNull
    private Mono<String> checkIun(String iun, String recCode, String recipientId) {
        return checkIunIsAlreadyExistsInCompleted(iun, recipientId)
                .flatMap(counter -> checkIunAndInternalId(iun, recCode)
                        .thenReturn(iun));
    }

    @NotNull
    private Mono<String> checkQrCode(String recipientType, String qrCode, String recipientId) {
        return controlAndCheckAar(recipientType, recipientId, qrCode)
                .flatMap(responseCheckAarDtoDto -> checkIunIsAlreadyExistsInCompleted(responseCheckAarDtoDto.getIun(), recipientId)
                        .thenReturn(responseCheckAarDtoDto.getIun()));
    }

    private Mono<Void> checkIunAndInternalId(String iun, String internalId) {
        return pnDeliveryClient.checkIunAndInternalId(iun, internalId);
    }

    private Mono<Integer> checkIunIsAlreadyExistsInCompleted(String iun, String recipientId) {
        return this.raddTransactionDAO.countFromIunAndStatus(iun, recipientId)
                .filter(counter -> pnRaddFsuConfig.getMaxPrintRequests() == 0 || counter < pnRaddFsuConfig.getMaxPrintRequests())
                .switchIfEmpty(Mono.error(new IunAlreadyExistsException(pnRaddFsuConfig.getMaxPrintRequests())))
                .doOnError(err -> log.error(err.getMessage()));
    }

    public Mono<StartTransactionResponse> startTransaction(String uid, String xPagopaPnCxId, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxRole, ActStartTransactionRequest request) {
        PnRaddAltAuditLog pnRaddAltAuditLog = PnRaddAltAuditLog.builder()
                .eventType(PnAuditLogEventType.AUD_RADD_ACTTRAN)
                .msg(START_ACT_START_TRANSACTION)
                .context(new PnRaddAltLogContext()
                        .addUid(uid)
                        .addCxType(xPagopaPnCxType.toString())
                        .addCxId(xPagopaPnCxId)
                        .addOperationId(request.getOperationId()))
                .build()
                .log();

        return verifyRoleForStarTransaction(xPagopaPnCxRole, request.getFileKey(), request.getChecksum(), request.getVersionToken())
                .then(validateAndSettingsData(uid, request, xPagopaPnCxType, xPagopaPnCxId))
                .flatMap(this::getEnsureRecipientAndDelegate)
                .doOnNext(transactionData -> {
                    pnRaddAltAuditLog.getContext().addRecipientInternalId(transactionData.getEnsureRecipientId());
                    if (StringUtils.hasText(transactionData.getEnsureDelegateId())) {
                        pnRaddAltAuditLog.getContext().addDelegateInternalId(transactionData.getEnsureDelegateId());
                    }
                })
                .flatMap(transactionData -> checkQrCodeOrIun(request.getRecipientType().getValue(), request.getQrCode(), request.getIun(), transactionData.getEnsureRecipientId())
                        .map(s -> setIun(transactionData, s)))
                .flatMap(transactionData -> hasNotificationsCancelled(transactionData.getIun())
                        .thenReturn(transactionData))
                .flatMap(transactionData -> this.createRaddTransaction(uid, transactionData))
                .doOnNext(transactionData ->
                        pnRaddAltAuditLog.getContext().addTransactionId(transactionData.getTransactionId())
                                .addIun(transactionData.getIun())
                )
                .flatMap(this::verifyCheckSum)
                .zipWhen(transaction -> hasDocumentsAvailable(transaction.getIun()))
                .zipWhen(transactionAndSentNotification -> retrieveDocumentsAndAttachments(request, transactionAndSentNotification),
                        (tupla, response) -> Tuples.of(tupla.getT1(), response))
                .zipWhen(transactionAndResponse -> this.updateFileMetadata(transactionAndResponse.getT1()), (in, out) -> in.getT2())
                .map(response -> {
                    log.trace("START ACT TRANSACTION TOCK {}", new Date().getTime());
                    pnRaddAltAuditLog.getContext().addDownloadFilekeys(response.getDownloadUrlList()).addResponseStatus(response.getStatus().toString());
                    pnRaddAltAuditLog.generateSuccessWithContext(END_ACT_START_TRANSACTION);
                    return response;
                })
                .onErrorResume(PnRaddException.class, ex -> {
                    pnRaddAltAuditLog.generateFailure(END_ACT_START_TRANSACTION_WITH_ERROR, ex.getWebClientEx().getMessage(), ex);
                    return this.settingErrorReason(ex, request.getOperationId(), OperationTypeEnum.ACT, xPagopaPnCxType, xPagopaPnCxId)
                            .flatMap(entity -> Mono.error(ex));
                })
                .onErrorResume(PnRaddBadRequestException.class, ex -> {
                    pnRaddAltAuditLog.generateFailure(END_ACT_START_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                    return Mono.error(ex);
                })
                .onErrorResume(isStartTransactionAcceptedException, ex ->
                        Mono.just(StartTransactionResponseMapper.fromException((RaddGenericException) ex))
                                .doOnNext(startTransactionResponse -> {
                                    pnRaddAltAuditLog.getContext().addResponseStatus(startTransactionResponse.getStatus().toString());
                                    pnRaddAltAuditLog.generateFailure(END_ACT_START_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                                })
                )
                .onErrorResume(RaddGenericException.class, ex -> this.settingErrorReason(ex, request.getOperationId(), OperationTypeEnum.ACT, xPagopaPnCxType, xPagopaPnCxId)
                        .map(entity -> StartTransactionResponseMapper.fromException(ex))
                        .doOnNext(startTransactionResponse -> {
                            pnRaddAltAuditLog.getContext().addResponseStatus(startTransactionResponse.getStatus().toString());
                            pnRaddAltAuditLog.generateFailure(END_ACT_START_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                        }));
    }



    @NotNull
    private Mono<StartTransactionResponse> retrieveDocumentsAndAttachments(ActStartTransactionRequest request, Tuple2<TransactionData, SentNotificationV25Dto> transactionAndSentNotification) {
        log.debug("Retrieving document and attachments");
        Flux<DownloadUrl> urlDocuments = getUrlDoc(transactionAndSentNotification.getT1(), transactionAndSentNotification.getT2());
        Flux<DownloadUrl> urlAttachments = getUrlsAttachments(transactionAndSentNotification.getT1(), transactionAndSentNotification.getT2());
        Flux<DownloadUrl> urlLegalFacts = legalFact(transactionAndSentNotification.getT1());
        return ParallelFlux.from(urlDocuments, urlAttachments, urlLegalFacts)
                .sequential()
                .collectList()
                .map(resultList -> StartTransactionResponseMapper.fromResult(resultList, OperationTypeEnum.ACT.name(), request.getOperationId(), pnRaddFsuConfig.getApplicationBasepath(), pnRaddFsuConfig.getDocumentTypeEnumFilter()));
    }

    @NotNull
    private static TransactionData setIun(TransactionData transactionData, String s) {
        transactionData.setIun(s);
        return transactionData;
    }

    public Mono<CompleteTransactionResponse> completeTransaction(String uid, CompleteTransactionRequest
            completeTransactionRequest, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId) {
        PnRaddAltAuditLog pnRaddAltAuditLog = PnRaddAltAuditLog.builder()
                .eventType(PnAuditLogEventType.AUD_RADD_ACTTRAN)
                .msg(START_ACT_COMPLETE_TRANSACTION)
                .context(new PnRaddAltLogContext()
                        .addUid(uid)
                        .addCxType(xPagopaPnCxType.toString())
                        .addCxId(xPagopaPnCxId)
                        .addOperationId(completeTransactionRequest.getOperationId()))
                .build()
                .log();

        return this.validateCompleteTransactionRequest(completeTransactionRequest)
                .zipWhen(req -> getAndCheckStatusTransaction(req.getOperationId(), xPagopaPnCxType, xPagopaPnCxId))
                .zipWhen(reqAndEntity -> this.pnDeliveryPushClient.notifyNotificationRaddRetrieved(reqAndEntity.getT2(), reqAndEntity.getT1().getOperationDate()), (reqAndEntity, response) -> reqAndEntity)
                .map(reqAndEntity -> {
                    RaddTransactionEntity entity = reqAndEntity.getT2();
                    entity.setOperationEndDate(DateUtils.formatDate(reqAndEntity.getT1().getOperationDate()));
                    entity.setUid(uid);
                    return entity;
                })
                .doOnNext(raddTransaction -> log.debug("[uid={} - operationId={}] updating transaction entity with status {}", raddTransaction.getUid(), raddTransaction.getOperationId(), raddTransaction.getStatus()))
                .flatMap(entity -> this.raddTransactionDAO.updateStatus(entity, RaddTransactionStatusEnum.COMPLETED))
                .doOnNext(entity -> log.debug("[uid={} - transactionId={}]  New status of transaction entity is {}", entity.getUid(), entity.getTransactionId(), entity.getStatus()))
                .doOnNext(entity -> log.debug("[uid={} - transactionId={}] End ACT Complete transaction", entity.getUid(), entity.getTransactionId()))
                .map(entity -> CompleteTransactionResponseMapper.fromResult())
                .doOnNext(completeTransactionResponse -> {
                    pnRaddAltAuditLog.getContext().addResponseStatus(completeTransactionResponse.getStatus().toString());
                    pnRaddAltAuditLog.generateSuccessWithContext(END_ACT_COMPLETE_TRANSACTION);
                })
                .onErrorResume(PnRaddException.class, ex -> {
                    pnRaddAltAuditLog.generateFailure(END_ACT_COMPLETE_TRANSACTION_WITH_ERROR, ex.getWebClientEx().getMessage(), ex);
                    return this.settingErrorReason(ex, completeTransactionRequest.getOperationId(), OperationTypeEnum.ACT, xPagopaPnCxType, xPagopaPnCxId)
                            .flatMap(entity -> Mono.error(ex));
                })
                .onErrorResume(RaddGenericException.class, ex ->
                        Mono.just(CompleteTransactionResponseMapper.fromException(ex))
                                .doOnNext(completeTransactionResponse -> {
                                    pnRaddAltAuditLog.getContext().addResponseStatus(completeTransactionResponse.getStatus().toString());
                                    pnRaddAltAuditLog.generateFailure(END_ACT_COMPLETE_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                                })
                );
    }

    public Mono<AbortTransactionResponse> abortTransaction(String uid, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, AbortTransactionRequest req) {
        if (req == null || !StringUtils.hasText(req.getOperationId())
                || !StringUtils.hasText(req.getReason())) {
            log.error("Missing input parameters");
            return Mono.error(new PnInvalidInputException("Alcuni parametri come operazione id o data di operazione non sono valorizzate"));
        }

        PnRaddAltAuditLog pnRaddAltAuditLog = PnRaddAltAuditLog.builder()
                .eventType(PnAuditLogEventType.AUD_RADD_ACTTRAN)
                .msg(START_ACT_ABORT_TRANSACTION)
                .context(new PnRaddAltLogContext()
                        .addUid(uid)
                        .addCxType(xPagopaPnCxType.toString())
                        .addCxId(xPagopaPnCxId)
                        .addOperationId(req.getOperationId()))
                .build()
                .log();
        return raddTransactionDAO.getTransaction(String.valueOf(xPagopaPnCxType), xPagopaPnCxId, req.getOperationId(), OperationTypeEnum.ACT)
                .doOnNext(this::checkTransactionStatus)
                .map(raddEntity -> {
                    raddEntity.setUid(uid);
                    raddEntity.setErrorReason(req.getReason());
                    raddEntity.setOperationEndDate(DateUtils.formatDate(req.getOperationDate()));
                    return raddEntity;
                })
                .flatMap(entity -> raddTransactionDAO.updateStatus(entity, RaddTransactionStatusEnum.ABORTED))
                .doOnNext(raddTransaction -> log.debug("[uid={} - transactionId={}] End ACT abortTransaction with entity status {}", raddTransaction.getUid(), raddTransaction.getTransactionId(), raddTransaction.getStatus()))
                .map(result -> AbortTransactionResponseMapper.fromResult())
                .doOnNext(abortTransactionResponse -> {
                    pnRaddAltAuditLog.getContext().addResponseStatus(abortTransactionResponse.getStatus().toString());
                    pnRaddAltAuditLog.generateSuccessWithContext(END_ACT_ABORT_TRANSACTION);
                })
                .onErrorResume(RaddGenericException.class, ex ->
                        Mono.just(AbortTransactionResponseMapper.fromException(ex))
                                .doOnNext(abortTransactionResponse -> {
                                    pnRaddAltAuditLog.getContext().addResponseStatus(abortTransactionResponse.getStatus().toString());
                                    pnRaddAltAuditLog.generateFailure(END_ACT_ABORT_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                                })
                );
    }

    private Flux<DownloadUrl> legalFact(TransactionData transaction) {
        return pnDeliveryPushClient.getNotificationLegalFacts(transaction.getEnsureRecipientId(), transaction.getIun())
                .filter(filterLegalFacts(transaction))
                .flatMap(item ->
                        pnDeliveryPushClient.getLegalFact(transaction.getEnsureRecipientId(),
                                        transaction.getIun(),
                                        item.getLegalFactsId().getKey())
                                .filter(legalFact -> CONTENT_TYPE_PDF.equals(legalFact.getContentType()) ||
                                        CONTENT_TYPE_ZIP.equals(legalFact.getContentType()))
                                .mapNotNull(legalFact -> getLegalFactInfo(item, legalFact)))
                .collectList()
                .flatMap(legalFactInfoList -> updateZipAttachments(transaction, legalFactInfoList))
                .flatMapMany(Flux::fromIterable)
                .map(legalFactInfo -> getDownloadUrl(transaction, legalFactInfo))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @NotNull
    private DownloadUrl getDownloadUrl(TransactionData transaction, LegalFactInfo legalFactInfo) {
        if (CONTENT_TYPE_PDF.equals(legalFactInfo.getContentType())) {
            return getDownloadUrl(legalFactInfo.getUrl(), getDocumentType(legalFactInfo));
        } else {
            return getDocumentDownloadUrl(pnRaddFsuConfig.getApplicationBasepath(),
                    transaction.getOperationType().name(),
                    transaction.getOperationId(),
                    legalFactInfo.getKey(),
                    getDocumentType(legalFactInfo));
        }
    }

    @NotNull
    private static String getDocumentType(LegalFactInfo legalFactInfo) {
        return LegalFactCategoryV20Dto.PEC_RECEIPT.equals(legalFactInfo.getCategory()) ||
                LegalFactCategoryV20Dto.ANALOG_DELIVERY.equals(legalFactInfo.getCategory()) ?
                DocumentTypeEnum.LEGAL_FACT_EXTERNAL.name() :
                DocumentTypeEnum.LEGAL_FACT.name();
    }

    @NotNull
    private static Predicate<LegalFactListElementV20Dto> filterLegalFacts(TransactionData transaction) {
        return legalFact -> legalFact.getLegalFactsId().getCategory() != LegalFactCategoryV20Dto.PEC_RECEIPT;
    }

    @NotNull
    private static LegalFactInfo getLegalFactInfo(LegalFactListElementV20Dto item, LegalFactDownloadMetadataWithContentTypeResponseDto legalFact) {
        if (legalFact.getRetryAfter() != null && legalFact.getRetryAfter().intValue() != 0) {
            log.debug("Found legal fact with retry after {}", legalFact.getRetryAfter());
            throw new RaddGenericException(RETRY_AFTER, legalFact.getRetryAfter());
        }
        log.debug("URL : {}", legalFact.getUrl());
        return createLegalFactInfo(item, legalFact);
    }

    @NotNull
    private static LegalFactInfo createLegalFactInfo(LegalFactListElementV20Dto item, LegalFactDownloadMetadataWithContentTypeResponseDto legalFact) {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        if (CONTENT_TYPE_ZIP.equals(legalFact.getContentType())) {
            legalFactInfo.setKey(removeSafeStoragePrefix(item.getLegalFactsId().getKey()));
        } else {
            legalFactInfo.setKey(item.getLegalFactsId().getKey());
        }
        legalFactInfo.setUrl(legalFact.getUrl());
        legalFactInfo.setContentType(legalFact.getContentType());
        legalFactInfo.setCategory(item.getLegalFactsId().getCategory());
        return legalFactInfo;
    }

    @NotNull
    private static String removeSafeStoragePrefix(String legalFactUrl) {
        if (StringUtils.hasText(legalFactUrl) && legalFactUrl.contains(SAFESTORAGE_PREFIX)) {
            legalFactUrl = legalFactUrl.replace(SAFESTORAGE_PREFIX, "");
        }
        return legalFactUrl;
    }

    @NotNull
    private Mono<List<LegalFactInfo>> updateZipAttachments(TransactionData transaction, List<LegalFactInfo> legalFactInfoList) {
        Map<String, String> zipAttachments = legalFactInfoList.stream()
                .filter(legalFactInfo -> CONTENT_TYPE_ZIP.equals(legalFactInfo.getContentType()))
                .collect(Collectors.toMap(LegalFactInfo::getKey, LegalFactInfo::getUrl));
        transaction.setZipAttachments(zipAttachments);
        return raddTransactionDAO.updateZipAttachments(transactionDataMapper.toEntity(transaction.getUid(), transaction), zipAttachments)
                .thenReturn(legalFactInfoList);
    }

    @NotNull
    private static DownloadUrl getDownloadUrl(String url, String documentType) {
        DownloadUrl downloadUrl = new DownloadUrl();
        downloadUrl.setUrl(url);
        downloadUrl.setNeedAuthentication(false);
        downloadUrl.setDocumentType(documentType);
        return downloadUrl;
    }

    private Flux<DownloadUrl> getUrlDoc(TransactionData transaction, SentNotificationV25Dto sentDTO) {
        return Flux.fromStream(sentDTO.getDocuments().stream())
                .flatMap(doc -> this.pnDeliveryClient.getPresignedUrlDocument(transaction.getIun(), doc.getDocIdx(), transaction.getEnsureRecipientId())
                        .map(notificationMetadata -> new NotificationAttachment(DOCUMENT, notificationMetadata))
                        .mapNotNull(ActService::getNotificationAttachmentUrl))
                .map(url -> getDownloadUrl(url, DocumentTypeEnum.DOCUMENT.name()));
    }

    @Nullable
    private static String getNotificationAttachmentUrl(NotificationAttachment notificationAttachment) {
        if (notificationAttachment.getNotificationMetadata().getRetryAfter() != null && notificationAttachment.getNotificationMetadata().getRetryAfter() != 0) {
            logFatalOrError(notificationAttachment);
            throw new RaddGenericException(RETRY_AFTER, notificationAttachment.getNotificationMetadata().getRetryAfter());
        }
        return notificationAttachment.getNotificationMetadata().getUrl();
    }

    private static void logFatalOrError(NotificationAttachment notificationAttachment) {
        if (F24.equals(notificationAttachment.getType())) {
            log.error("Found document/attachment with retry after {}", notificationAttachment.getNotificationMetadata().getRetryAfter());
        } else {
            log.fatal("Found document/attachment with retry after {}", notificationAttachment.getNotificationMetadata().getRetryAfter());
        }
    }

    private Flux<DownloadUrl> getUrlsAttachments(TransactionData transactionData, SentNotificationV25Dto sentDTO) {
        if (sentDTO.getRecipients().isEmpty())
            return Flux.empty();
        return Flux.fromStream(sentDTO.getRecipients().stream())
                .filter(recipient -> recipient.getInternalId().equalsIgnoreCase(transactionData.getEnsureRecipientId()))
                .filter(recipient -> recipient.getPayments() != null)
                .doOnError(e -> log.error(e.getMessage()))
                .flatMap(notificationRecipientV21Dto -> Flux.concat
                        (Flux.fromStream(notificationRecipientV21Dto.getPayments().stream())
                                        .index()
                                        .flatMap(item -> getPagoPAAttachmentDownloadMetadataResponse(transactionData, item.getT2(), Math.toIntExact(item.getT1()))),
                                Flux.fromStream(notificationRecipientV21Dto.getPayments().stream())
                                        .index()
                                        .flatMap(item -> getF24AttachmentDownloadMetadataResponseDto(transactionData, item.getT2(), Math.toIntExact(item.getT1())))))
                .mapNotNull(ActService::getNotificationAttachmentUrl)
                .map(url -> getDownloadUrl(url, DocumentTypeEnum.ATTACHMENT.name()));
    }

    private Mono<NotificationAttachment> getPagoPAAttachmentDownloadMetadataResponse(TransactionData transactionData, NotificationPaymentItemDto item, Integer attachmentIdx) {
        if (item.getPagoPa() != null && item.getPagoPa().getAttachment() != null) {
            return pnDeliveryClient.getPresignedUrlPaymentDocument(transactionData.getIun(), "PAGOPA", transactionData.getEnsureRecipientId(), attachmentIdx)
                    .map(notificationMetadata -> new NotificationAttachment(PAGOPA, notificationMetadata));
        }
        return Mono.empty();
    }

    private Mono<NotificationAttachment> getF24AttachmentDownloadMetadataResponseDto(TransactionData transactionData, NotificationPaymentItemDto item, Integer attachmentIdx) {
        if (item.getF24() != null) {
            return pnDeliveryClient.getPresignedUrlPaymentDocument(transactionData.getIun(), "F24", transactionData.getEnsureRecipientId(), attachmentIdx)
                    .map(notificationAttachmentDownloadMetadataResponseDto -> new NotificationAttachment(F24, notificationAttachmentDownloadMetadataResponseDto));
        }
        return Mono.empty();
    }

    private Mono<ResponseCheckAarDtoDto> controlAndCheckAar(String recipientType, String recipientTaxId, String
            qrCode) {
        return this.pnDeliveryClient.getCheckAar(recipientType, recipientTaxId, qrCode)
                .map(response -> {
                    if (response == null || Strings.isBlank(response.getIun())) {
                        throw new RaddGenericException(IUN_NOT_FOUND);
                    }
                    return response;
                });
    }

    private Mono<TransactionData> validateAndSettingsData(String uid, ActStartTransactionRequest request, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId) {
        return validateOperationId(request.getOperationId())
                .flatMap(validated -> validateRecTaxIdRecTypeQrCodeIun(request.getRecipientTaxId(), request.getRecipientType().getValue(), request.getQrCode(), request.getIun()))
                .doOnNext(validated -> log.trace("START ACT TRANSACTION TICK {}", new Date().getTime()))
                .map(validated -> transactionDataMapper.toTransaction(uid, request, xPagopaPnCxType, xPagopaPnCxId));
    }

    private Mono<CompleteTransactionRequest> validateCompleteTransactionRequest(CompleteTransactionRequest req) {
        return validateOperationId(req.getOperationId())
                .map(validated -> req);
    }

    private static Mono<Boolean> validateOperationId(String operationId) {
        if (!StringUtils.hasText(operationId)) {
            return Mono.error(new PnInvalidInputException("Operation id non valorizzato"));
        }
        return Mono.just(true);
    }

    private Mono<Boolean> validateRecTaxIdRecTypeQrCodeIun(String recipientTaxId, String recipientType, String
            qrCode, String iun) {
        if (Strings.isBlank(recipientTaxId)) {
            return Mono.error(new PnInvalidInputException("Codice fiscale non valorizzato"));
        }
        if (!Utils.checkPersonType(recipientType)) {
            return Mono.error(new PnInvalidInputException("Recipient Type non valorizzato correttamente"));
        }
        if (Strings.isBlank(iun) && Strings.isBlank(qrCode)) {
            return Mono.error(new PnInvalidInputException("Né IUN nè QrCode valorizzati"));
        }
        if (!Strings.isBlank(iun) && !Strings.isBlank(qrCode)) {
            return Mono.error(new PnInvalidInputException("IUN e QrCode valorizzati contemporaneamente"));
        }
        return Mono.just(true);
    }

    private Mono<SentNotificationV25Dto> hasDocumentsAvailable(String iun) {
        return this.pnDeliveryClient.getNotifications(iun)
                .flatMap(response -> {
                    if (response.getDocumentsAvailable() != null && Boolean.FALSE.equals(response.getDocumentsAvailable())) {
                        return Mono.error(new RaddGenericException(DOCUMENT_UNAVAILABLE));
                    }
                    return Mono.just(response);
                });
    }

    private Mono<String> hasNotificationsCancelled(String iun) {
        return this.pnDeliveryPushClient.getNotificationHistory(iun)
                .flatMap(response -> {
                    if (response.getNotificationStatus() == NotificationStatusV26Dto.CANCELLED) {
                        return Mono.error(new RaddGenericException(NOTIFICATION_CANCELLED));
                    }
                    return Mono.just(iun);
                });
    }

    private Mono<TransactionData> createRaddTransaction(String uid, TransactionData transactionData) {
        return Mono.just(transactionDataMapper.toEntity(uid, transactionData))
                .flatMap(raddTransaction -> raddTransactionDAO.createRaddTransaction(raddTransaction, null))
                .thenReturn(transactionData);
    }

    private Mono<RaddTransactionEntity> getAndCheckStatusTransaction(String operationId, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId) {
        return raddTransactionDAO.getTransaction(String.valueOf(xPagopaPnCxType), xPagopaPnCxId, operationId, OperationTypeEnum.ACT)
                .doOnNext(raddTransaction -> log.debug("[{}] Check status entity : {}", operationId, raddTransaction.getStatus()))
                .doOnNext(this::checkTransactionStatus);
    }

}
