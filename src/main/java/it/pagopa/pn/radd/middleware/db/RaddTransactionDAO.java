package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
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

import java.util.Date;
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
    DynamoDbAsyncTable<OperationsIunsEntity> raddOperationIunTable;
    String table;

    TransactWriterInitializer transactWriterInitializer;

    public RaddTransactionDAO(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                              DynamoDbAsyncClient dynamoDbAsyncClient,
                              AwsConfigs awsConfigs,
                              PnAuditLogBuilder pnAuditLogBuilder, TransactWriterInitializer transactWriterInitializer) {
        this.raddTable = dynamoDbEnhancedAsyncClient.table(awsConfigs.getDynamodbTable(), TableSchema.fromBean(RaddTransactionEntity.class));
        this.raddOperationIunTable = dynamoDbEnhancedAsyncClient.table(awsConfigs.getDynamodbIunsoperationsTable(), TableSchema.fromBean(OperationsIunsEntity.class));
        this.table = awsConfigs.getDynamodbTable();
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.auditLogBuilder = pnAuditLogBuilder;
        this.transactWriterInitializer = transactWriterInitializer;
    }


    public Mono<RaddTransactionEntity> createRaddTransaction(RaddTransactionEntity entity, List<OperationsIunsEntity> entityIuns){
        String logMessage = String.format("create Radd Transaction=%s", entity);
        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_DL_CREATE, logMessage)
                .uid(entity.getUid())
                .build();
        logEvent.log();
        log.info("CREATE TRANSACTION DAO TICK {}", new Date().getTime());
        return Mono.fromFuture(
                countFromIunAndOperationIdAndStatus(entity.getOperationId(), entity.getIun())
                        .thenCompose(total -> {
                            if (total == 0) {
                                log.debug("no current transaction for delegator-delegate pair, can proceed to create transaction");
                                try {
                                    TransactWriteItemsEnhancedRequest transactRequest = createTransaction(entity, entityIuns);
                                    return dynamoDbEnhancedAsyncClient.transactWriteItems(transactRequest)
                                                .thenApply(item -> entity);
                                } catch (TransactionCanceledException ex) {
                                    throw new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_SAVED);
                                }
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

                    log.info("CREATE TRANSACTION DAO TOCK {}", new Date().getTime());
                    logEvent.generateSuccess(String.format("created Radd transaction object=%s", item)).log();

                    return item;
                });
    }

    public Mono<RaddTransactionEntity> getTransaction(String operationId, OperationTypeEnum operationType) {
        Key key = Key.builder().partitionValue(operationId).sortValue(operationType.name()).build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();
        return Mono.fromFuture(raddTable.getItem(request).thenApply(item -> {
            log.debug("Item finded : {}", item);
            if (item == null) {
                throw new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST);
            }
            return item;
        }));
    }

    public Mono<RaddTransactionEntity> updateStatus(RaddTransactionEntity entity){
        String logMessage = String.format("Update Radd Transaction=%s", entity);
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
                    log.debug("Radd transaction object={}", item);
                    logEvent.generateSuccess(String.format("Updated Radd transaction object=%s", item)).log();

                    return item;
                });
    }

    public CompletableFuture<Integer> countFromIunAndOperationIdAndStatus(String operationId, String iun){
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        String query = "(" + RaddTransactionEntity.COL_STATUS + " = :completed" + " OR " +
                RaddTransactionEntity.COL_STATUS + " = :aborted )" +
                " AND ( " + RaddTransactionEntity.COL_IUN + " = :iun)";

        expressionValues.put(":iun", AttributeValue.builder().s(iun).build());
        expressionValues.put(":operationId",  AttributeValue.builder().s(operationId).build());
        expressionValues.put(":completed",  AttributeValue.builder().s(Const.COMPLETED).build());
        expressionValues.put(":aborted",  AttributeValue.builder().s(Const.ABORTED).build());
        log.info("COUNT DAO TICK {}", new Date().getTime());
        return this.getCounterQuery(expressionValues, query);
    }

    public CompletableFuture<Integer> countFromQrCodeCompleted(String qrCode){
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        String query = "(" + RaddTransactionEntity.COL_STATUS + " = :completed AND " + RaddTransactionEntity.COL_OPERATION_TYPE +  " = :type)";
        expressionValues.put(":completed", AttributeValue.builder().s(Const.COMPLETED).build());
        expressionValues.put(":type", AttributeValue.builder().s(OperationTypeEnum.ACT.name()).build());
        expressionValues.put(":qrcodevalue",  AttributeValue.builder().s(qrCode).build());

        QueryRequest qeRequest = QueryRequest
                .builder()
                .select(Select.COUNT)
                .tableName(table)
                .indexName(RaddTransactionEntity.QRCODE_SECONDARY_INDEX)
                .keyConditionExpression(RaddTransactionEntity.COL_QR_CODE + " = :qrcodevalue")
                .filterExpression(query)
                .expressionAttributeValues(expressionValues)
                .build();
        log.info("COUNT QUERY DAO TICK {}", new Date().getTime());
        return dynamoDbAsyncClient.query(qeRequest).thenApply(QueryResponse::count);
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


        return Flux.from(raddTable.index(RaddTransactionEntity.IUN_SECONDARY_INDEX).query(qeRequest).flatMapIterable(Page::items));
    }

    public Flux<RaddTransactionEntity> getTransactionsFromFiscalCode(String ensureFiscalCode) {
        QueryEnhancedRequest qeRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(QueryConditional.keyEqualTo(
                                Key.builder()
                                        .partitionValue(ensureFiscalCode)
                                        .build()
                        )
                )
                .scanIndexForward(true)
                .build();


        return Flux.from(raddTable.index(RaddTransactionEntity.RECIPIENT_SECONDARY_INDEX).query(qeRequest).flatMapIterable(Page::items))
                .concatWith(raddTable.index(RaddTransactionEntity.DELEGATE_SECONDARY_INDEX).query(qeRequest).flatMapIterable(Page::items));
    }

    private TransactWriteItemsEnhancedRequest createTransaction(RaddTransactionEntity raddTransactionEntity,
                                                                List<OperationsIunsEntity> operationIuns) {


        this.transactWriterInitializer.init();
        TransactPutItemEnhancedRequest<RaddTransactionEntity> requestEntity =
                TransactPutItemEnhancedRequest.builder(RaddTransactionEntity.class)
                        .item(raddTransactionEntity)
                        .build();
        this.transactWriterInitializer.addRequestTransaction(raddTable, requestEntity);

        if (operationIuns != null && !operationIuns.isEmpty()) {
            for (OperationsIunsEntity item : operationIuns  ) {
                TransactPutItemEnhancedRequest<OperationsIunsEntity> itemEnhancedRequest =
                        TransactPutItemEnhancedRequest.builder(OperationsIunsEntity.class)
                                .item(item)
                                .build();
                this.transactWriterInitializer.addRequestOperationAndIun(raddOperationIunTable, itemEnhancedRequest);
            }
        }

        return this.transactWriterInitializer.build();
    }

}
