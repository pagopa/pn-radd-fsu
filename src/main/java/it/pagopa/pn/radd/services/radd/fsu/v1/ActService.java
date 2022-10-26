package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.mapper.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.SentNotificationDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactCategoryDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.*;
import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.DateUtils;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import it.pagopa.pn.radd.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.*;

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

    public Mono<ActInquiryResponse> actInquiry(String uid, String recipientTaxId, String recipientType, String qrCode) {
        // check if iun exists
        return Mono.fromFuture(this.raddTransactionDAO.countFromQrCodeCompleted(qrCode)
                    .thenApply(response -> {
                        if (response > 0) throw new RaddGenericException(ALREADY_COMPLETE_PRINT);
                        return response;
                    })
                )
                .flatMap(counter -> getEnsureFiscalCode(recipientTaxId, recipientType))
                .flatMap(recCode -> controlAndCheckAar(recipientType, recCode, qrCode))
                .map(item -> ActInquiryResponseMapper.fromResult())
                .onErrorResume(RaddGenericException.class, ex -> Mono.just(ActInquiryResponseMapper.fromException(ex)));
    }

    public Mono<StartTransactionResponse> startTransaction(String uid, ActStartTransactionRequest request){
        return validateAndSettingsData(uid, request)
                .zipWhen(this::getEnsureRecipientAndDelegate, (transaction, transationReq) -> transationReq)
                .zipWhen(tmp -> controlAndCheckAar(tmp.getRecipientType(), tmp.getEnsureRecipientId(), tmp.getQrCode())
                        .map(ResponseCheckAarDtoDto::getIun), (transaction, iun) -> {
                                                                transaction.setIun(iun);
                                                                return transaction;
                })
                .zipWhen( transaction -> getCounterTransactions(transaction.getIun(), transaction.getOperationId()), (transaction, counter)-> transaction)
                .zipWhen( transaction -> {
                    log.debug("Ensure recipient : {}", transaction.getEnsureRecipientId());
                    return this.raddTransactionDAO.createRaddTransaction(transactionDataMapper.toEntity(uid, transaction), null);
                }, (transaction, entity) -> transaction )
                .flatMap(this::verifyCheckSum)
                .zipWhen(transaction -> this.pnDeliveryClient.getNotifications(transaction.getIun()))
                .zipWhen(transactionAndSentNotification ->
                        urlDocAndAttachments(transactionAndSentNotification.getT1(), transactionAndSentNotification.getT2())
                                .concatWith(legalFact(transactionAndSentNotification.getT1()))
                                .collectList()
                                .map(StartTransactionResponseMapper::fromResult), (tupla, response) -> Tuples.of(tupla.getT1(), response))
                .zipWhen(transactionAndResponse -> {
                    TransactionData transaction = transactionAndResponse.getT1();
                    return this.updateFileMetadata(transaction);
                }, (in, out) -> in.getT2())
                .onErrorResume(PnRaddException.class, ex ->
                        this.settingErrorReason(ex, request.getOperationId(), OperationTypeEnum.ACT)
                                .flatMap(entity -> Mono.error(ex))
                )
                .onErrorResume(RaddGenericException.class, ex ->
                    this.settingErrorReason(ex, request.getOperationId(), OperationTypeEnum.ACT)
                            .flatMap(entity -> Mono.just(StartTransactionResponseMapper.fromException(ex)))
                );

    }

    public Mono<CompleteTransactionResponse> completeTransaction(String uid, CompleteTransactionRequest completeTransactionRequest) {
        return this.validateCompleteRequest(completeTransactionRequest)
                .zipWhen(req -> this.raddTransactionDAO.getTransaction(req.getOperationId(), OperationTypeEnum.ACT)
                                .map(entity -> {
                                    checkTransactionStatus(entity);
                                    return entity;
                                }))
                .zipWhen(reqAndEntity -> this.pnDeliveryPushClient.notifyNotificationViewed(reqAndEntity.getT2()), (reqAndEntity, response) -> reqAndEntity)
                .zipWhen(reqAndEntity -> {
                    RaddTransactionEntity entity = reqAndEntity.getT2();
                    entity.setOperationEndDate(DateUtils.formatDate(reqAndEntity.getT1().getOperationDate()));
                    entity.setUid(uid);
                    entity.setStatus(Const.COMPLETED);
                    return this.raddTransactionDAO.updateStatus(entity);
                })
                .map(entity -> CompleteTransactionResponseMapper.fromResult())
                .onErrorResume(PnRaddException.class, ex ->
                    this.settingErrorReason(ex, completeTransactionRequest.getOperationId(), OperationTypeEnum.ACT)
                            .flatMap(entity -> Mono.error(ex))
                )
                .onErrorResume(RaddGenericException.class, ex ->
                        Mono.just(CompleteTransactionResponseMapper.fromException(ex))
                );
    }

    public Mono<AbortTransactionResponse> abortTransaction(String uid, AbortTransactionRequest req) {

        if (req == null || StringUtils.isBlank(req.getOperationId())
                || StringUtils.isBlank(req.getReason())) {
            log.error("Missing input parameters");
            return Mono.error(new PnInvalidInputException("Alcuni paramentri come operazione id o data di operazione non sono valorizzate"));
        }


        return raddTransactionDAO.getTransaction(req.getOperationId(), OperationTypeEnum.ACT)
                .map(raddEntity -> {
                    checkTransactionStatus(raddEntity);
                    raddEntity.setUid(uid);
                    raddEntity.setErrorReason(req.getReason());
                    raddEntity.setOperationEndDate(DateUtils.formatDate(req.getOperationDate()));
                    raddEntity.setStatus(Const.ABORTED);
                    return raddTransactionDAO.updateStatus(raddEntity);
                })
                .map(result -> AbortTransactionResponseMapper.fromResult())
                .onErrorResume(RaddGenericException.class, ex ->
                        Mono.just(AbortTransactionResponseMapper.fromException(ex))
                );
    }

    private Flux<String> legalFact(TransactionData transaction){
        return pnDeliveryPushInternalClient.getNotificationLegalFacts(transaction.getEnsureRecipientId(), transaction.getIun())
                .filter(legalFact -> legalFact.getLegalFactsId().getCategory() != LegalFactCategoryDto.PEC_RECEIPT)
                .flatMap(item -> pnDeliveryPushInternalClient
                            .getLegalFact(transaction.getEnsureRecipientId(), transaction.getIun(), item.getLegalFactsId().getCategory(), item.getLegalFactsId().getKey())
                                .mapNotNull(legalFact -> {
                                if (legalFact.getRetryAfter() != null && legalFact.getRetryAfter().intValue() != 0){
                                    log.debug("Finded legal fact with retry after {}", legalFact.getRetryAfter());
                                    throw new RaddGenericException(RETRY_AFTER, legalFact.getRetryAfter());
                                }
                                log.debug("URL : {}", legalFact.getUrl());
                                return legalFact.getUrl();
                            })
                    , 5);
    }

    private Flux<String> urlDocAndAttachments(TransactionData transaction, SentNotificationDto sentDTO){
        return Flux.fromStream(sentDTO.getDocuments().stream())
                    .flatMap(doc -> this.pnDeliveryClient.getPresignedUrlDocument(transaction.getIun(), doc.getDocIdx(), transaction.getEnsureRecipientId()))
                    .mapNotNull(NotificationAttachmentDownloadMetadataResponseDto::getUrl)
                    .concatWith(getUrlsAttachments(transaction, sentDTO));
    }

    private Flux<String> getUrlsAttachments(TransactionData transactionData, SentNotificationDto sentDTO){
        if (sentDTO.getRecipients().isEmpty())
            return Flux.empty();
        return Flux.fromStream(sentDTO.getRecipients().stream())
                .filter(item -> item.getTaxId().equalsIgnoreCase(transactionData.getRecipientId()) && item.getPayment() != null && item.getPayment().getPagoPaForm() != null)
                .flatMap(item -> pnDeliveryClient.getPresignedUrlPaymentDocument(transactionData.getIun(), "PAGOPA", transactionData.getEnsureRecipientId()))
                .mapNotNull(NotificationAttachmentDownloadMetadataResponseDto::getUrl);
    }

    private Mono<Integer> getCounterTransactions(String iun, String operationId){
        return Mono.fromFuture(this.raddTransactionDAO.countFromIunAndOperationIdAndStatus(operationId, iun)
                .thenApply(response -> {
                    if (response > 0){
                        throw new RaddGenericException(TRANSACTION_ALREADY_EXIST);
                    }
                    return response;
                })
        );
    }

    private Mono<ResponseCheckAarDtoDto> controlAndCheckAar(String recipientType, String recipientTaxId, String qrCode){
        if (StringUtils.isBlank(recipientTaxId) || !Utils.checkPersonType(recipientType) || StringUtils.isBlank(qrCode)) {
            log.error("Missing input parameters");
            throw new PnInvalidInputException("Codice fiscale, tipo utente o codice fiscale non valorizzato");
        }
        return this.pnDeliveryClient.getCheckAar(recipientType, recipientTaxId, qrCode)
                .map(response -> {
                    if (response == null || Strings.isBlank(response.getIun())){
                        throw new RaddGenericException(IUN_NOT_FOUND);
                    }
                    return response;
                });
    }

    private Mono<TransactionData> validateAndSettingsData(String uid, ActStartTransactionRequest request){
        if (Strings.isBlank(request.getOperationId())){
            return Mono.error(new PnInvalidInputException("Id operazione non valorizzato"));
        }
        if (Strings.isBlank(request.getRecipientTaxId())){
            return Mono.error(new PnInvalidInputException("Codice fiscale non valorizzato"));
        }
        if (Strings.isBlank(request.getQrCode())){
            return Mono.error(new PnInvalidInputException("QRCode non valorizzato"));
        }
        if (request.getRecipientType() == null || !Utils.checkPersonType(request.getRecipientType().getValue())){
            return Mono.error(new PnInvalidInputException("Recipient Type non valorizzato correttamente"));
        }
        return Mono.just(this.transactionDataMapper.toTransaction(uid, request));
    }

    private Mono<CompleteTransactionRequest> validateCompleteRequest(CompleteTransactionRequest req){
        if (StringUtils.isEmpty(req.getOperationId())){
            return Mono.error(new PnInvalidInputException("Operation id non valorizzato"));
        }
        return Mono.just(req);
    }

}
