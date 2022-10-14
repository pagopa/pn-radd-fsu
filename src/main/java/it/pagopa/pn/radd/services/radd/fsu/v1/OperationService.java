package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.OperationActResponseMapper;
import it.pagopa.pn.radd.mapper.OperationAorResponseMapper;
import it.pagopa.pn.radd.mapper.RaddTransactionEntityNotificationResponse;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
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


    public Mono<OperationActResponse> getTransactionActByOperationIdAndType(String operationId){
        log.info("Find transaction with {} operation id", operationId);
        return transactionDAO.getTransaction(operationId, OperationTypeEnum.ACT)
                .map(entity ->
                        OperationActResponseMapper.fromResult(
                                mapperToNotificationResponse.toDto(entity)
                        )
                )
                .onErrorResume(RaddGenericException.class, ex -> Mono.just(OperationActResponseMapper.fromException(ex)));
    }

    public Mono<OperationAorResponse> getTransactionAorByOperationIdAndType(String operationId){
        log.info("Find transaction with {} operation id", operationId);
        return transactionDAO.getTransaction(operationId, OperationTypeEnum.AOR)
                .map(OperationAorResponseMapper::fromResult)
                .onErrorResume(RaddGenericException.class, ex -> Mono.just(OperationAorResponseMapper.fromException(ex)));
    }

    public Mono<OperationsResponse> getTransactionActByIun(String iun){
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
                        status.setMessage(Const.OK);
                        notificationPracticesResponse.setResult(true);
                    }
                    notificationPracticesResponse.setStatus(status);
                    return notificationPracticesResponse;
                });
    }


}
