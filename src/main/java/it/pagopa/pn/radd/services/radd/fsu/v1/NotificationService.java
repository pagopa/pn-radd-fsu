package it.pagopa.pn.radd.services.radd.fsu.v1;


import it.pagopa.pn.radd.mapper.RaddTransactionEntityNotificationResponse;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationResponseStatus;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class NotificationService {

    private final RaddTransactionDAO transactionDAO;
    private final RaddTransactionEntityNotificationResponse mapperToNotificationResponse;


    public NotificationService(RaddTransactionDAO transactionDAO, RaddTransactionEntityNotificationResponse mapperToNotificationResponse) {
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
                    status.setCode(OperationResponseStatus.CodeEnum.NUMBER_1);
                    response.setStatus(status);
                    return response;
                });
    }

    public Mono<OperationsResponse> getPracticesId(String iun){
        return transactionDAO.getTransactionsFromIun(iun).map(RaddTransactionEntity::getOperationId).collectList().map(operationsId -> {
            OperationsResponse notificationPracticesResponse = new OperationsResponse();
            notificationPracticesResponse.setOperationIds(operationsId);
            notificationPracticesResponse.setResult(true);
            OperationResponseStatus status = new OperationResponseStatus();
            status.setCode(OperationResponseStatus.CodeEnum.NUMBER_1);
            notificationPracticesResponse.setStatus(status);
            return notificationPracticesResponse;
        });
    }


}
