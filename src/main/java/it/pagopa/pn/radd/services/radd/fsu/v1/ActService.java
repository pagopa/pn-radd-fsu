package it.pagopa.pn.radd.services.radd.fsu.v1;

import io.netty.handler.codec.http.HttpResponseStatus;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.internal.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.NotificationDocumentDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.NotificationRecipientDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.SentNotificationDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactDownloadMetadataResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileDownloadResponseDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.*;
import it.pagopa.pn.radd.pojo.EnsureFiscalCode;
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
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
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

    public ActService(RaddTransactionDAO raddTransactionDAO, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient, PnDataVaultClient pnDataVaultClient, PnSafeStorageClient safeStorageClient, PnDeliveryInternalClient pnDeliveryInternalClient, PnDeliveryPushInternalClient pnDeliveryPushInternalClient) {
        this.raddTransactionDAO = raddTransactionDAO;
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.pnDataVaultClient = pnDataVaultClient;
        this.safeStorageClient = safeStorageClient;
        this.pnDeliveryInternalClient = pnDeliveryInternalClient;
        this.pnDeliveryPushInternalClient = pnDeliveryPushInternalClient;
    }

    public Mono<ActInquiryResponse> actInquiry(String uid, String recipientTaxId, String recipientType, String qrCode) {
        // check if iun exists
        return getEnsureFiscalCode(recipientTaxId, recipientType)
                .zipWhen(recCode -> getCheckAar(recipientType, recCode, qrCode))
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
                        return Mono.just(addErrorResponse(ex));
                    }
                    return Mono.error(ex);
                });
    }

    public Mono<StartTransactionResponse> startTransaction(String uid, Mono<ActStartTransactionRequest> request){
        log.info("Service");

        AtomicReference<String> iunRef = new AtomicReference<>();
        return request.zipWhen(tmp -> getIun(
                        tmp.getRecipientType().getValue(),
                        tmp.getRecipientTaxId(),
                        tmp.getQrCode())
                )
                .zipWhen( reqAndIun -> getCounterNotification(reqAndIun.getT2(), reqAndIun.getT1().getOperationId()), (reqAndIun, counter)-> reqAndIun)
                .zipWhen( reqAndIun -> getEnsureRecipientAndDelegate(reqAndIun.getT1()))
                .zipWhen( reqIunAndEnsure -> {
                    iunRef.set(reqIunAndEnsure.getT1().getT2());
                    log.info("IUN : {}", iunRef.get());
                    log.info("Ensure recipient : {}", reqIunAndEnsure.getT2().getRecipient());
                    return createTransaction(reqIunAndEnsure.getT1().getT1(), reqIunAndEnsure.getT1().getT2(), reqIunAndEnsure.getT2(), uid);
                }, (reqIunAndEnsure, entity) -> reqIunAndEnsure.getT1().getT1())

                .zipWhen(onlyRequest -> verifyCheckSum(onlyRequest.getFileKey(), onlyRequest.getChecksum()), (onlyRequest, responseCheckSum) -> onlyRequest)

                .zipWhen(onlyRequest -> {
                    log.info("Sono nella notification : {}", iunRef.get());
                    return  notification(iunRef.get(), onlyRequest.getRecipientTaxId());
                })

                .flatMap(requestAndUrls -> {
                    return legalFact(uid, iunRef.get(), requestAndUrls.getT1().getRecipientType().getValue())
                            .collectList().map(listUrl -> {
                                String urlDoc = requestAndUrls.getT2().getT1();
                                String urlAttachment = requestAndUrls.getT2().getT2();
                                if (!Strings.isBlank(urlDoc)) listUrl.add(urlDoc);
                                if (!Strings.isBlank(urlAttachment)) listUrl.add(urlAttachment);

                                StartTransactionResponse response = new StartTransactionResponse();
                                response.setUrlList(listUrl);
                                StartTransactionResponseStatus status = new StartTransactionResponseStatus();
                                status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_2);
                                response.setStatus(status);
                                return response;
                            });
                });

    }

    public Mono<CompleteTransactionResponse> completeTransaction(String uid, Mono<CompleteTransactionRequest> completeTransactionRequest) {
        return completeTransactionRequest.map(req -> req)
                .zipWhen(req -> this.raddTransactionDAO.getTransaction(req.getOperationId())
                        .map(entity -> {
                            if (Strings.isBlank(entity.getStatus()) || entity.getStatus().equals(Const.COMPLETED)){
                                throw new RaddTransactionStatusException("Stato Transazione incoerente", "La trasazione risulta già completa");
                            }
                            return entity;
                        }),
                        (request, entity) -> entity)
                .zipWhen(this.pnDeliveryPushClient::notifyNotificationViewed, (entity, response) -> entity)
                .zipWhen(entity -> {
                    entity.setStatus(Const.COMPLETED);
                    return this.raddTransactionDAO.updateStatus(entity);
                }).map(tupla -> {
                    return new CompleteTransactionResponse();
                });
    }


    private Flux<String> legalFact(String uid, String iun, String recipientType){
        return pnDeliveryPushInternalClient.getNotificationLegalFacts(uid, iun, recipientType)
                .flatMap(item -> {
                    return pnDeliveryPushInternalClient
                            .getLegalFact(uid, iun, recipientType,
                                    item.getLegalFactsId().getCategory(), item.getLegalFactsId().getKey())
                            .map(LegalFactDownloadMetadataResponseDto::getUrl);
                });


    }

    private Mono<Tuple2<String, String>> notification(String iun, String fiscalCode) {
        return this.pnDeliveryClient.getNotifications(iun)
                .zipWhen(response -> docIdAndAttachments(iun, fiscalCode, response), (response, tupleUrl) -> tupleUrl);
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

    private Mono<RaddTransactionEntity> createTransaction(ActStartTransactionRequest request, String iun, EnsureFiscalCode ensureFiscalCode, String uid){
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setIun(iun);
        entity.setOperationId(request.getOperationId());
        entity.setDelegateId(ensureFiscalCode.getDelegate());
        entity.setRecipientId(ensureFiscalCode.getRecipient());
        entity.setFileKey(request.getFileKey());
        entity.setUid(uid);
        entity.setQrCode(request.getQrCode());
        entity.setStatus(Const.STARTED);
        entity.setOperationStartDate(DateUtils.formatDate(request.getOperationDate()));
        return this.raddTransactionDAO.createRaddTransaction(entity);
    }


    private Mono<EnsureFiscalCode> getEnsureRecipientAndDelegate(ActStartTransactionRequest request){
        return getEnsureFiscalCode(request.getRecipientTaxId(), request.getRecipientType().getValue())
                .flatMap(ensureRecipient -> {
                    if (!Strings.isBlank(request.getDelegateTaxId())){
                        return getEnsureFiscalCode(request.getDelegateTaxId(), Const.PF)
                                .flatMap(delegateEnsure -> Mono.just(new EnsureFiscalCode(ensureRecipient, delegateEnsure)));
                    }
                    return  Mono.just(new EnsureFiscalCode(ensureRecipient, null));
                });
    }

    private Mono<Integer> getCounterNotification(String iun, String operationId){
        return Mono.fromFuture(this.raddTransactionDAO.countTransactionIunIdPractice(iun, operationId)
                .thenApply(response -> {
                    if (response > 0){
                        throw new RaddTransactionAlreadyExist();
                    }
                    return response;
                })
        );
    }

    private Mono<String> getIun(String recipientType, String recipientTaxId, String qrCode){
        return this.pnDeliveryClient.getCheckAar(recipientType, recipientTaxId, qrCode)
                .map(response -> {
                    if (response == null || Strings.isBlank(response.getIun())){
                        throw new RaddIunNotFoundException();
                    }
                    return response.getIun();
                }).onErrorResume(Mono::error);
    }

    private Mono<String> getEnsureFiscalCode(String recipientTaxId, String type){
        return getEnsureFiscalCode(recipientTaxId, type, this.pnDataVaultClient);
    }

    private Mono<ResponseCheckAarDtoDto> getCheckAar(String recipientType, String recipientInternalId, String qrCode) {
        if (StringUtils.isEmpty(recipientInternalId) || !Utils.checkPersonType(recipientType) || StringUtils.isEmpty(qrCode)) {
            log.error("Missing input parameters");
            throw new PnInvalidInputException();
        }
        return pnDeliveryClient.getCheckAar(recipientType, recipientInternalId, qrCode);
    }

    private ActInquiryResponse addErrorResponse(Throwable ex){
        ActInquiryResponse r = new ActInquiryResponse();
        r.setResult(false);
        ActInquiryResponseStatus status = new ActInquiryResponseStatus();
        status.setMessage(Const.KO);
        WebClientResponseException webClientException = null;
        if (ex instanceof PnCheckQrCodeException) {
            webClientException = ((PnCheckQrCodeException) ex).getWebClientEx();
            if (webClientException.getRawStatusCode() == HttpResponseStatus.NOT_FOUND.code()) {
                status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_1);
            } else if (webClientException.getRawStatusCode() == HttpResponseStatus.FORBIDDEN.code()) {
                status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_2);
            } else if (webClientException.getRawStatusCode() == HttpResponseStatus.CONFLICT.code()) {
                status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_3);
            } else {
                status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_99);
            }

        } else if (ex instanceof PnEnsureFiscalCodeException) {
            webClientException = ((PnEnsureFiscalCodeException) ex).getWebClientEx();
            if (webClientException.getRawStatusCode() == HttpResponseStatus.BAD_REQUEST.code()) {
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
