package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.mapper.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
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
import reactor.core.publisher.ParallelFlux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.RETRY_AFTER;

@Slf4j
@Service
public class AorService extends BaseService {
    private final PnDeliveryPushClient pnDeliveryPushClient;
    private final TransactionDataMapper transactionDataMapper;

    public AorService(PnDeliveryPushClient pnDeliveryPushClient, PnDataVaultClient pnDataVaultClient, PnSafeStorageClient pnSafeStorageClient,
                      TransactionDataMapper transactionDataMapper, RaddTransactionDAO raddTransactionDAO) {
        super(pnDataVaultClient, raddTransactionDAO, pnSafeStorageClient);
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.transactionDataMapper = transactionDataMapper;
    }

    public Mono<AORInquiryResponse> aorInquiry(String uid, String recipientTaxId, String recipientType){
        if (StringUtils.isBlank(recipientTaxId)){
            throw new PnInvalidInputException("Il campo codice fiscale non è valorizzato");
        }
        log.info("Start AORInquiry - uid={}", uid);
        return this.getEnsureFiscalCode(recipientTaxId, recipientType)
                .flatMap(ensureFiscalCode ->
                        this.getIunFromPaperNotificationFailed(ensureFiscalCode)
                                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF)))
                                .collectList()
                                .map(list -> {
                                    log.info("End of AORInquiry with documents list size {}", list.size());
                                    return AorInquiryResponseMapper.fromResult();
                                })
                )
                .onErrorResume(RaddGenericException.class, ex -> {
                    log.debug("End of AORInquiry with error {}", ex.getMessage(), ex);
                    return Mono.just(AorInquiryResponseMapper.fromException(ex));
                });
    }


    public Mono<CompleteTransactionResponse> completeTransaction(String uid, Mono<CompleteTransactionRequest> completeTransactionRequest) {
        log.info("Start AOR complete transaction - uid={}", uid);
        return completeTransactionRequest.map(this::validateCompleteRequest)
                .zipWhen(req -> this.getAndCheckStatusTransaction(req.getOperationId()))
                .map(reqAndEntity -> {
                    RaddTransactionEntity entity = reqAndEntity.getT2();
                    entity.setOperationEndDate(DateUtils.formatDate(reqAndEntity.getT1().getOperationDate()));
                    entity.setUid(uid);
                    entity.setStatus(Const.COMPLETED);
                    return entity;
                })
                .doOnNext(entity -> log.debug("[uid={} - operationId={}] Updating transaction entity with status {}", entity.getUid(), entity.getOperationId(), entity.getStatus()))
                .flatMap(raddTransactionDAO::updateStatus)
                .doOnNext(entity -> log.debug("[uid={} - operationId={}] New status of transaction entity is {}", entity.getUid(), entity.getOperationId(), entity.getStatus()))
                .map(entity -> CompleteTransactionResponseMapper.fromResult())
                .onErrorResume(RaddGenericException.class, ex -> Mono.just(CompleteTransactionResponseMapper.fromException(ex)));
    }

    public Mono<StartTransactionResponse> startTransaction(String uid, AorStartTransactionRequest request){
        log.info("Start AOR startTransaction - uid={} - operationId={}", uid, request.getOperationId());
        return validationAorStartTransaction(uid, request)
                .zipWhen(this::getEnsureRecipientAndDelegate, (transaction, transactionReq) -> transactionReq)
                .zipWhen(transaction -> this.getIunFromPaperNotificationFailed(transaction.getEnsureRecipientId())
                                .map(item -> {
                                    log.debug("Retrieved IUN : {}", item.getIun());
                                    transaction.getIuns().add(item.getIun());
                                    transaction.getUrls().add(item.getAarUrl());
                                    return item;
                                })
                            .collectList().map(list -> transaction), (transaction, transactionWithIuns) -> transactionWithIuns)
                .flatMap(transaction -> this.createAorTransaction(uid, transaction))
                .flatMap(this::verifyCheckSum)
                .flatMap(transactionData ->
                    this.getPresignedUrls(transactionData.getUrls()).sequential().collectList()
                            .map(urls -> {
                                transactionData.setUrls(urls);
                                return transactionData;
                            })
                )
                .doOnNext(transactionData -> log.debug("Update file metadata"))
                .flatMap(this::updateFileMetadata)
                .doOnNext(transactionData -> log.debug("End AOR start transaction"))
                .map(data -> StartTransactionResponseMapper.fromResult(data.getUrls()))
                .onErrorResume(RaddGenericException.class, ex -> {
                    log.error("End AOR start transaction with error {}", ex.getMessage(), ex);
                    return this.settingErrorReason(ex, request.getOperationId(), OperationTypeEnum.AOR)
                                    .flatMap(entity -> Mono.just(StartTransactionResponseMapper.fromException(ex)));
                });
    }

    public ParallelFlux<String> getPresignedUrls(List<String> listFileKey) {
        return Flux.fromStream(listFileKey.stream())
                .flatMap(this.safeStorageClient::getFile)
                .parallel()
                .map(file -> {
                    if (file.getDownload() != null && file.getDownload().getRetryAfter() != null && file.getDownload().getRetryAfter().intValue() != 0) {
                        log.info("Finded document with retry after {}", file.getDownload().getRetryAfter());
                        throw new RaddGenericException(RETRY_AFTER, file.getDownload().getRetryAfter());
                    }
                    if (file.getDownload() != null) {
                        log.info("File : {}", file.getVersionId());
                        log.info("URL : {}", file.getDownload().getUrl());
                        return file.getDownload().getUrl();
                    }
                    return "";
                });
    }

    private Mono<TransactionData> createAorTransaction(String uid, TransactionData transaction){
        List<OperationsIunsEntity> raddOperationIunList = new ArrayList<>();
        if (transaction.getIuns() != null){
            raddOperationIunList = transaction.getIuns().parallelStream().map(iun -> {
                OperationsIunsEntity operationIun = new OperationsIunsEntity();
                operationIun.setOperationId(transaction.getOperationId());
                operationIun.setIun(iun);
                operationIun.setId(UUID.randomUUID().toString());
                return operationIun;
            }).toList();
        }
        RaddTransactionEntity entity = transactionDataMapper.toEntity(uid, transaction);
        log.debug("Create new Transaction entity iun={}, status={}", entity.getIun(), entity.getStatus());
        return this.raddTransactionDAO.createRaddTransaction(entity, raddOperationIunList).map(ent -> transaction);
    }

    private CompleteTransactionRequest validateCompleteRequest(CompleteTransactionRequest req){
        if (StringUtils.isEmpty(req.getOperationId())){
            throw new PnInvalidInputException("Operation id non valorizzato");
        }
        return req;
    }



    public Mono<AbortTransactionResponse> abortTransaction(String uid, Mono<AbortTransactionRequest> monoAbortTransactionRequest){
        return monoAbortTransactionRequest
                .filter(isValidAbortTransactionRequest)
                .switchIfEmpty(Mono.error( new PnInvalidInputException("Alcuni paramentri come operazione id o data di operazione non sono valorizzate")))
                .doOnNext(m -> log.info("Start AOR abort transaction - uid={} - operationId={}", uid, m.getOperationId()))

                .zipWhen(operation -> raddTransactionDAO.getTransaction(operation.getOperationId(), OperationTypeEnum.AOR))
                .map(entity -> {
                    RaddTransactionEntity raddEntity = entity.getT2();
                    checkTransactionStatus(raddEntity);
                    raddEntity.setUid(uid);
                    raddEntity.setErrorReason(entity.getT1().getReason());
                    raddEntity.setOperationEndDate(DateUtils.formatDate(entity.getT1().getOperationDate()));
                    raddEntity.setStatus(Const.ABORTED);
                    return raddEntity;
                })
                .flatMap(raddTransactionDAO::updateStatus)
                .doOnNext(raddTransaction -> log.debug("End AOR abortTransaction with entity status {}", raddTransaction.getStatus()))
                .map(result -> AbortTransactionResponseMapper.fromResult())
                .onErrorResume(RaddGenericException.class, ex -> {
                    log.error("Errore AOR Abort transaction {}", ex.getMessage(), ex);
                    return Mono.just(AbortTransactionResponseMapper.fromException(ex));
                });
    }

    private Mono<TransactionData> validationAorStartTransaction(String uid, AorStartTransactionRequest req){
        if (Strings.isBlank(req.getOperationId())){
            return Mono.error(new PnInvalidInputException("Id operazione non valorizzato"));
        }
        if (Strings.isBlank(req.getRecipientTaxId())){
            return Mono.error(new PnInvalidInputException("Codice fiscale non valorizzato"));
        }
        if (req.getRecipientType() == null || !Utils.checkPersonType(req.getRecipientType().getValue())){
            return Mono.error(new PnInvalidInputException("Recipient Type non valorizzato correttamente"));
        }
        return Mono.just(this.transactionDataMapper.toTransaction(uid, req));
    }

    private Flux<ResponsePaperNotificationFailedDtoDto> getIunFromPaperNotificationFailed(String recipientTaxId){
        return this.pnDeliveryPushClient.getPaperNotificationFailed(recipientTaxId)
                .filter(item -> StringUtils.equalsIgnoreCase(recipientTaxId, item.getRecipientInternalId()))
                .onErrorResume(NullPointerException.class, ex -> Mono.error(new RaddGenericException(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED)));
    }

    private Mono<RaddTransactionEntity> getAndCheckStatusTransaction(String operationId){
        return raddTransactionDAO.getTransaction(operationId, OperationTypeEnum.AOR)
                .doOnNext(raddTransaction -> log.debug("[{}] Check status entity : {}", operationId, raddTransaction.getStatus()))
                .doOnNext(this::checkTransactionStatus);
    }

    private final Predicate<AbortTransactionRequest> isValidAbortTransactionRequest = m -> (
            m != null && !StringUtils.isEmpty(m.getOperationId()) && !StringUtils.isEmpty(m.getReason())
                    && m.getOperationDate() != null
    );

}
