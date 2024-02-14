package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.dto.LegalFactCategoryDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.dto.LegalFactDownloadMetadataWithContentTypeResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.dto.LegalFactListElementDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.dto.NotificationStatusDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.*;
import it.pagopa.pn.radd.pojo.LegalFactInfo;
import it.pagopa.pn.radd.pojo.RaddTransactionStatusEnum;
import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.utils.DateUtils;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import it.pagopa.pn.radd.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.*;
import static it.pagopa.pn.radd.utils.Const.*;
import static it.pagopa.pn.radd.utils.Utils.getDocumentDownloadUrl;
import static org.springframework.util.StringUtils.*;

@Service
@Slf4j
public class ActService extends BaseService {
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;
    private final TransactionDataMapper transactionDataMapper;
    private final PnRaddFsuConfig pnRaddFsuConfig;

    public ActService(RaddTransactionDAO raddTransactionDAO, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient, PnDataVaultClient pnDataVaultClient, PnSafeStorageClient safeStorageClient, TransactionDataMapper transactionDataMapper, PnRaddFsuConfig pnRaddFsuConfig) {
        super(pnDataVaultClient, raddTransactionDAO, safeStorageClient);
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.transactionDataMapper = transactionDataMapper;
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }

    public Mono<ActInquiryResponse> actInquiry(String uid, String xPagopaPnCxId, CxTypeAuthFleet xPagopaPnCxType, String recipientTaxId, String recipientType, String qrCode, String iun) {
        log.info("Start act inquiry - uid={}, cxType={}, cxId={}", uid, xPagopaPnCxType, xPagopaPnCxId);
        // check if iun exists
        return validateInputActInquiry(recipientTaxId, recipientType, qrCode, iun)
                .doOnNext(isValid -> log.trace("ACT INQUIRY TICK {}", new Date().getTime()))
                .flatMap(isValid -> getEnsureFiscalCode(recipientTaxId, recipientType))
                .flatMap(recCode -> checkQrCodeOrIun(recipientType, qrCode, iun, recCode))
                .flatMap(this::hasDocumentsAvailable)
                .flatMap(this::hasNotificationsCancelled)
                .doOnNext(nothing -> log.trace("ACT INQUIRY TOCK {}", new Date().getTime()))
                .map(item -> ActInquiryResponseMapper.fromResult())
                .onErrorResume(
                        RaddGenericException.class,
                        ex -> {
                            log.error(ex.getMessage());
                            return Mono.just(ActInquiryResponseMapper.fromException(ex));
                        });
    }

    @NotNull
    private Mono<String> checkQrCodeOrIun(String recipientType, String qrCode, String iun, String recCode) {
        if (hasText(qrCode)) {
            return checkQrCode(recipientType, qrCode, recCode);
        } else {
            return checkIun(iun, recCode);
        }
    }

    @NotNull
    private Mono<String> checkIun(String iun, String recCode) {
        return checkIunIsAlreadyExistsInCompleted(iun)
                .flatMap(counter -> checkIunAndInternalId(iun, recCode)
                        .thenReturn(iun));
    }

    @NotNull
    private Mono<String> checkQrCode(String recipientType, String qrCode, String recCode) {
        return controlAndCheckAar(recipientType, recCode, qrCode)
                .flatMap(responseCheckAarDtoDto -> checkIunIsAlreadyExistsInCompleted(responseCheckAarDtoDto.getIun())
                        .thenReturn(responseCheckAarDtoDto.getIun()));
    }

    private Mono<Void> checkIunAndInternalId(String iun, String internalId) {
        return pnDeliveryClient.checkIunAndInternalId(iun, internalId);
    }

    private Mono<Integer> checkIunIsAlreadyExistsInCompleted(String iun) {
        return this.raddTransactionDAO.countFromIunAndStatus(iun)
                .filter(counter -> counter == 0)
                .switchIfEmpty(Mono.error(new IunAlreadyExistsException()))
                .doOnError(err -> log.error(err.getMessage()));
    }

    public Mono<StartTransactionResponse> startTransaction(String uid, String xPagopaPnCxId, CxTypeAuthFleet xPagopaPnCxType, ActStartTransactionRequest request) {
        log.info("Start ACT startTransaction - uid={} - cxId={} - cxType={} - operationId={}", uid, xPagopaPnCxId, xPagopaPnCxType, request.getOperationId());
        return validateAndSettingsData(uid, request, xPagopaPnCxType, xPagopaPnCxId)
                .flatMap(this::getEnsureRecipientAndDelegate)
                .flatMap(transactionData -> checkQrCodeOrIun(request.getRecipientType().getValue(), request.getQrCode(), request.getIun(), transactionData.getEnsureRecipientId())
                        .map(s -> setIun(transactionData, s)))
                .flatMap(transactionData -> hasNotificationsCancelled(transactionData.getIun())
                        .thenReturn(transactionData))
                .flatMap(transactionData -> this.createRaddTransaction(uid, transactionData))
                .flatMap(this::verifyCheckSum)
                .zipWhen(transaction -> this.pnDeliveryClient.getNotifications(transaction.getIun()))
                .zipWhen(transactionAndSentNotification -> retrieveDocumentsAndAttachments(request, transactionAndSentNotification),
                        (tupla, response) -> Tuples.of(tupla.getT1(), response))
                .doOnError(e -> log.error(e.getMessage()))
                .zipWhen(transactionAndResponse -> {
                    log.debug("Update file metadata");
                    TransactionData transaction = transactionAndResponse.getT1();
                    return this.updateFileMetadata(transaction);
                }, (in, out) -> in.getT2())
                .map(response -> {
                    log.trace("START ACT TRANSACTION TOCK {}", new Date().getTime());
                    log.debug("Ended startTransaction");
                    return response;
                })
                .onErrorResume(PnRaddException.class, ex -> {
                    log.error(ENDED_ACT_START_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                    return this.settingErrorReason(ex, request.getOperationId(), OperationTypeEnum.ACT, xPagopaPnCxType, xPagopaPnCxId)
                            .flatMap(entity -> Mono.error(ex));
                })
                .onErrorResume(IunAlreadyExistsException.class, ex -> {
                    log.error(ENDED_ACT_START_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                    return Mono.just(StartTransactionResponseMapper.fromException(ex));
                })
                .onErrorResume(TransactionAlreadyExistsException.class, ex -> {
                    log.error(ENDED_ACT_START_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                    return Mono.just(StartTransactionResponseMapper.fromException(ex));
                })
                .onErrorResume(RaddGenericException.class, ex -> {
                    log.error(ENDED_ACT_START_TRANSACTION_WITH_ERROR, ex.getMessage(), ex);
                    return this.settingErrorReason(ex, request.getOperationId(), OperationTypeEnum.ACT, xPagopaPnCxType, xPagopaPnCxId)
                            .flatMap(entity -> Mono.just(StartTransactionResponseMapper.fromException(ex)));
                });

    }

    @NotNull
    private Mono<StartTransactionResponse> retrieveDocumentsAndAttachments(ActStartTransactionRequest request, Tuple2<TransactionData, SentNotificationV23Dto> transactionAndSentNotification) {
        log.debug("Retrieving document and attachments");
        Flux<DownloadUrl> urlDocuments = getUrlDoc(transactionAndSentNotification.getT1(), transactionAndSentNotification.getT2());
        Flux<DownloadUrl> urlAttachments = getUrlsAttachments(transactionAndSentNotification.getT1(), transactionAndSentNotification.getT2());
        Flux<DownloadUrl> urlLegalFacts = legalFact(transactionAndSentNotification.getT1());
        return ParallelFlux.from(urlDocuments, urlAttachments, urlLegalFacts)
                .sequential()
                .collectList()
                .map(resultList -> StartTransactionResponseMapper.fromResult(resultList, OperationTypeEnum.ACT.name(), request.getOperationId(), pnRaddFsuConfig.getApplicationBasepath()));
    }

    @NotNull
    private static TransactionData setIun(TransactionData transactionData, String s) {
        transactionData.setIun(s);
        return transactionData;
    }

    public Mono<CompleteTransactionResponse> completeTransaction(String uid, CompleteTransactionRequest
            completeTransactionRequest, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId) {
        log.info("Start ACT complete transaction - uid={}, cxType={}, cxId={}, operationId={}", uid, xPagopaPnCxType, xPagopaPnCxId, completeTransactionRequest.getOperationId());
        return this.validateCompleteRequest(completeTransactionRequest)
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
                .doOnNext(entity -> log.debug("[uid={} - operationId={}]  New status of transaction entity is {}", entity.getUid(), entity.getOperationId(), entity.getStatus()))
                .doOnNext(entity -> log.debug("[uid={} - operationId={}] End ACT Complete transaction", entity.getUid(), entity.getOperationId()))
                .map(entity -> CompleteTransactionResponseMapper.fromResult())
                .doOnError(PnRaddException.class, ex -> log.debug("End ACT Complete transaction with error {}", ex.getMessage(), ex))
                .onErrorResume(PnRaddException.class, ex ->
                        this.settingErrorReason(ex, completeTransactionRequest.getOperationId(), OperationTypeEnum.ACT, xPagopaPnCxType, xPagopaPnCxId)
                                .flatMap(entity -> Mono.error(ex))
                )
                .onErrorResume(RaddGenericException.class, ex ->
                        Mono.just(CompleteTransactionResponseMapper.fromException(ex))
                );
    }

    public Mono<AbortTransactionResponse> abortTransaction(String uid, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, AbortTransactionRequest req) {
        if (req == null || !StringUtils.hasText(req.getOperationId())
                || !StringUtils.hasText(req.getReason())) {
            log.error("Missing input parameters");
            return Mono.error(new PnInvalidInputException("Alcuni parametri come operazione id o data di operazione non sono valorizzate"));
        }
        log.info("Start ACT abort transaction - uid={}, cxType={}, cxId={}, operationId={}", uid, xPagopaPnCxType, xPagopaPnCxId, req.getOperationId());
        return raddTransactionDAO.getTransaction(String.valueOf(xPagopaPnCxType), xPagopaPnCxId, req.getOperationId(), OperationTypeEnum.ACT)
                .doOnNext(this::checkTransactionStatus)
                .map(raddEntity -> {
                    raddEntity.setUid(uid);
                    raddEntity.setErrorReason(req.getReason());
                    raddEntity.setOperationEndDate(DateUtils.formatDate(req.getOperationDate()));
                    return raddEntity;
                })
                .flatMap(entity -> raddTransactionDAO.updateStatus(entity, RaddTransactionStatusEnum.ABORTED))
                .doOnNext(raddTransaction -> log.debug("[uid={} - operationId={}] End ACT abortTransaction with entity status {}", raddTransaction.getUid(), raddTransaction.getOperationId(), raddTransaction.getStatus()))
                .map(result -> AbortTransactionResponseMapper.fromResult())
                .doOnError(RaddGenericException.class, ex -> log.error("End ACT abort transaction with error : {}", ex.getMessage(), ex))
                .onErrorResume(RaddGenericException.class, ex ->
                        Mono.just(AbortTransactionResponseMapper.fromException(ex))
                );
    }

    private Flux<DownloadUrl> legalFact(TransactionData transaction) {
        return pnDeliveryPushClient.getNotificationLegalFacts(transaction.getEnsureRecipientId(), transaction.getIun())
                .filter(filterLegalFacts(transaction))
                .flatMap(item ->
                        pnDeliveryPushClient.getLegalFact(transaction.getEnsureRecipientId(),
                                        transaction.getIun(),
                                        item.getLegalFactsId().getCategory(),
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
            return getDownloadUrl(legalFactInfo.getUrl(), false);
        } else {
            return getDocumentDownloadUrl(pnRaddFsuConfig.getApplicationBasepath(),
                    transaction.getOperationType().name(),
                    transaction.getOperationId(),
                    legalFactInfo.getKey());
        }
    }

    @NotNull
    private static Predicate<LegalFactListElementDto> filterLegalFacts(TransactionData transaction) {
        return legalFact -> StringUtils.hasText(legalFact.getTaxId())
                && legalFact.getTaxId().equalsIgnoreCase(transaction.getRecipientId())
                && legalFact.getLegalFactsId().getCategory() != LegalFactCategoryDto.PEC_RECEIPT;
    }

    @NotNull
    private static LegalFactInfo getLegalFactInfo(LegalFactListElementDto item, LegalFactDownloadMetadataWithContentTypeResponseDto legalFact) {
        if (legalFact.getRetryAfter() != null && legalFact.getRetryAfter().intValue() != 0) {
            log.debug("Found legal fact with retry after {}", legalFact.getRetryAfter());
            throw new RaddGenericException(RETRY_AFTER, legalFact.getRetryAfter());
        }
        log.debug("URL : {}", legalFact.getUrl());
        return createLegalFactInfo(item, legalFact);
    }

    @NotNull
    private static LegalFactInfo createLegalFactInfo(LegalFactListElementDto item, LegalFactDownloadMetadataWithContentTypeResponseDto legalFact) {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setKey(item.getLegalFactsId().getKey());
        legalFactInfo.setUrl(legalFact.getUrl());
        legalFactInfo.setContentType(legalFact.getContentType());
        return legalFactInfo;
    }

    @NotNull
    private static String removeSafeStoragePrefix(LegalFactDownloadMetadataWithContentTypeResponseDto legalFact) {
        String legalFactUrl = legalFact.getUrl();
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
    private static DownloadUrl getDownloadUrl(String url, boolean needAuthentication) {
        DownloadUrl downloadUrl = new DownloadUrl();
        downloadUrl.setUrl(url);
        downloadUrl.setNeedAuthentication(needAuthentication);
        return downloadUrl;
    }

    private Flux<DownloadUrl> getUrlDoc(TransactionData transaction, SentNotificationV23Dto sentDTO) {
        return Flux.fromStream(sentDTO.getDocuments().stream())
                .flatMap(doc -> this.pnDeliveryClient.getPresignedUrlDocument(transaction.getIun(), doc.getDocIdx(), transaction.getEnsureRecipientId())
                        .mapNotNull(ActService::getNotificationAttachmentUrl))
                .map(url -> getDownloadUrl(url, false));
    }

    @Nullable
    private static String getNotificationAttachmentUrl(NotificationAttachmentDownloadMetadataResponseDto notificationAttachment) {
        if (notificationAttachment.getRetryAfter() != null && notificationAttachment.getRetryAfter() != 0) {
            log.debug("Found attachment with retry after {}", notificationAttachment.getRetryAfter());
            throw new RaddGenericException(DOCUMENT_UNAVAILABLE_RETRY_AFTER, notificationAttachment.getRetryAfter());
        }
        return notificationAttachment.getUrl();
    }

    private Flux<DownloadUrl> getUrlsAttachments(TransactionData transactionData, SentNotificationV23Dto sentDTO) {
        if (sentDTO.getRecipients().isEmpty())
            return Flux.empty();
        return Flux.fromStream(sentDTO.getRecipients().stream())
                .filter(recipient -> recipient.getInternalId().equalsIgnoreCase(transactionData.getRecipientId()))
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
                .map(url -> getDownloadUrl(url, false))
                .doOnError(e -> log.error(e.getMessage()));
    }

    private Mono<NotificationAttachmentDownloadMetadataResponseDto> getPagoPAAttachmentDownloadMetadataResponse(TransactionData transactionData, NotificationPaymentItemDto item, Integer attachmentIdx) {
        if (item.getPagoPa() != null && item.getPagoPa().getAttachment() != null) {
            return pnDeliveryClient.getPresignedUrlPaymentDocument(transactionData.getIun(), "PAGOPA", transactionData.getEnsureRecipientId(), attachmentIdx);
        }
        return Mono.empty();
    }

    private Mono<NotificationAttachmentDownloadMetadataResponseDto> getF24AttachmentDownloadMetadataResponseDto(TransactionData transactionData, NotificationPaymentItemDto item, Integer attachmentIdx) {
        if (item.getF24() != null) {
            return pnDeliveryClient.getPresignedUrlPaymentDocument(transactionData.getIun(), "F24", transactionData.getEnsureRecipientId(), attachmentIdx);
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
        if (Strings.isBlank(request.getOperationId())) {
            return Mono.error(new PnInvalidInputException("Id operazione non valorizzato"));
        }
        if (Strings.isBlank(request.getRecipientTaxId())) {
            return Mono.error(new PnInvalidInputException("Codice fiscale non valorizzato"));
        }
        if (!Utils.checkPersonType(request.getRecipientType().getValue())) {
            return Mono.error(new PnInvalidInputException("Recipient Type non valorizzato correttamente"));
        }
        if (Strings.isBlank(request.getIun()) && Strings.isBlank(request.getQrCode())) {
            return Mono.error(new PnInvalidInputException("Né IUN nè QrCode valorizzati"));
        }
        if (!Strings.isBlank(request.getIun()) && !Strings.isBlank(request.getQrCode())) {
            return Mono.error(new PnInvalidInputException("IUN e QrCode valorizzati contemporaneamente"));
        }
        log.trace("START ACT TRANSACTION TICK {}", new Date().getTime());
        return Mono.just(this.transactionDataMapper.toTransaction(uid, request, xPagopaPnCxType, xPagopaPnCxId));
    }

    private Mono<CompleteTransactionRequest> validateCompleteRequest(CompleteTransactionRequest req) {
        if (!StringUtils.hasText(req.getOperationId())) {
            return Mono.error(new PnInvalidInputException("Operation id non valorizzato"));
        }
        return Mono.just(req);
    }

    private Mono<Boolean> validateInputActInquiry(String recipientTaxId, String recipientType, String
            qrCode, String iun) {
        if (!StringUtils.hasText(recipientTaxId) || !Utils.checkPersonType(recipientType)
                || (!StringUtils.hasText(qrCode) && !StringUtils.hasText(iun)) || (StringUtils.hasText(qrCode) && StringUtils.hasText(iun))) {
            log.error("Missing input parameters");
            return Mono.error(new PnInvalidInputException("Codice fiscale, tipo utente o codice fiscale non valorizzati correttamente"));
        }
        return Mono.just(true);
    }

    private Mono<String> hasDocumentsAvailable(String iun) {
        return this.pnDeliveryClient.getNotifications(iun)
                .flatMap(response -> {
                    if (response.getDocumentsAvailable() != null && !response.getDocumentsAvailable()) {
                        return Mono.error(new RaddGenericException(DOCUMENT_UNAVAILABLE));
                    }
                    return Mono.just(iun);
                });
    }

    private Mono<String> hasNotificationsCancelled(String iun) {
        return this.pnDeliveryPushClient.getNotificationHistory(iun)
                .flatMap(response -> {
                    if (response.getNotificationStatus() == NotificationStatusDto.CANCELLED) {
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
