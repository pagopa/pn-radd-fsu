package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.Select;

import java.util.HashMap;
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
    String table;


    public RaddTransactionDAO(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                              DynamoDbAsyncClient dynamoDbAsyncClient,
                              AwsConfigs awsConfigs,
                              PnAuditLogBuilder pnAuditLogBuilder) {
        this.raddTable = dynamoDbEnhancedAsyncClient.table(awsConfigs.getDynamodbTable(), TableSchema.fromBean(RaddTransactionEntity.class));
        this.table = awsConfigs.getDynamodbTable();
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.auditLogBuilder = pnAuditLogBuilder;
    }


    public Mono<RaddTransactionEntity> createRaddTransaction(RaddTransactionEntity entity){
        String logMessage = String.format("create Radd Transaction=%s", entity);
        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_DL_CREATE, logMessage)
                .uid(entity.getIun())
                .build();

        logEvent.log();

        return Mono.fromFuture(
                countFromIunAndOperationIdAndStatus(entity.getIun(), entity.getOperationId())
                        .thenCompose(total -> {
                            if (total == 0) {
                                log.info("no current transaction for delegator-delegate pair, can proceed to create transaction");
                                PutItemEnhancedRequest<RaddTransactionEntity> putRequest = PutItemEnhancedRequest.builder(RaddTransactionEntity.class)
                                        .item(entity)
                                        //.conditionExpression()
                                        .build();
                                return raddTable.putItem(putRequest).thenApply(x -> {
                                    log.info("saved Radd transaction object {}", entity);
                                    return entity;
                                });
                            }
                            else {
                                throw new RaddGenericException(ExceptionTypeEnum.TRANSACTION_ALREADY_EXIST);
                            }
                        }))
                .onErrorResume(throwable -> {
                    logEvent.generateFailure(throwable.getMessage()).log();
                    return Mono.error(throwable);
                })
                .map(item -> {
                    log.info("Radd transaction object={}", item);
                    logEvent.generateSuccess(String.format("created Radd transaction object=%s", item)).log();

                    return item;
                });
    }

    public Mono<RaddTransactionEntity> getTransaction(String operationId) {
        Key key = Key.builder().partitionValue(operationId).build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();
        return Mono.fromFuture(raddTable.getItem(request).thenApply(item -> {
            log.info("Item finded : {}", item);
            if (item == null) {
                throw new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST);
            }
            return item;
        }));
    }

    public Mono<RaddTransactionEntity> getTransaction(String operationId, OperationTypeEnum operationType) {
        return Mono.from(this.findQuery(operationId, operationType.name(), null, null)
                .collectList()
                .map(m -> {
                    if (m.isEmpty()) {
                        throw new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST);
                    }
                    return m.get(0);
                })
        );
    }

    public Mono<RaddTransactionEntity> updateStatus(RaddTransactionEntity entity){
        String logMessage = String.format("Update Radd Transaction=%s", entity);
        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_DL_CREATE, logMessage)
                .uid(entity.getIun())
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

    public Flux<RaddTransactionEntity> getTransactionsFromIun(String iun) {
        QueryEnhancedRequest qeRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(iun)
                                .build()
                        )
                )
                .scanIndexForward(true)
                .build();


        return Flux.from(raddTable.index(RaddTransactionEntity.IUN_INDEX).query(qeRequest).flatMapIterable(Page::items));
    }

    public CompletableFuture<Integer> countFromIunAndOperationIdAndStatus(String iun, String operationId){
        String query = ":iun = "+ RaddTransactionEntity.COL_IUN + " AND (" +
                RaddTransactionEntity.COL_STATUS + " = :completed" + " OR " + RaddTransactionEntity.COL_STATUS + " = :aborted" +
                ")";
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":iun",  AttributeValue.builder().s(iun).build());
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

    private Flux<RaddTransactionEntity> findQuery(String partitionValue, String sortKey, String query, Map<String, AttributeValue> values) {
        Key.Builder builderKeys = Key.builder().partitionValue(partitionValue);
        if (!StringUtils.isBlank(sortKey)){
            builderKeys.sortValue(sortKey);
        }
        QueryEnhancedRequest.Builder qeRequestBuilder = QueryEnhancedRequest
                .builder()
                .queryConditional(QueryConditional.keyEqualTo(builderKeys.build()));
        if (!StringUtils.isBlank(query) && values != null){
            qeRequestBuilder.filterExpression(Expression.builder().expression(query).expressionValues(values).build());
        }
        return Flux.from(raddTable.query(qeRequestBuilder.build()).flatMapIterable(Page::items));
    }

}
