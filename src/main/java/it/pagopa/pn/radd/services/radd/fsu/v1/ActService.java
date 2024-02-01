package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactCategoryDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.NotificationStatusDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.*;
import it.pagopa.pn.radd.pojo.RaddTransactionStatusEnum;
import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.utils.DateUtils;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import it.pagopa.pn.radd.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.util.function.Tuples;

import java.util.Date;
import java.util.List;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.*;
import static org.springframework.util.StringUtils.*;

@Service
@Slf4j
public class ActService extends BaseService {
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;
    private final PnDeliveryPushInternalClient pnDeliveryPushInternalClient;
    private final TransactionDataMapper transactionDataMapper;

    public ActService(RaddTransactionDAO raddTransactionDAO, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient, PnDataVaultClient pnDataVaultClient, PnSafeStorageClient safeStorageClient, PnDeliveryPushInternalClient pnDeliveryPushInternalClient, TransactionDataMapper transactionDataMapper) {
        super(pnDataVaultClient, raddTransactionDAO, safeStorageClient);
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.pnDeliveryPushInternalClient = pnDeliveryPushInternalClient;
        this.transactionDataMapper = transactionDataMapper;
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
        return pnDeliveryClient.checkIunAndInternalId(iun, internalId, null);
    }

    private Mono<Integer> checkIunIsAlreadyExistsInCompleted(String iun) {
        return this.raddTransactionDAO.countFromIunAndStatus(iun)
                .filter(counter -> counter == 0)
                .switchIfEmpty(Mono.error(new IunAlreadyExistsException()))
                .doOnError(err -> log.error(err.getMessage()));
    }

    public Mono<StartTransactionResponse> startTransaction(String uid, ActStartTransactionRequest request) {
        log.info("Start ACT startTransaction - uid={} - operationId={}", uid, request.getOperationId());
        return validateAndSettingsData(uid, request)
                .flatMap(transactionData -> checkIunIsAlreadyExistsInCompleted(request.getIun()).thenReturn(transactionData))
                .flatMap(this::getEnsureRecipientAndDelegate)
                .flatMap(tmp ->
                        controlAndCheckAar(tmp.getRecipientType(), tmp.getEnsureRecipientId(), tmp.getQrCode())
                                .map(ResponseCheckAarDtoDto::getIun)
                                .doOnNext(iun -> log.info("Iun finded - {}", iun))
                                .map(iun -> {
                                    tmp.setIun(iun);
                                    return tmp;
                                })
                )
                .flatMap(transactionData -> this.createRaddTransaction(uid, transactionData))
                .flatMap(this::verifyCheckSum)
                .zipWhen(transaction -> this.pnDeliveryClient.getNotifications(transaction.getIun()))
                .zipWhen(transactionAndSentNotification -> {
                    log.debug("Retrieving document and attachments");
                    Flux<String> urlDocuments = getUrlDoc(transactionAndSentNotification.getT1(), transactionAndSentNotification.getT2());
                    Flux<String> urlAttachments = getUrlsAttachments(transactionAndSentNotification.getT1(), transactionAndSentNotification.getT2());
                    ParallelFlux<String> urlLegalFacts = legalFact(transactionAndSentNotification.getT1());
                    return ParallelFlux.from(urlDocuments, urlAttachments, urlLegalFacts)
                            .sequential()
                            .collectList()
                            .map(StartTransactionResponseMapper::fromResult);
                }, (tupla, response) -> Tuples.of(tupla.getT1(), response))
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
                    log.error("Ended ACT startTransaction with error {}", ex.getMessage(), ex);
                    return this.settingErrorReason(ex, request.getOperationId(), OperationTypeEnum.ACT)
                            .flatMap(entity -> Mono.error(ex));
                })
                .onErrorResume(QrCodeAlreadyExistsException.class, ex -> {
                    log.error("Ended ACT startTransaction with error {}", ex.getMessage(), ex);
                    return Mono.just(StartTransactionResponseMapper.fromException(ex));
                })
                .onErrorResume(TransactionAlreadyExistsException.class, ex -> {
                    log.error("Ended ACT startTransaction with error {}", ex.getMessage(), ex);
                    return Mono.just(StartTransactionResponseMapper.fromException(ex));
                })
                .onErrorResume(RaddGenericException.class, ex -> {
                    log.error("Ended ACT startTransaction with error {}", ex.getMessage(), ex);
                    return this.settingErrorReason(ex, request.getOperationId(), OperationTypeEnum.ACT)
                            .flatMap(entity -> Mono.just(StartTransactionResponseMapper.fromException(ex)));
                });

    }

    public Mono<CompleteTransactionResponse> completeTransaction(String uid, CompleteTransactionRequest
            completeTransactionRequest) {
        log.info("Start ACT CompleteTransaction - uid={} - operationId={}", uid, completeTransactionRequest.getOperationId());
        return this.validateCompleteRequest(completeTransactionRequest)
                .zipWhen(req -> getAndCheckStatusTransaction(req.getOperationId()))
                .zipWhen(reqAndEntity -> this.pnDeliveryPushClient.notifyNotificationViewed(reqAndEntity.getT2(), reqAndEntity.getT1().getOperationDate()), (reqAndEntity, response) -> reqAndEntity)
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
                        this.settingErrorReason(ex, completeTransactionRequest.getOperationId(), OperationTypeEnum.ACT)
                                .flatMap(entity -> Mono.error(ex))
                )
                .onErrorResume(RaddGenericException.class, ex ->
                        Mono.just(CompleteTransactionResponseMapper.fromException(ex))
                );
    }

    public Mono<AbortTransactionResponse> abortTransaction(String uid, AbortTransactionRequest req) {
        if (req == null || !StringUtils.hasText(req.getOperationId())
                || !StringUtils.hasText(req.getReason())) {
            log.error("Missing input parameters");
            return Mono.error(new PnInvalidInputException("Alcuni parametri come operazione id o data di operazione non sono valorizzate"));
        }
        log.info("Start ACT abort transaction - uid={} - operationId={}", uid, req.getOperationId());
        // TODO passare cxType e cxId in seguito all'aggiornamento dell'open api
        return raddTransactionDAO.getTransaction("", "", req.getOperationId(), OperationTypeEnum.ACT)
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

    private ParallelFlux<String> legalFact(TransactionData transaction) {
        return pnDeliveryPushInternalClient.getNotificationLegalFacts(transaction.getEnsureRecipientId(), transaction.getIun())
                .parallel()
                .filter(legalFact -> ((isEmpty(legalFact.getTaxId()) || (StringUtils.hasText(legalFact.getTaxId()) && legalFact.getTaxId().equalsIgnoreCase(transaction.getRecipientId()))) && legalFact.getLegalFactsId().getCategory() != LegalFactCategoryDto.PEC_RECEIPT))
                .flatMap(item ->
                        pnDeliveryPushInternalClient.getLegalFact(transaction.getEnsureRecipientId(), transaction.getIun(), item.getLegalFactsId().getCategory(), item.getLegalFactsId().getKey())
                                .mapNotNull(legalFact -> {
                                    if (legalFact.getRetryAfter() != null && legalFact.getRetryAfter().intValue() != 0) {
                                        log.debug("Finded legal fact with retry after {}", legalFact.getRetryAfter());
                                        throw new RaddGenericException(RETRY_AFTER, legalFact.getRetryAfter());
                                    }
                                    log.debug("URL : {}", legalFact.getUrl());
                                    return legalFact.getUrl();
                                })
                );
    }

    private Flux<String> getUrlDoc(TransactionData transaction, SentNotificationV21Dto sentDTO) {
        return Flux.fromStream(sentDTO.getDocuments().stream())
                .flatMap(doc -> this.pnDeliveryClient.getPresignedUrlDocument(transaction.getIun(), doc.getDocIdx(), transaction.getEnsureRecipientId())
                        .mapNotNull(NotificationAttachmentDownloadMetadataResponseDto::getUrl));
    }

    private Flux<String> getUrlsAttachments(TransactionData transactionData, SentNotificationV21Dto sentDTO) {
        if (sentDTO.getRecipients().isEmpty())
            return Flux.empty();
        return Flux.fromStream(sentDTO.getRecipients().stream())
                .filter(item -> item.getTaxId().equalsIgnoreCase(transactionData.getRecipientId()) && item.getPayments() != null && checkPayments(item.getPayments()))
                .flatMap(item -> pnDeliveryClient.getPresignedUrlPaymentDocument(transactionData.getIun(), "PAGOPA", transactionData.getEnsureRecipientId()))
                .mapNotNull(NotificationAttachmentDownloadMetadataResponseDto::getUrl);
    }

    private boolean checkPayments(List<NotificationPaymentItemDto> notificationPaymentItemDtos) {
        long count = notificationPaymentItemDtos.stream().filter(ActService::checkAttachments).count();
        return count != 0;
    }

    private static boolean checkAttachments(NotificationPaymentItemDto paymentItemDto) {
        return paymentItemDto.getPagoPa() != null && paymentItemDto.getPagoPa().getAttachment() != null || paymentItemDto.getF24() != null;
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

    private Mono<TransactionData> validateAndSettingsData(String uid, ActStartTransactionRequest request) {
        if (Strings.isBlank(request.getOperationId())) {
            return Mono.error(new PnInvalidInputException("Id operazione non valorizzato"));
        }
        if (Strings.isBlank(request.getRecipientTaxId())) {
            return Mono.error(new PnInvalidInputException("Codice fiscale non valorizzato"));
        }
        if (Strings.isBlank(request.getQrCode())) {
            return Mono.error(new PnInvalidInputException("QRCode non valorizzato"));
        }
        if (request.getRecipientType() == null || !Utils.checkPersonType(request.getRecipientType().getValue())) {
            return Mono.error(new PnInvalidInputException("Recipient Type non valorizzato correttamente"));
        }
        log.trace("START ACT TRANSACTION TICK {}", new Date().getTime());
        return Mono.just(this.transactionDataMapper.toTransaction(uid, request));
    }

    private Mono<CompleteTransactionRequest> validateCompleteRequest(CompleteTransactionRequest req) {
        if (isEmpty(req.getOperationId())) {
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

    private Mono<RaddTransactionEntity> getAndCheckStatusTransaction(String operationId) {
        // TODO passare cxType e cxId in seguito all'aggiornamento dell'open api
        return raddTransactionDAO.getTransaction("", "", operationId, OperationTypeEnum.ACT)
                .doOnNext(raddTransaction -> log.debug("[{}] Check status entity : {}", operationId, raddTransaction.getStatus()))
                .doOnNext(this::checkTransactionStatus);
    }

}
