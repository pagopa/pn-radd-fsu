package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.internal.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.NotificationDocumentDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileDownloadResponseDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.*;
import it.pagopa.pn.radd.pojo.EnsureFiscalCode;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService {

    private final PnDataVaultClient pnDataVaultClient;
    private final PnDeliveryClient pnDeliveryClient;
    private final RaddTransactionDAO raddTransactionDAO;
    private final PnSafeStorageClient safeStorageClient;
    private final PnDeliveryInternalClient pnDeliveryInternalClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;

    public TransactionService(PnDataVaultClient pnDataVaultClient, PnDeliveryClient pnDeliveryClient, RaddTransactionDAO raddTransactionDAO, PnSafeStorageClient safeStorageClient, PnDeliveryInternalClient pnDeliveryInternalClient, PnDeliveryPushClient pnDeliveryPushClient) {
        this.pnDataVaultClient = pnDataVaultClient;
        this.pnDeliveryClient = pnDeliveryClient;
        this.raddTransactionDAO = raddTransactionDAO;
        this.safeStorageClient = safeStorageClient;
        this.pnDeliveryInternalClient = pnDeliveryInternalClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
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
                .zipWhen(req -> this.raddTransactionDAO.getTransaction(req.getOperationId()), (request, entity) -> entity)
                .zipWhen(this.pnDeliveryPushClient::notifyNotificationViewed, (entity, response) -> entity)
                .zipWhen(entity -> {
                    entity.setStatus("COMPLETED");
                    return this.raddTransactionDAO.updateStatus(entity);
                }).map(tupla -> {
                    return new CompleteTransactionResponse();
                });
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
            if (Strings.isBlank(response.getDocumentStatus()) || !response.getDocumentStatus().equals("PRELOADED")){
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
        entity.setStatus("STARTED");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        entity.setOperationStartDate(simpleDateFormat.format(new Date()));
        return this.raddTransactionDAO.createRaddTransaction(entity);
    }


    private Mono<EnsureFiscalCode> getEnsureRecipientAndDelegate(ActStartTransactionRequest request){
        return getEnsureFiscalCode(request.getRecipientTaxId())
                .flatMap(ensureRecipient -> {
                    if (!Strings.isBlank(request.getDelegateTaxId())){
                        return getEnsureFiscalCode(request.getDelegateTaxId())
                                .flatMap(delegateEnsure -> Mono.just(new EnsureFiscalCode(ensureRecipient, delegateEnsure)));
                    }
                    return  Mono.just(new EnsureFiscalCode(ensureRecipient, null));
                });
    }

    private Mono<String> getEnsureFiscalCode(String fiscalCode){
        return this.pnDataVaultClient.getEnsureFiscalCode(fiscalCode)
                .map(response -> {

                    if (response == null || Strings.isBlank(response)){
                        throw new RaddFiscalCodeEnsureException();
                    }
                    return response;
                }).onErrorResume(Mono::error);
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
}
