package it.pagopa.pn.radd.services.radd.fsu.v1;


import it.pagopa.pn.radd.mapper.RaddTransactionEntityNotificationResponse;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.rest.radd.v1.dto.NotificationPracticesResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.NotificationResponse;
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


    public Mono<NotificationResponse> getTransaction(String operationId){
        log.info("Find transaction with {} operation id", operationId);
        return transactionDAO.getTransaction(operationId)
                .map(mapperToNotificationResponse::toDto);
    }

    public Mono<NotificationPracticesResponse> getPracticesId(String iun){
        return transactionDAO.getTransactionsFromIun(iun).map(RaddTransactionEntity::getOperationId).collectList().map(operationsId -> {
            NotificationPracticesResponse notificationPracticesResponse = new NotificationPracticesResponse();
            notificationPracticesResponse.setData(operationsId);
            return notificationPracticesResponse;
        });
    }


}
