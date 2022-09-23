package it.pagopa.pn.radd.services.radd.fsu.v1;

import io.netty.handler.codec.http.HttpResponseStatus;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.internal.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.NotificationDocumentDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
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
import reactor.util.function.Tuple2;

import java.util.ArrayList;
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

    public ActService(RaddTransactionDAO raddTransactionDAO, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient, PnDataVaultClient pnDataVaultClient, PnSafeStorageClient safeStorageClient, PnDeliveryInternalClient pnDeliveryInternalClient) {
        this.raddTransactionDAO = raddTransactionDAO;
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.pnDataVaultClient = pnDataVaultClient;
        this.safeStorageClient = safeStorageClient;
        this.pnDeliveryInternalClient = pnDeliveryInternalClient;
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
                        return Mono.just(actInquiryErrorResponse(ex));
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
                .zipWhen(onlyRequest -> sentNotification(iunRef.get()), (onlyRequest, response) -> response);

    }

    public Mono<CompleteTransactionResponse> completeTransaction(String uid, Mono<CompleteTransactionRequest> completeTransactionRequest) {
        return completeTransactionRequest.map(req -> req)
                .zipWhen(req -> this.raddTransactionDAO.getTransaction(req.getOperationId())
                                .map(entity -> {
                                    checkTransctionStatus(entity);
                                    return entity;
                                }),
                        (request, entity) -> entity)
                .zipWhen(this.pnDeliveryPushClient::notifyNotificationViewed, (entity, response) -> entity)
                .zipWhen(entity -> {
                    // TODO aggiungere data
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
                    checkTransctionStatus(raddEntity);
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

    private void checkTransctionStatus(RaddTransactionEntity entity) {
        if (StringUtils.equals(entity.getStatus(), Const.COMPLETED)) {
            throw new RaddTransactionStatusException("Stato Transazione incoerente", "La trasazione risulta già completa", HttpResponseStatus.CONFLICT.code());
        } else if (StringUtils.equals(entity.getStatus(), Const.ABORTED)){
            throw new RaddTransactionStatusException("Stato Transazione incoerente", "La trasazione risulta annullata", HttpResponseStatus.FORBIDDEN.code());
        }
    }

    private Mono<StartTransactionResponse> sentNotification(String iun) {

        return this.pnDeliveryClient.getNotifications(iun).map(item -> {
            if (item != null && !item.getDocuments().isEmpty()){
                return getUrlsList(item.getDocuments(), iun);
            }
            return new ArrayList<Mono<String>>();

        }).map(this::getMonoResponse);
    }

    private StartTransactionResponse getMonoResponse(List<Mono<String>> list){
        Flux<String> mergedMono = Flux.fromIterable(list)
                .flatMapSequential(Function.identity());
        List<String> elements = new ArrayList<>();
        mergedMono.subscribe(elements::add);
        StartTransactionResponse response = getResponse();
        response.setUrlList(elements);
        return response;
    }

    private List<Mono<String>> getUrlsList(List<NotificationDocumentDto> list, String iun){
        return list.stream().map(document -> urlForDocument(iun, document).map(url -> url)).collect(Collectors.toList());
    }

    private StartTransactionResponse getResponse(){
        StartTransactionResponse response = new StartTransactionResponse();
        StartTransactionResponseStatus status = new StartTransactionResponseStatus();
        status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_2);
        response.setStatus(status);
        response.setUrlList(new ArrayList<>());
        return response;
    }

    private Mono<String> urlForDocument(String iun,  NotificationDocumentDto documentDto){
        log.info(documentDto.toString());
        //f24standard != null
        if (documentDto.getTitle() == null){
            //documento normale
            return this.pnDeliveryInternalClient.getPresignedUrlPaymentDocument(iun, documentDto.getTitle())
                    .mapNotNull(NotificationAttachmentDownloadMetadataResponseDto::getUrl);
        }
        return this.pnDeliveryInternalClient.getPresignedUrlDocument(iun, documentDto.getDocIdx())
                .mapNotNull(NotificationAttachmentDownloadMetadataResponseDto::getUrl);

    }

    private Mono<FileDownloadResponseDto> verifyCheckSum(String fileKey, String checkSum){
        return this.safeStorageClient.getFile(fileKey).map(response -> {
            log.info("CheckSum response : {}", response.getChecksum());
            log.info("CheckSum Request : {}", checkSum);
            if (StringUtils.equals(response.getDocumentStatus(), Const.PRELOADED)){
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

    private ActInquiryResponse actInquiryErrorResponse(Throwable ex) {
        ActInquiryResponse r = new ActInquiryResponse();
        r.setResult(false);
        ActInquiryResponseStatus status = new ActInquiryResponseStatus();
        status.setMessage(Const.KO);
        WebClientResponseException webClientException = null;
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
