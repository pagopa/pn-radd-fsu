package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.NotificationRecipientDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.SentNotificationDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactDownloadMetadataResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileDownloadResponseDto;
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

import static it.pagopa.pn.radd.exception.ExceptionCodeEnum.KO;
import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.*;

@Service
@Slf4j
public class ActService extends BaseService {

    private final RaddTransactionDAO raddTransactionDAO;
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;
    private final PnDataVaultClient pnDataVaultClient;
    private final PnSafeStorageClient safeStorageClient;
    private final PnDeliveryPushInternalClient pnDeliveryPushInternalClient;
    private final TransactionDataMapper transactionDataMapper;

    public ActService(RaddTransactionDAO raddTransactionDAO, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient, PnDataVaultClient pnDataVaultClient, PnSafeStorageClient safeStorageClient, PnDeliveryPushInternalClient pnDeliveryPushInternalClient, TransactionDataMapper transactionDataMapper) {
        this.raddTransactionDAO = raddTransactionDAO;
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.pnDataVaultClient = pnDataVaultClient;
        this.safeStorageClient = safeStorageClient;
        this.pnDeliveryPushInternalClient = pnDeliveryPushInternalClient;
        this.transactionDataMapper = transactionDataMapper;
    }

    public Mono<ActInquiryResponse> actInquiry(String uid, String recipientTaxId, String recipientType, String qrCode) {
        // check if iun exists
        return getEnsureFiscalCode(recipientTaxId, recipientType, this.pnDataVaultClient)
                .zipWhen(recCode -> controlAndCheckAar(recipientType, recCode, qrCode))
                .map(item -> ActInquiryResponseMapper.fromResult())
                .onErrorResume(RaddGenericException.class, ex -> Mono.just(ActInquiryResponseMapper.fromException(ex)));
    }

    public Mono<StartTransactionResponse> startTransaction(String uid, Mono<ActStartTransactionRequest> request){
        return request
                .map(req -> validateAndSettingsData(uid, req))
                .zipWhen(tmp -> controlAndCheckAar(tmp.getRecipientType(), tmp.getRecipientId(), tmp.getQrCode())
                        .map(ResponseCheckAarDtoDto::getIun), (transaction, iun) -> {
                                                                transaction.setIun(iun);
                                                                return transaction;
                })
                .zipWhen( transaction -> getCounterNotification(transaction.getIun(), transaction.getOperationId()), (transaction, counter)-> transaction)
                .zipWhen(this::getEnsureRecipientAndDelegate, (transaction, transactionWithEnsure) -> transactionWithEnsure)
                .zipWhen( transaction -> {
                    log.info("Ensure recipient : {}", transaction.getEnsureRecipientId());
                    return createTransaction(transaction, uid, OperationTypeEnum.ACT);
                }, (transaction, entity) -> transaction)

                .zipWhen(transaction -> verifyCheckSum(transaction.getFileKey(), transaction.getChecksum(), transaction.getVersionId()), (transaction, responseCheckSum) -> transaction)
                .zipWhen(this::updateFileMetadata, (transaction, t2) -> transaction)
                .zipWhen(this::notification, (transaction, transactionWithUlrs) -> transactionWithUlrs)

                .zipWhen(transaction ->
                    legalFact(transaction)
                            .collectList().map(listUrl -> {
                                log.info("Creo la risposta");
                                listUrl.addAll(transaction.getUrls());
                                return StartTransactionResponseMapper.fromResult(listUrl);
                            }), (transaction, response) -> response
                ).onErrorResume(RaddGenericException.class, ex -> Mono.just(StartTransactionResponseMapper.fromException(ex)));

    }

    public Mono<CompleteTransactionResponse> completeTransaction(String uid, Mono<CompleteTransactionRequest> completeTransactionRequest) {
        return completeTransactionRequest.map(this::validateCompleteRequest)
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
                .onErrorResume(RaddGenericException.class, ex -> Mono.just(CompleteTransactionResponseMapper.fromException(ex)));
    }

    public Mono<AbortTransactionResponse> abortTransaction(String uid, Mono<AbortTransactionRequest> abortTransactionRequestMono) {
        return abortTransactionRequestMono
                .map(m -> {
                    if (m == null || StringUtils.isEmpty(m.getOperationId())
                            || StringUtils.isEmpty(m.getReason())
                            || m.getOperationDate() == null) {
                        log.error("Missing input parameters");
                        throw new PnInvalidInputException("Alcuni paramentri come operazione id o data di operazione non sono valorizzate");
                    }
                    return m;
                })
                .zipWhen(operation -> raddTransactionDAO.getTransaction(operation.getOperationId(), OperationTypeEnum.ACT))
                .map(entity -> {
                    RaddTransactionEntity raddEntity = entity.getT2();
                    checkTransactionStatus(raddEntity);
                    raddEntity.setUid(uid);
                    raddEntity.setErrorReason(entity.getT1().getReason());
                    raddEntity.setOperationEndDate(DateUtils.formatDate(entity.getT1().getOperationDate()));
                    raddEntity.setStatus(Const.ABORTED);
                    return raddTransactionDAO.updateStatus(raddEntity);
                })
                .map(result -> AbortTransactionResponseMapper.fromResult())
                .onErrorResume(RaddGenericException.class,
                        ex -> Mono.just(AbortTransactionResponseMapper.fromException(ex)));
    }

    private Flux<String> legalFact(TransactionData transaction){
        return pnDeliveryPushInternalClient.getNotificationLegalFacts(transaction.getEnsureRecipientId(), transaction.getIun())
                .flatMap(item ->pnDeliveryPushInternalClient
                            .getLegalFact(transaction.getEnsureRecipientId(), transaction.getIun(), item.getLegalFactsId().getCategory(), item.getLegalFactsId().getKey())
                            .mapNotNull(LegalFactDownloadMetadataResponseDto::getUrl)
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

    private Mono<FileDownloadResponseDto> verifyCheckSum(String fileKey, String checkSum, String versionId){
        return this.safeStorageClient.getFile(fileKey).map(response -> {
            /*
            if (!StringUtils.equals(response.getDocumentStatus(), Const.PRELOADED)){
                throw new RaddGenericException(DOCUMENT_STATUS_VALIDATION, KO);
            }

            if (!StringUtils.equals(response.getVersionId(), versionId)){
                throw new RaddGenericException(VERSION_ID_VALIDATION, KO);
            }
            */
            if (Strings.isBlank(response.getChecksum()) ||
                    !response.getChecksum().equals(checkSum)){
                throw new RaddGenericException(CHECKSUM_VALIDATION, KO);
            }
            return response;
        });
    }

    private Mono<TransactionData> updateFileMetadata(TransactionData transactionData){
        return this.safeStorageClient.updateFileMetadata(transactionData.getFileKey()).map(resp -> transactionData);
    }

    private Mono<RaddTransactionEntity> createTransaction(TransactionData transaction, String uid, OperationTypeEnum operationType){
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setIun(transaction.getIun());
        entity.setOperationId(transaction.getOperationId());
        entity.setDelegateId(transaction.getEnsureDelegateId());
        entity.setRecipientId(transaction.getEnsureRecipientId());
        entity.setRecipientType(transaction.getRecipientType());
        entity.setFileKey(transaction.getFileKey());
        entity.setUid(uid);
        entity.setQrCode(transaction.getQrCode());
        entity.setStatus(Const.STARTED);
        if (operationType != null) {
            entity.setOperationType(operationType.name());
        }
        entity.setOperationStartDate(DateUtils.formatDate(transaction.getOperationDate()));
        return this.raddTransactionDAO.createRaddTransaction(entity);
    }


    private Mono<TransactionData> getEnsureRecipientAndDelegate(TransactionData transaction){
        return getEnsureFiscalCode(transaction.getRecipientId(), transaction.getRecipientType(), this.pnDataVaultClient)
                .flatMap(ensureRecipient -> {
                    if (!Strings.isBlank(transaction.getDelegateId())){
                        return getEnsureFiscalCode(transaction.getDelegateId(), Const.PF, this.pnDataVaultClient)
                                .flatMap(delegateEnsure -> {
                                    transaction.setEnsureRecipientId(ensureRecipient);
                                    transaction.setEnsureDelegateId(delegateEnsure);
                                    return Mono.just(transaction);
                                });
                    }
                    transaction.setEnsureRecipientId(ensureRecipient);
                    return  Mono.just(transaction);
                });
    }

    private Mono<Integer> getCounterNotification(String iun, String operationId){
        return Mono.fromFuture(this.raddTransactionDAO.countFromIunAndOperationIdAndStatus(iun, operationId)
                .thenApply(response -> {
                    if (response > 0){
                        throw new RaddGenericException(TRANSACTION_ALREADY_EXIST, KO);
                    }
                    return response;
                })
        );
    }

    private Mono<ResponseCheckAarDtoDto> controlAndCheckAar(String recipientType, String recipientTaxId, String qrCode){
        if (StringUtils.isEmpty(recipientTaxId) || !Utils.checkPersonType(recipientType) || StringUtils.isEmpty(qrCode)) {
            log.error("Missing input parameters");
            throw new PnInvalidInputException("Codice fiscale, tipo utente o codice fiscale non valorizzato");
        }
        return this.pnDeliveryClient.getCheckAar(recipientType, recipientTaxId, qrCode)
                .map(response -> {
                    if (response == null || Strings.isBlank(response.getIun())){
                        throw new RaddGenericException(IUN_NOT_FOUND, KO);
                    }
                    return response;
                });
    }

    private void checkTransactionStatus(RaddTransactionEntity entity) {
        if (StringUtils.equals(entity.getStatus(), Const.COMPLETED)) {
            throw new RaddGenericException(TRANSACTION_ALREADY_COMPLETED, ExceptionCodeEnum.NUMBER_2);
        } else if (StringUtils.equals(entity.getStatus(), Const.ABORTED)){
            throw new RaddGenericException(TRANSACTION_ALREADY_ABORTED, ExceptionCodeEnum.NUMBER_2);
        }
    }

    private TransactionData validateAndSettingsData(String uid, ActStartTransactionRequest request){
        if (Strings.isBlank(request.getOperationId())){
            throw new PnInvalidInputException("Id operazione non valorizzato");
        }
        if (Strings.isBlank(request.getRecipientTaxId())){
            throw new PnInvalidInputException("Codice fiscale non valorizzato");
        }
        if (Strings.isBlank(request.getQrCode())){
            throw new PnInvalidInputException("QRCode non valorizzato");
        }
        if (!Utils.checkPersonType(request.getRecipientType().getValue())){
            throw new PnInvalidInputException("Recipient Type non valorizzato correttamente");
        }
        return this.transactionDataMapper.toTransaction(uid, request);
    }

    private CompleteTransactionRequest validateCompleteRequest(CompleteTransactionRequest req){
        if (StringUtils.isEmpty(req.getOperationId())){
            throw new PnInvalidInputException("Operation id non valorizzato");
        }
        return req;
    }

}
