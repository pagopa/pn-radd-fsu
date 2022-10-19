package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.OperationActResponseMapper;
import it.pagopa.pn.radd.mapper.OperationAorResponseMapper;
import it.pagopa.pn.radd.mapper.OperationsResponseMapper;
import it.pagopa.pn.radd.mapper.RaddTransactionEntityNotificationResponse;
import it.pagopa.pn.radd.middleware.db.OperationsIunsDAO;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OperationService {

    private final RaddTransactionDAO transactionDAO;
    private final OperationsIunsDAO operationsIunsDAO;
    private final RaddTransactionEntityNotificationResponse mapperToNotificationResponse;


    public OperationService(RaddTransactionDAO transactionDAO, OperationsIunsDAO operationsIunsDAO, RaddTransactionEntityNotificationResponse mapperToNotificationResponse) {
        this.transactionDAO = transactionDAO;
        this.operationsIunsDAO = operationsIunsDAO;
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

    public Mono<OperationsResponse> getOperationsActByIun(String iun){
        return transactionDAO.getTransactionsFromIun(iun)
                .map(RaddTransactionEntity::getOperationId)
                .collectList()
                .map(OperationsResponseMapper::fromResult);
    }

    public Mono<OperationsResponse> getOperationsAorByIun(String iun){
        return operationsIunsDAO.getAllOperationFromIun(iun)
                .map(OperationsIunsEntity::getOperationId)
                .collectList()
                .map(OperationsResponseMapper::fromResult);
    }


}
