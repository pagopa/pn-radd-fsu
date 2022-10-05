package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.AORInquiryResponseMapper;
import it.pagopa.pn.radd.mapper.AbortTransactionResponseMapper;
import it.pagopa.pn.radd.mapper.CompleteTransactionResponseMapper;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.TRANSACTION_ALREADY_ABORTED;
import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.TRANSACTION_ALREADY_COMPLETED;

@Slf4j
@Service
public class AorService extends BaseService {

    private final PnDeliveryPushClient pnDeliveryPushClient;

    private final RaddTransactionDAO raddTransactionDAO;

    public AorService(PnDeliveryPushClient pnDeliveryPushClient, RaddTransactionDAO raddTransactionDAO) {
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.raddTransactionDAO = raddTransactionDAO;
    }

    public Mono<AORInquiryResponse> aorInquiry(String uid, String recipientTaxId,
                                               String recipientType){
        if (StringUtils.isBlank(recipientTaxId)){
            throw new PnInvalidInputException("Il campo codice fiscale non è valorizzato");
        }
        return this.pnDeliveryPushClient.getPaperNotificationFailed(recipientTaxId).collectList()
                .map(listNotification -> {

                    if (listNotification == null){
                        throw new RaddGenericException(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED, ExceptionCodeEnum.KO);
                    }

                    List< ResponsePaperNotificationFailedDtoDto> filter = listNotification
                            .stream()
                            .filter(item -> StringUtils.equals(item.getRecipientInternalId(), recipientTaxId))
                            .collect(Collectors.toList());
                    if (filter.isEmpty()) {
                        throw new RaddGenericException(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILDE_FOR_CF, ExceptionCodeEnum.KO);
                    }
                    return AORInquiryResponseMapper.fromResult();
                }).onErrorResume(RaddGenericException.class, ex -> Mono.just(AORInquiryResponseMapper.fromException(ex)));
    }

    public Mono<CompleteTransactionResponse> completeTransaction(String uid, Mono<CompleteTransactionRequest> completeTransactionRequest) {
        return completeTransactionRequest.map(this::validateCompleteRequest)
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
                })
                .map(entity -> CompleteTransactionResponseMapper.fromResult())
                .onErrorResume(RaddGenericException.class, ex -> Mono.just(CompleteTransactionResponseMapper.fromException(ex)));
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
                //todo : capire se viene sempre estratto il valore corrispondente al campo "type" da DB.
                //todo: La transaction estratta deve avere un type corrispondente a "aor" e non a "act"
                .zipWhen(operation -> raddTransactionDAO.getTransaction(operation.getOperationId()))
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


    private void checkTransactionStatus(RaddTransactionEntity entity) {
        if (StringUtils.equals(entity.getStatus(), Const.COMPLETED)) {
            throw new RaddGenericException(TRANSACTION_ALREADY_COMPLETED, ExceptionCodeEnum.NUMBER_2);
        } else if (StringUtils.equals(entity.getStatus(), Const.ABORTED)){
            throw new RaddGenericException(TRANSACTION_ALREADY_ABORTED, ExceptionCodeEnum.NUMBER_2);
        }
    }

}
