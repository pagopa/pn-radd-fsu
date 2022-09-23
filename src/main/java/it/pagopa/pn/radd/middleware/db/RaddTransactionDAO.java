package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.exception.RaddTransactionAlreadyExist;
import it.pagopa.pn.radd.exception.RaddTransactionNoExistedException;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.utils.Const;
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
                        countFromIunAndIdPracticeAndStatus(entity.getIun(), entity.getOperationId())
                        .thenCompose(total -> {
                            if (total == 0)
                            {
                                log.info("no current mandate for delegator-delegate pair, can proceed to create mandate");
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
                                throw new RaddTransactionAlreadyExist();
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
                throw new RaddTransactionNoExistedException(Const.NOT_EXISTS_OPERATION);
            }
            return item;
        }));
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
        return Mono.fromFuture(raddTable.updateItem(updateRequest).thenApply(x->entity)).onErrorResume(throwable -> {
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
                .queryConditional( QueryConditional.keyEqualTo(Key.builder().partitionValue(iun).build()))
                .scanIndexForward(true)
                .build();

        return Flux.from(raddTable.index(RaddTransactionEntity.IUN_INDEX).query(qeRequest).flatMapIterable(Page::items));
    }

    public CompletableFuture<Integer> countFromIunAndIdPracticeAndStatus(String iun, String idPractice){
        String query = ":iun = "+ RaddTransactionEntity.COL_IUN + " AND (" +
                RaddTransactionEntity.COL_STATUS + " = :completed" + " OR " + RaddTransactionEntity.COL_STATUS + " = :aborted" +
                ")";
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":iun",  AttributeValue.builder().s(iun).build());
        expressionValues.put(":idPractice",  AttributeValue.builder().s(idPractice).build());
        expressionValues.put(":completed",  AttributeValue.builder().s(Const.COMPLETED).build());
        expressionValues.put(":aborted",  AttributeValue.builder().s(Const.ABORTED).build());
        return this.getCounterQuery(expressionValues, query);
    }
    private CompletableFuture<Integer> getCounterQuery(Map<String, AttributeValue> values, String filterExpression){
        QueryRequest qeRequest = QueryRequest
                .builder()
                .select(Select.COUNT)
                .tableName(table)
                .keyConditionExpression(RaddTransactionEntity.COL_OPERATION_ID + " = :idPractice")
                .filterExpression(filterExpression)
                .expressionAttributeValues(values)
                .build();

        return dynamoDbAsyncClient.query(qeRequest).thenApply(QueryResponse::count);
    }


}
