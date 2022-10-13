package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.mapper.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.NotificationRecipientDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.SentNotificationDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        return getEnsureFiscalCode(recipientTaxId, recipientType)
                .zipWhen(recCode -> controlAndCheckAar(recipientType, recCode, qrCode))
                .map(item -> ActInquiryResponseMapper.fromResult())
                .onErrorResume(RaddGenericException.class, ex -> Mono.just(ActInquiryResponseMapper.fromException(ex)));
    }

    public Mono<StartTransactionResponse> startTransaction(String uid, ActStartTransactionRequest request){
        return validateAndSettingsData(uid, request)
                .zipWhen(tmp -> controlAndCheckAar(tmp.getRecipientType(), tmp.getRecipientId(), tmp.getQrCode())
                        .map(ResponseCheckAarDtoDto::getIun), (transaction, iun) -> {
                                                                transaction.setIun(iun);
                                                                return transaction;
                })
                .zipWhen( transaction -> getCounterTransactions(transaction.getIun(), transaction.getOperationId()), (transaction, counter)-> transaction)
                .zipWhen(this::getEnsureRecipientAndDelegate, (transaction, transationReq) -> transationReq)
                .zipWhen( transaction -> {
                    log.info("Ensure recipient : {}", transaction.getEnsureRecipientId());
                    return this.raddTransactionDAO.createRaddTransaction(transactionDataMapper.toEntity(uid, transaction));
                }, (transaction, entity) -> transaction )
                .zipWhen(this::verifyCheckSum, (transaction, responseCheckSum) -> transaction)
                .zipWhen(this::updateFileMetadata, (transaction, t2) -> transaction)
                .zipWhen(this::notification, (transaction, transactionWithUlrs) -> transactionWithUlrs)

                .zipWhen(transaction ->
                    legalFact(transaction)
                            .collectList().map(listUrl -> {
                                listUrl.addAll(transaction.getUrls());
                                return StartTransactionResponseMapper.fromResult(listUrl);
                            }), (transaction, response) -> response
                )
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
                .flatMap(item ->pnDeliveryPushInternalClient
                            .getLegalFact(transaction.getEnsureRecipientId(), transaction.getIun(), item.getLegalFactsId().getCategory(), item.getLegalFactsId().getKey())
                            .mapNotNull(legalFact -> {
                                if (legalFact.getRetryAfter() != null && legalFact.getRetryAfter().intValue() != 0){
                                    log.info("Finded legal fact with retry after {}", legalFact.getRetryAfter());
                                   throw new RaddGenericException(RETRY_AFTER, legalFact.getRetryAfter());
                                }
                                return legalFact.getUrl();
                            })
                );
    }

    private Mono<TransactionData> notification(TransactionData transaction) {
        return this.pnDeliveryClient.getNotifications(transaction.getIun())
                .zipWhen(response -> docIdAndAttachments(transaction, response),
                        (response, tupleUrl) -> tupleUrl)
                .map(urls -> {
                    transaction.getUrls().addAll(urls);
                    return transaction;
                });
    }

    private Mono<List<String>> docIdAndAttachments(TransactionData transaction, SentNotificationDto sentDTO){
        return Mono.just(sentDTO)
                .zipWhen(notification -> {
                    if (notification.getDocuments().isEmpty()){
                        return Mono.just(new ArrayList<String>());
                    }
                    return retrieveUrlsDocuments(transaction, sentDTO).collectList();
                })
                .zipWhen(notAndUrls -> {
                    SentNotificationDto dto = notAndUrls.getT1();
                    if (!dto.getRecipients().isEmpty()){
                        List<NotificationRecipientDto> listDTO =
                                dto.getRecipients().stream()
                                        .filter(i -> i.getTaxId().equals(transaction.getRecipientId())).collect(Collectors.toList());

                        if (!listDTO.isEmpty()){
                            NotificationRecipientDto recipient = listDTO.get(0);
                            if (recipient.getPayment() != null && recipient.getPayment().getPagoPaForm() != null){
                                return pnDeliveryClient.getPresignedUrlPaymentDocument(transaction.getIun(), "PAGOPA", transaction.getEnsureRecipientId())
                                        .mapNotNull(NotificationAttachmentDownloadMetadataResponseDto::getUrl);
                            }
                        }
                    }
                    return Mono.just("");
                }, (notAndTransaction, urlAttachment) ->  {
                    List<String> urls = notAndTransaction.getT2();
                    urls.add(urlAttachment);
                    return urls;
                });
    }

    private Flux<String> retrieveUrlsDocuments(TransactionData transaction, SentNotificationDto documents){

        return Flux.fromStream(documents.getDocuments().stream())
                .flatMap(document ->
                        pnDeliveryClient.getPresignedUrlDocument(transaction.getIun(), document.getDocIdx(), transaction.getEnsureRecipientId())
                        .mapNotNull(NotificationAttachmentDownloadMetadataResponseDto::getUrl));
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
