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
import java.util.stream.Collectors;

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
        return  this.getEnsureFiscalCode(recipientTaxId, recipientType)
                .zipWhen(ensureFiscalCode ->
                        this.getIunFromPaperNotificationFailed(ensureFiscalCode)
                                .collectList()
                                .map(list -> {
                                    if (list.isEmpty()){
                                        throw new RaddGenericException(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF);
                                    }
                                    return AorInquiryResponseMapper.fromResult();
                                }), (ensure, resp) -> resp
                )
                .onErrorResume(RaddGenericException.class, ex -> Mono.just(AorInquiryResponseMapper.fromException(ex)));
    }

    public Mono<CompleteTransactionResponse> completeTransaction(String uid, Mono<CompleteTransactionRequest> completeTransactionRequest) {
        return completeTransactionRequest.map(this::validateCompleteRequest)
                .zipWhen(req -> this.raddTransactionDAO.getTransaction(req.getOperationId(), OperationTypeEnum.AOR)
                        .map(entity -> {
                            checkTransactionStatus(entity);
                            return entity;
                        }))
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

    public Mono<StartTransactionResponse> startTransaction(String uid, AorStartTransactionRequest request){
        return validationAorStartTransaction(uid, request)
                .zipWhen(this::getEnsureRecipientAndDelegate, (transaction, transactionReq) -> transactionReq)
                .zipWhen(transaction -> this.getIunFromPaperNotificationFailed(transaction.getEnsureRecipientId())
                                .map(item -> {
                                    transaction.getIuns().add(item.getIun());
                                    transaction.getUrls().add(item.getAarUrl());
                                    return item;
                                }).collectList().map(list -> transaction), (transaction, transactionWithIuns) -> transactionWithIuns)
                .flatMap(transaction -> this.createAorTransaction(uid, transaction))
                .flatMap(this::verifyCheckSum)
                .flatMap(this::updateFileMetadata)
                .flatMap(transactionData ->
                    this.getPresignedUrls(transactionData.getUrls())
                            .sequential()
                            .collectList()
                            .map(StartTransactionResponseMapper::fromResult)
                )
                .onErrorResume(RaddGenericException.class, ex ->
                        this.settingErrorReason(ex, request.getOperationId(), OperationTypeEnum.AOR)
                                .flatMap(entity -> Mono.just(StartTransactionResponseMapper.fromException(ex)))
                );
    }

    public ParallelFlux<String> getPresignedUrls(List<String> listFileKey) {
        return Flux.fromStream(listFileKey.stream())
                .flatMap(this.safeStorageClient::getFile)
                .parallel()
                .map(file -> {
                    if (file.getDownload() != null && file.getDownload().getRetryAfter() != null && file.getDownload().getRetryAfter().intValue() != 0) {
                        log.info("Finded legal fact with retry after {}", file.getDownload().getRetryAfter());
                        throw new RaddGenericException(RETRY_AFTER, file.getDownload().getRetryAfter());
                    }
                    if (file.getDownload() != null) {
                        log.info("FIle : {}", file.getVersionId());
                        log.info("URL : {}", file.getDownload().getUrl());
                        return file.getDownload().getUrl();
                    }
                    return "";
                });
    }

    private Mono<TransactionData> createAorTransaction(String uid, TransactionData transaction){
        List<OperationsIunsEntity> raddOperationIunList = new ArrayList<>();
        if (transaction.getIuns() != null){
            raddOperationIunList = transaction.getIuns().stream().map(iun -> {
                OperationsIunsEntity operationIun = new OperationsIunsEntity ();
                operationIun.setOperationId(transaction.getOperationId());
                operationIun.setIun(iun);
                operationIun.setId(UUID.randomUUID().toString());
                return operationIun;
            }).collect(Collectors.toList());
        }
        RaddTransactionEntity entity = transactionDataMapper.toEntity(uid, transaction);
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
                .map(m -> {
                    if (m == null || StringUtils.isEmpty(m.getOperationId())
                            || StringUtils.isEmpty(m.getReason())
                            || m.getOperationDate() == null) {
                        log.error("Missing input parameters");
                        throw new PnInvalidInputException("Alcuni paramentri come operazione id o data di operazione non sono valorizzate");
                    }
                    return m;
                })
                .zipWhen(operation -> raddTransactionDAO.getTransaction(operation.getOperationId(), OperationTypeEnum.AOR))
                .map(entity -> {
                    RaddTransactionEntity raddEntity = entity.getT2();
                    checkTransactionStatus(raddEntity);
                    raddEntity.setUid(uid);
                    raddEntity.setErrorReason(entity.getT1().getReason());
                    raddEntity.setOperationEndDate(DateUtils.formatDate(entity.getT1().getOperationDate()));
                    raddEntity.setStatus(Const.ABORTED);
                    return raddTransactionDAO.updateStatus(raddEntity);
                })
                .map(result -> {
                    log.info("Return result of abort transaction");
                    return AbortTransactionResponseMapper.fromResult();
                })
                .onErrorResume(RaddGenericException.class,
                        ex -> Mono.just(AbortTransactionResponseMapper.fromException(ex)));
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

}
