package it.pagopa.pn.radd.middleware.db.impl;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.OperationsIunsDAO;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.pojo.RaddTransactionStatusEnum;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.DateUtils;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DATE_VALIDATION_ERROR;

@Repository
@Slf4j
public class RaddTransactionDAOImpl extends BaseDao<RaddTransactionEntity> implements RaddTransactionDAO {
    private final OperationsIunsDAO operationsIunsDAO;

    public RaddTransactionDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                  DynamoDbAsyncClient dynamoDbAsyncClient,
                                  OperationsIunsDAO operationsIunsDAO,
                                  PnRaddFsuConfig pnRaddFsuConfig,
                                  AwsConfigs awsConfigs) {
        super(dynamoDbEnhancedAsyncClient,
                dynamoDbAsyncClient,
                awsConfigs.getDynamodbTable(),
                pnRaddFsuConfig,
                RaddTransactionEntity.class);
        this.operationsIunsDAO = operationsIunsDAO;
    }

    @Override
    public Mono<RaddTransactionEntity> createRaddTransaction(RaddTransactionEntity entity, List<OperationsIunsEntity> iunsEntities){
        return putTransaction(entity)
                .flatMap(raddTransaction -> operationsIunsDAO.putWithBatch(iunsEntities)
                        .thenReturn(entity))
                .flatMap(raddTransaction -> updateStatus(raddTransaction, RaddTransactionStatusEnum.STARTED));
    }


    private Mono<RaddTransactionEntity> putTransaction(RaddTransactionEntity entity) {
        return this.countFromIunAndOperationIdAndStatus(entity.getOperationId(), entity.getIun())
                .filter(item -> item == 0)
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_ALREADY_EXIST)))
                .doOnNext(item -> log.trace("PUT RADD TRANSACTION TICK {}", new Date().getTime()))
                .flatMap(counter -> this.putItem(entity)
                        .doOnError(ex -> log.debug("[{} - {}] Error during creation radd transaction", entity.getOperationId(), entity.getIun()))
                        .doOnError(ex -> log.trace("PUT RADD TRANSACTION TOCK {}", new Date().getTime()))
                        .onErrorResume(ex -> Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_SAVED)))
                )
                .doOnNext(raddTransaction -> log.debug("[{} - {}] radd transaction created", raddTransaction.getOperationId(), raddTransaction.getIun()))
                .doOnNext(raddTransaction -> log.trace("PUT RADD TRANSACTION TOCK {}", new Date().getTime()));
    }


    @Override
    public Mono<RaddTransactionEntity> getTransaction(String operationId, OperationTypeEnum operationType) {
        Key key = Key.builder()
                    .partitionValue(operationId)
                    .sortValue(operationType.name())
                    .build();
        return this.findFromKey(key)
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST)));
    }

    @Override
    public Mono<RaddTransactionEntity> updateStatus(RaddTransactionEntity entity, RaddTransactionStatusEnum status) {
        entity.setStatus(status.name());
        return this.updateItem(entity)
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST)))
                .filter(updated -> updated.getStatus().equals(entity.getStatus()))
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS)));
    }

    @Override
    public Mono<Integer> countFromIunAndOperationIdAndStatus(String operationId, String iun) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        String query = "(" + RaddTransactionEntity.COL_STATUS + " = :completed" + " OR " +
                RaddTransactionEntity.COL_STATUS + " = :aborted )" +
                " AND ( " + RaddTransactionEntity.COL_IUN + " = :iun)";

        expressionValues.put(":iun", AttributeValue.builder().s(iun).build());
        expressionValues.put(":operationId",  AttributeValue.builder().s(operationId).build());
        expressionValues.put(":completed",  AttributeValue.builder().s(Const.COMPLETED).build());
        expressionValues.put(":aborted",  AttributeValue.builder().s(Const.ABORTED).build());

        log.trace("COUNT DAO TICK {}", new Date().getTime());

        return this.getCounterQuery(expressionValues, query, RaddTransactionEntity.COL_OPERATION_ID + " = :operationId")
                .doOnNext(response -> log.trace("COUNT DAO TOCK {}", new Date().getTime()));
    }

    @Override
    public Mono<Integer> countFromQrCodeCompleted(String qrCode) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        String query = "(" + RaddTransactionEntity.COL_STATUS + " = :completed AND " + RaddTransactionEntity.COL_OPERATION_TYPE +  " = :type)";
        expressionValues.put(":completed", AttributeValue.builder().s(Const.COMPLETED).build());
        expressionValues.put(":type", AttributeValue.builder().s(OperationTypeEnum.ACT.name()).build());
        expressionValues.put(":qrcodevalue",  AttributeValue.builder().s(qrCode).build());

        log.trace("COUNT QUERY DAO TICK {}", new Date().getTime());

        return this.getCounterQuery(expressionValues, query, RaddTransactionEntity.COL_QR_CODE + " = :qrcodevalue")
                .doOnNext(result ->  log.trace("COUNT QUERY DAO TOCK {}", new Date().getTime()))
                .doOnError(ex ->  log.trace("COUNT QUERY DAO TOCK {}", new Date().getTime()));
    }


    @Override
    public Flux<RaddTransactionEntity> getTransactionsFromIun(String iun) {
        Key key = Key.builder().partitionValue(iun).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        String index = RaddTransactionEntity.IUN_SECONDARY_INDEX;
        return this.getByFilter(conditional, index, null, null, null);
    }

    @Override
    public Flux<RaddTransactionEntity> getTransactionsFromFiscalCode(String ensureFiscalCode, Date from, Date to) {
        if (from != null && to != null && from.after(to)){
            return Flux.error(new RaddGenericException(DATE_VALIDATION_ERROR));
        }

        Map<String, AttributeValue> map = new HashMap<>();
        String query = "";

        if (from != null) {
            map.put(":fromDate", AttributeValue.builder().s(DateUtils.formatDate(from)).build());
            query = RaddTransactionEntity.COL_OPERATION_START_DATE + " >= :fromDate";
        }

        if (from != null && to != null) {
            map.put(":toDate", AttributeValue.builder().s(DateUtils.formatDate(to)).build());
            query += " AND " + RaddTransactionEntity.COL_OPERATION_START_DATE + " <= :toDate";
        }

        Key key = Key.builder().partitionValue(ensureFiscalCode).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        String indexRecipient = RaddTransactionEntity.RECIPIENT_SECONDARY_INDEX;
        String indexDelegate = RaddTransactionEntity.DELEGATE_SECONDARY_INDEX;

        return this.getByFilter(conditional, indexRecipient, map, query, null)
                .concatWith(this.getByFilter(conditional, indexDelegate, map, query, null))
                .distinct(RaddTransactionEntity::getOperationId);
    }

}
