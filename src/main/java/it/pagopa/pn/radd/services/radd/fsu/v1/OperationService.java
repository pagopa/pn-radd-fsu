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
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.TRANSACTIONS_NOT_FOUND_FOR_CF;

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

    public Mono<OperationsActDetailsResponse> getAllActTransactionFromFiscalCode(String ensureFiscalCode){
        return this.transactionDAO.getTransactionsFromFiscalCode(ensureFiscalCode)
                .filter(transactionEntity -> transactionEntity.getOperationType().equals(OperationTypeEnum.ACT.name()))
                .map(OperationActResponseMapper::getDetail)
                .collectList()
                .map(operationsList -> {
                    OperationsActDetailsResponse response = new OperationsActDetailsResponse();
                    response.setElements(operationsList);
                    OperationResponseStatus status = new OperationResponseStatus();
                    response.setStatus(status);
                    if (operationsList.isEmpty()){
                        response.setResult(false);
                        status.setCode(OperationResponseStatus.CodeEnum.NUMBER_1);
                        status.setMessage(TRANSACTIONS_NOT_FOUND_FOR_CF.getMessage());
                    } else{
                        response.setResult(true);
                        status.setCode(OperationResponseStatus.CodeEnum.NUMBER_0);
                        status.setMessage(Const.OK);
                    }
                    return response;
                })
                .onErrorResume(e ->{
                    OperationsActDetailsResponse response = new OperationsActDetailsResponse();
                    OperationResponseStatus status = new OperationResponseStatus();
                    response.setStatus(status);
                    response.setResult(false);
                    status.setCode(OperationResponseStatus.CodeEnum.NUMBER_99);
                    status.setMessage(e.getMessage());
                    return Mono.just(response);
                });
    }

    public Mono<OperationsAorDetailsResponse> getAllAorTransactionFromFiscalCode(String ensureFiscalCode){
        return this.transactionDAO.getTransactionsFromFiscalCode(ensureFiscalCode)
                .filter(transactionEntity -> transactionEntity.getOperationType().equals(OperationTypeEnum.AOR.name()))
                .map(OperationAorResponseMapper::getDetail)
                .collectList()
                .map(operationsList -> {
                    OperationsAorDetailsResponse response = new OperationsAorDetailsResponse();
                    response.setElements(operationsList);
                    OperationResponseStatus status = new OperationResponseStatus();
                    response.setStatus(status);
                    if (operationsList.isEmpty()){
                        response.setResult(false);
                        status.setCode(OperationResponseStatus.CodeEnum.NUMBER_1);
                        status.setMessage(TRANSACTIONS_NOT_FOUND_FOR_CF.getMessage());
                    } else{
                        response.setResult(true);
                        status.setCode(OperationResponseStatus.CodeEnum.NUMBER_0);
                        status.setMessage(Const.OK);
                    }
                    return response;
                }).onErrorResume(e ->{
                    OperationsAorDetailsResponse response = new OperationsAorDetailsResponse();
                    OperationResponseStatus status = new OperationResponseStatus();
                    response.setStatus(status);
                    response.setResult(false);
                    status.setCode(OperationResponseStatus.CodeEnum.NUMBER_99);
                    status.setMessage(e.getMessage());
                    return Mono.just(response);
                });
    }


}
