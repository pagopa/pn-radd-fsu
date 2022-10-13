package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.RaddOperationIun;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
@Slf4j
@Import(PnAuditLogBuilder.class)
public class RaddTransactionDAO extends BaseDao {

    private final PnAuditLogBuilder auditLogBuilder;
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    DynamoDbAsyncClient dynamoDbAsyncClient;
    DynamoDbAsyncTable<RaddTransactionEntity> raddTable;
    DynamoDbAsyncTable<RaddOperationIun> raddOperationIunTable;
    String table;


    public RaddTransactionDAO(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                              DynamoDbAsyncClient dynamoDbAsyncClient,
                              AwsConfigs awsConfigs,
                              PnAuditLogBuilder pnAuditLogBuilder) {
        this.raddTable = dynamoDbEnhancedAsyncClient.table(awsConfigs.getDynamodbTable(), TableSchema.fromBean(RaddTransactionEntity.class));
        this.raddOperationIunTable = dynamoDbEnhancedAsyncClient.table(awsConfigs.getOperationIunDynamodbTable(), TableSchema.fromBean(RaddOperationIun.class));
        this.table = awsConfigs.getDynamodbTable();
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.auditLogBuilder = pnAuditLogBuilder;
    }

    public Mono<RaddTransactionEntity> createRaddTransaction(RaddTransactionEntity entity, List<RaddOperationIun> entityIuns){
        String logMessage = String.format("create Radd Transaction =%s", entity);
        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_DL_CREATE, logMessage)
                .uid(entity.getUid())
                .build();
        logEvent.log();
        return Mono.fromFuture(
                countFromIunAndOperationIdAndStatus(entity.getOperationId(), entity.getIun())
                        .thenCompose(total -> {
                            if (total == 0) {
                                log.info("no current transaction for delegator-delegate pair, can proceed to create transaction");
                                try {
                                    TransactWriteItemsEnhancedRequest transactRequest = createTransaction(entity, entityIuns);
                                    return dynamoDbEnhancedAsyncClient.transactWriteItems(transactRequest)
                                                .thenApply(item -> {
                                                    log.info("Created");
                                                    return entity;
                                                });
                                } catch (TransactionCanceledException ex) {
                                    throw new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_SAVED);
                                }
                            }
                            else {
                                throw new RaddGenericException(ExceptionTypeEnum.TRANSACTION_ALREADY_EXIST);
                            }
                        })
        ).onErrorResume(throwable -> {
            logEvent.generateFailure(throwable.getMessage()).log();
            return Mono.error(throwable);
        })
        .map(item -> {
            log.info("Radd transaction object={}", item);
            logEvent.generateSuccess(String.format("created Radd transaction object=%s", item)).log();

            return item;
        });
    }

    public Mono<RaddTransactionEntity> getTransaction(String operationId, OperationTypeEnum operationType) {
        Key key = Key.builder().partitionValue(operationId).sortValue(operationType.name()).build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();

        return Mono.fromFuture(raddTable.getItem(request).thenApply(item -> {
            log.info("Item finded : {}", item);
            if (item == null) {
                throw new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST);
            }
            return item;
        }));
    }

    public Mono<RaddTransactionEntity> updateStatus(RaddTransactionEntity entity){
        String logMessage = String.format("Update Radd Transaction=%s", entity);
        // TODO check audit log event type
        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_DL_CREATE, logMessage)
                .uid(entity.getUid())
                .build();

        logEvent.log();
        UpdateItemEnhancedRequest<RaddTransactionEntity> updateRequest = UpdateItemEnhancedRequest
                .builder(RaddTransactionEntity.class).item(entity).build();
        return Mono.fromFuture(
                raddTable.updateItem(updateRequest).thenApply(x -> {
                    if (!x.getStatus().equals(entity.getStatus())){
                        throw new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS);
                    }
                    return x;
                })
        )
                .onErrorResume(throwable -> {
                    logEvent.generateFailure(throwable.getMessage()).log();
                    return Mono.error(throwable);
                })
                .map(item -> {
                    log.info("Radd transaction object={}", item);
                    logEvent.generateSuccess(String.format("Updated Radd transaction object=%s", item)).log();

                    return item;
                });
    }

    public CompletableFuture<Integer> countFromIunAndOperationIdAndStatus(String operationId,String iun){
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        String query = "(" + RaddTransactionEntity.COL_STATUS + " = :completed" + " OR " +
                RaddTransactionEntity.COL_STATUS + " = :aborted )" +
                " AND ( " + RaddTransactionEntity.COL_IUN + " = :iun)";

        expressionValues.put(":iun", AttributeValue.builder().s(iun).build());
        expressionValues.put(":operationId",  AttributeValue.builder().s(operationId).build());
        expressionValues.put(":completed",  AttributeValue.builder().s(Const.COMPLETED).build());
        expressionValues.put(":aborted",  AttributeValue.builder().s(Const.ABORTED).build());
        return this.getCounterQuery(expressionValues, query);
    }

    private CompletableFuture<Integer> getCounterQuery(Map<String, AttributeValue> values, String filterExpression){
        QueryRequest qeRequest = QueryRequest
                .builder()
                .select(Select.COUNT)
                .tableName(table)
                .keyConditionExpression(RaddTransactionEntity.COL_OPERATION_ID + " = :operationId")
                .filterExpression(filterExpression)
                .expressionAttributeValues(values)
                .build();

        return dynamoDbAsyncClient.query(qeRequest).thenApply(QueryResponse::count);
    }

    public Flux<RaddTransactionEntity> getTransactionsFromIun(String iun, OperationTypeEnum operationType){

        AttributeValue at1 = AttributeValue.builder()
                .s(iun)
                .build();
        AttributeValue at2 = AttributeValue.builder()
                .s(operationType.name())
                .build();

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":value", at1);
        expressionValues.put(":operationTypeValue", at2);

        Expression expression = Expression.builder()
                .expression("contains(iuns, :value) AND " +
                        RaddTransactionEntity.COL_OPERATION_TYPE + " = :operationTypeValue")
                .expressionValues(expressionValues)
                .build();

        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
                .filterExpression(expression)
                .build();

        return Flux.from(raddTable.scan(scanEnhancedRequest)).flatMapIterable(Page::items);
    }

    private TransactWriteItemsEnhancedRequest createTransaction(RaddTransactionEntity raddTransactionEntity,
                                                                List<RaddOperationIun> operationIuns) {

        TransactWriteItemsEnhancedRequest.Builder requestBuilder = TransactWriteItemsEnhancedRequest.builder();

        TransactPutItemEnhancedRequest<RaddTransactionEntity> requestEntity =
                TransactPutItemEnhancedRequest.builder(RaddTransactionEntity.class)
                        .item(raddTransactionEntity)
                        .build();
        requestBuilder.addPutItem(raddTable, requestEntity);

        if (operationIuns != null && !operationIuns.isEmpty()) {
            for (RaddOperationIun item : operationIuns  ) {
                TransactPutItemEnhancedRequest<RaddOperationIun> itemEnhancedRequest =
                        TransactPutItemEnhancedRequest.builder(RaddOperationIun.class)
                                .item(item)
                                .build();
                requestBuilder.addPutItem(raddOperationIunTable, itemEnhancedRequest);
            }
        }

        return requestBuilder.build();
    }

}
