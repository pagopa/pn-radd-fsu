package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.RaddTransactionEntityNotificationResponse;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OperationService {

    private final RaddTransactionDAO transactionDAO;
    private final RaddTransactionEntityNotificationResponse mapperToNotificationResponse;


    public OperationService(RaddTransactionDAO transactionDAO, RaddTransactionEntityNotificationResponse mapperToNotificationResponse) {
        this.transactionDAO = transactionDAO;
        this.mapperToNotificationResponse = mapperToNotificationResponse;
    }


    public Mono<OperationResponse> getTransaction(String operationId){
        log.info("Find transaction with {} operation id", operationId);
        return transactionDAO.getTransaction(operationId)
                .map(entity -> {
                    OperationResponse response = new OperationResponse();
                    response.setElement(mapperToNotificationResponse.toDto(entity));
                    response.setResult(true);
                    OperationResponseStatus status = new OperationResponseStatus();
                    status.setCode(OperationResponseStatus.CodeEnum.NUMBER_0);
                    response.setStatus(status);
                    return response;
                }).onErrorResume(ex -> {
                    if (ex instanceof RaddTransactionNoExistedException) {
                        return Mono.just(getOperationErrorResponse(ex));
                    }
                    return Mono.error(ex);
                });
    }

    public Mono<OperationsResponse> getPracticesId(String iun){
        return transactionDAO.getTransactionsFromIun(iun)
                .map(RaddTransactionEntity::getOperationId)
                .collectList()
                .map(operationsId -> {
                    OperationsResponse notificationPracticesResponse = new OperationsResponse();
                    OperationResponseStatus status = new OperationResponseStatus();
                    if (operationsId.isEmpty()){
                        status.setCode(OperationResponseStatus.CodeEnum.NUMBER_1);
                        status.setMessage("Non ci sono operation id");
                        notificationPracticesResponse.setResult(false);
                    } else {
                        status.setCode(OperationResponseStatus.CodeEnum.NUMBER_0);
                        notificationPracticesResponse.setOperationIds(operationsId);
                        notificationPracticesResponse.setResult(true);
                    }
                    notificationPracticesResponse.setStatus(status);
                    return notificationPracticesResponse;
                });
    }

    protected OperationResponse getOperationErrorResponse(Throwable ex) {
        OperationResponse r = new OperationResponse();
        OperationResponseStatus status = new OperationResponseStatus();
        status.setMessage(ex.getMessage());
        status.setCode(OperationResponseStatus.CodeEnum.NUMBER_1);
        r.setStatus(status);
        return r;
    }


}
