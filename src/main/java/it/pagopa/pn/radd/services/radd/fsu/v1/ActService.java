package it.pagopa.pn.radd.services.radd.fsu.v1;

import io.netty.handler.codec.http.HttpResponseStatus;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.TransactionDataMapper;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.internal.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
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
import it.pagopa.pn.radd.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ActService extends BaseService {

    private final RaddTransactionDAO raddTransactionDAO;
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;
    private final PnDataVaultClient pnDataVaultClient;
    private final PnSafeStorageClient safeStorageClient;
    private final PnDeliveryInternalClient pnDeliveryInternalClient;
    private final PnDeliveryPushInternalClient pnDeliveryPushInternalClient;
    private final TransactionDataMapper transactionDataMapper;

    public ActService(RaddTransactionDAO raddTransactionDAO, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient, PnDataVaultClient pnDataVaultClient, PnSafeStorageClient safeStorageClient, PnDeliveryInternalClient pnDeliveryInternalClient, PnDeliveryPushInternalClient pnDeliveryPushInternalClient, TransactionDataMapper transactionDataMapper) {
        this.raddTransactionDAO = raddTransactionDAO;
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.pnDataVaultClient = pnDataVaultClient;
        this.safeStorageClient = safeStorageClient;
        this.pnDeliveryInternalClient = pnDeliveryInternalClient;
        this.pnDeliveryPushInternalClient = pnDeliveryPushInternalClient;
        this.transactionDataMapper = transactionDataMapper;
    }

    public Mono<ActInquiryResponse> actInquiry(String uid, String recipientTaxId, String recipientType, String qrCode) {
        // check if iun exists
        return getEnsureFiscalCode(recipientTaxId, recipientType, this.pnDataVaultClient)
                .zipWhen(recCode -> controlAndCheckAar(recipientType, recCode, qrCode))
                .map(item -> {
                    ResponseCheckAarDtoDto response = item.getT2();
                    log.info("Response iun : {}", response.getIun());
                    ActInquiryResponse actInquiryResponse = new ActInquiryResponse();
                    actInquiryResponse.setResult(true);
                    ActInquiryResponseStatus status = new ActInquiryResponseStatus();
                    status.setMessage(Const.OK);
                    status.code(ActInquiryResponseStatus.CodeEnum.NUMBER_0);
                    actInquiryResponse.setStatus(status);
                    return actInquiryResponse;
                }).onErrorResume(ex -> {
                    if (ex instanceof PnCheckQrCodeException || ex instanceof PnEnsureFiscalCodeException) {
                        return Mono.just(actInquiryErrorResponse(ex));
                    }
                    return Mono.error(ex);
                });
    }

    public Mono<StartTransactionResponse> startTransaction(String uid, Mono<ActStartTransactionRequest> request){
        return request
                .map(this::validateAndSettingsData)
                .zipWhen(tmp -> controlAndCheckAar(tmp.getRecipientType(), tmp.getRecipientId(), tmp.getQrCode())
                        .map(ResponseCheckAarDtoDto::getIun), (transaction, iun) -> {
                                                                transaction.setIun(iun);
                                                                return transaction;
                })
                .zipWhen( transaction -> getCounterNotification(transaction.getIun(), transaction.getOperationId()), (transaction, counter)-> transaction)
                .zipWhen(this::getEnsureRecipientAndDelegate, (transaction, transactionWithEnsure) -> transactionWithEnsure)
                .zipWhen( transaction -> {
                    log.info("Ensure recipient : {}", transaction.getEnsureRecipientId());
                    return createTransaction(transaction, uid);
                }, (transaction, entity) -> transaction)

                .zipWhen(transaction -> verifyCheckSum(transaction.getFileKey(), transaction.getChecksum()), (transaction, responseCheckSum) -> transaction)

                .zipWhen(this::notification, (transaction, transactionWithUlrs) -> transactionWithUlrs)

                .flatMap(transaction ->
                    legalFact(uid,transaction.getIun(), transaction.getRecipientType())
                            .collectList().map(listUrl -> {
                                listUrl.addAll(transaction.getUrls());
                                StartTransactionResponse response = new StartTransactionResponse();
                                response.setUrlList(listUrl);
                                StartTransactionResponseStatus status = new StartTransactionResponseStatus();
                                status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_0);
                                response.setStatus(status);
                                return response;
                            })
                );

    }

    public Mono<CompleteTransactionResponse> completeTransaction(String uid, Mono<CompleteTransactionRequest> completeTransactionRequest) {
        return completeTransactionRequest.map(req -> req)
                .zipWhen(req -> this.raddTransactionDAO.getTransaction(req.getOperationId())
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
                }).map(tupla -> {
                    CompleteTransactionResponse response = new CompleteTransactionResponse();
                    TransactionResponseStatus status = new TransactionResponseStatus();
                    status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_0);
                    status.setMessage(Const.OK);
                    response.setStatus(status);
                    return response;
                }).onErrorResume(ex -> {
                    if (ex instanceof RaddTransactionNoExistedException || ex instanceof RaddTransactionStatusException) {
                        return Mono.just(completeErrorResponse(ex));
                    }
                    return Mono.error(ex);
                });
    }

    public Mono<AbortTransactionResponse> abortTransaction(String uid, Mono<AbortTransactionRequest> abortTransactionRequestMono) {
        return abortTransactionRequestMono
                .map(m -> {
                    if (m == null || StringUtils.isEmpty(m.getOperationId())
                            || StringUtils.isEmpty(m.getReason())
                            || m.getOperationDate() == null) {
                        log.error("Missing input parameters");
                        throw new PnInvalidInputException();
                    }
                    return m;
                })
                .zipWhen(operation -> raddTransactionDAO.getTransaction(operation.getOperationId()))
                .map(entity -> {
                    RaddTransactionEntity raddEntity = entity.getT2();
                    checkTransactionStatus(raddEntity);
                    raddEntity.setUid(uid);
                    raddEntity.setErrorReason(entity.getT1().getReason());
                    raddEntity.setOperationEndDate(DateUtils.formatDate(entity.getT1().getOperationDate()));
                    raddEntity.setStatus(Const.ABORTED);
                    return raddTransactionDAO.updateStatus(raddEntity);
                }).map(result -> {
                    AbortTransactionResponse response = new AbortTransactionResponse();
                    TransactionResponseStatus status = new TransactionResponseStatus();
                    status.setMessage(Const.OK);
                    status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_1);
                    response.setStatus(status);
                    return response;
                }).onErrorResume(ex -> {
                    if (ex instanceof RaddTransactionNoExistedException || ex instanceof RaddTransactionStatusException) {
                        return Mono.just(abortErrorResponse(ex));
                    }
                    return Mono.error(ex);
                });
    }

    private Flux<String> legalFact(String uid, String iun, String recipientType){
        return pnDeliveryPushInternalClient.getNotificationLegalFacts(uid, iun, recipientType)
                .flatMap(item ->
                        pnDeliveryPushInternalClient
                                .getLegalFact(uid, iun, recipientType,
                                        item.getLegalFactsId().getCategory(), item.getLegalFactsId().getKey())
                                .mapNotNull(LegalFactDownloadMetadataResponseDto::getUrl)
                ).onErrorResume(Mono::error);
    }

    private Mono<TransactionData> notification(TransactionData transaction) {
        return this.pnDeliveryClient.getNotifications(transaction.getIun())
                .zipWhen(response -> docIdAndAttachments(transaction.getIun(), transaction.getEnsureRecipientId(), response), (response, tupleUrl) -> tupleUrl)
                .map(url -> {
                    transaction.getUrls().add(url.getT1());
                    transaction.getUrls().add(url.getT2());
                    return transaction;
                })
                .onErrorResume(Mono::error);
    }

    private Mono<Tuple2<String, String>> docIdAndAttachments(String iun, String fiscalCode, SentNotificationDto sentDTO){
        return Mono.just(sentDTO)
                .zipWhen(notification -> {
                    if (!notification.getDocuments().isEmpty()){
                        return pnDeliveryInternalClient.getPresignedUrlDocument(iun, notification.getDocuments().get(0).getDocIdx())
                                .mapNotNull(NotificationAttachmentDownloadMetadataResponseDto::getUrl);
                    }
                    return Mono.just("");
                }).zipWhen(notAndUrlDoc -> {
                    SentNotificationDto dto = notAndUrlDoc.getT1();
                    if (!dto.getRecipients().isEmpty()){
                        List<NotificationRecipientDto> listDTO =
                                dto.getRecipients().stream()
                                        .filter(i -> i.getTaxId().equals(fiscalCode)).collect(Collectors.toList());

                        if (!listDTO.isEmpty()){
                            NotificationRecipientDto recipient = listDTO.get(0);
                            if (recipient.getPayment() != null && recipient.getPayment().getPagoPaForm() != null){
                                String attachment = recipient.getPayment().getPagoPaForm().getRef().getKey();
                                return pnDeliveryInternalClient.getPresignedUrlPaymentDocument(iun, attachment)
                                        .mapNotNull(NotificationAttachmentDownloadMetadataResponseDto::getUrl);
                            }
                        }
                    }
                    return Mono.just("");
                }, (notificationAndUrlDoc, urlAttachment) ->  Tuples.of(notificationAndUrlDoc.getT2(), urlAttachment));
    }

    private Mono<FileDownloadResponseDto> verifyCheckSum(String fileKey, String checkSum){
        return this.safeStorageClient.getFile(fileKey).map(response -> {
            if (!StringUtils.equals(response.getDocumentStatus(), Const.PRELOADED)){
                throw new RaddDocumentStatusException("Status is not preloaded");
            }
            if (Strings.isBlank(response.getChecksum()) ||
                    !response.getChecksum().equals(checkSum)){
                throw new RaddChecksumException();
            }
            return response;
        });
    }

    private Mono<RaddTransactionEntity> createTransaction(TransactionData transaction, String uid){
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setIun(transaction.getIun());
        entity.setOperationId(transaction.getOperationId());
        entity.setDelegateId(transaction.getEnsureDelegateId());
        entity.setRecipientId(transaction.getEnsureRecipientId());
        entity.setFileKey(transaction.getFileKey());
        entity.setUid(uid);
        entity.setQrCode(transaction.getQrCode());
        entity.setStatus(Const.STARTED);
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
        return Mono.fromFuture(this.raddTransactionDAO.countFromIunAndIdPracticeAndStatus(iun, operationId)
                .thenApply(response -> {
                    if (response > 0){
                        throw new RaddTransactionAlreadyExist();
                    }
                    return response;
                })
        );
    }

    private Mono<ResponseCheckAarDtoDto> controlAndCheckAar(String recipientType, String recipientTaxId, String qrCode){
        if (StringUtils.isEmpty(recipientTaxId) || !Utils.checkPersonType(recipientType) || StringUtils.isEmpty(qrCode)) {
            log.error("Missing input parameters");
            throw new PnInvalidInputException();
        }
        return this.pnDeliveryClient.getCheckAar(recipientType, recipientTaxId, qrCode)
                .map(response -> {
                    if (response == null || Strings.isBlank(response.getIun())){
                        throw new RaddIunNotFoundException();
                    }
                    return response;
                }).onErrorResume(Mono::error);
    }

    private void checkTransactionStatus(RaddTransactionEntity entity) {
        if (StringUtils.equals(entity.getStatus(), Const.COMPLETED)) {
            throw new RaddTransactionStatusException("Stato Transazione incoerente", "La trasazione risulta già completa", HttpResponseStatus.CONFLICT.code());
        } else if (StringUtils.equals(entity.getStatus(), Const.ABORTED)){
            throw new RaddTransactionStatusException("Stato Transazione incoerente", "La trasazione risulta annullata", HttpResponseStatus.FORBIDDEN.code());
        }
    }

    private TransactionData validateAndSettingsData(ActStartTransactionRequest request){
        if (Strings.isBlank(request.getOperationId())){
            throw new RaddTransactionStatusException("Id operazione", "Id operazione non valorizzato", HttpResponseStatus.BAD_REQUEST.code());
        }
        if (Strings.isBlank(request.getRecipientTaxId())){
            throw new RaddTransactionStatusException("Codice Fiscale", "Codice fiscale non valorizzato", HttpResponseStatus.BAD_REQUEST.code());
        }
        if (Strings.isBlank(request.getQrCode())){
            throw new RaddTransactionStatusException("QRCode", "QRCode non valorizzato", HttpResponseStatus.BAD_REQUEST.code());
        }
        if (!Utils.checkPersonType(request.getRecipientType().getValue())){
            throw new PnInvalidInputException("Recipient Type non valorizzato correttamente");
        }
        return this.transactionDataMapper.toTransaction(request);
    }


    private ActInquiryResponse actInquiryErrorResponse(Throwable ex) {
        ActInquiryResponse r = new ActInquiryResponse();
        r.setResult(false);
        ActInquiryResponseStatus status = new ActInquiryResponseStatus();
        status.setMessage(Const.KO);

        WebClientResponseException webClientException;
        if (ex instanceof PnCheckQrCodeException) {
            webClientException = ((PnCheckQrCodeException) ex).getWebClientEx();
            if (webClientException.getRawStatusCode() == HttpResponseStatus.NOT_FOUND.code()) {
                status.setMessage(Const.NOT_VALID_QR_CODE);
                status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_1);

            } else if (webClientException.getRawStatusCode() == HttpResponseStatus.FORBIDDEN.code()) {
                status.setMessage(Const.NOT_FOUND_DOCUMENT);
                status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_2);

            } else if (webClientException.getRawStatusCode() == HttpResponseStatus.CONFLICT.code()) {
                status.setMessage(Const.ALREADY_COMPLETE_PRINT);
                status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_3);

            } else {
                status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_99);
            }

        } else if (ex instanceof PnEnsureFiscalCodeException) {
            webClientException = ((PnEnsureFiscalCodeException) ex).getWebClientEx();
            if (webClientException.getRawStatusCode() == HttpResponseStatus.BAD_REQUEST.code()) {
                status.setMessage(Const.NOT_VALID_FISCAL_CODE);
                status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_1);

            } else {
                status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_99);
            }
        } else {
            status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_99);
        }
        r.setStatus(status);
        return r;
    }

}
