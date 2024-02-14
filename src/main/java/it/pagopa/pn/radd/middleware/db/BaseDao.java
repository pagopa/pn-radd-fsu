package it.pagopa.pn.radd.middleware.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.exception.TransactionAlreadyExistsException;
import it.pagopa.pn.radd.pojo.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.utils.ImmutableMap;

import java.util.List;
import java.util.Map;


@Slf4j
public abstract class BaseDao<T> {
    private final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    private final DynamoDbAsyncTable<T> tableAsync;
    private final String tableName;
    private final Class<T> tClass;
    private final PnRaddFsuConfig raddFsuConfig;

    protected BaseDao(
            DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
            DynamoDbAsyncClient dynamoDbAsyncClient,
            String tableName,
            PnRaddFsuConfig raddFsuConfig,
            Class<T> tClass
    ){
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.tableAsync = dynamoDbEnhancedAsyncClient.table(tableName, TableSchema.fromBean(tClass));
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.tableName = tableName;
        this.raddFsuConfig = raddFsuConfig;
        this.tClass = tClass;

    }

    protected Mono<T> putItem(T entity){
        return Mono.fromFuture(this.tableAsync.putItem(entity))
                .thenReturn(entity);
    }

    protected Mono<T> updateItem(T entity){
        return Mono.fromFuture(this.tableAsync.updateItem(entity));
    }

    protected Mono<T> findFromKey(Key key){
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();
        return Mono.fromFuture(tableAsync.getItem(request));
    }

    protected Mono<Void> batchWriter(List<T> entities, int attempt){
        if (attempt >= raddFsuConfig.getAttemptBatchWriter()) return Mono.error(new RaddGenericException("Ended attempt for batch writer"));
        if (entities == null || entities.isEmpty()) return Mono.just("").then();
        if (entities.size() > 25) {
            return Mono.error(new RaddGenericException("Limit overflow for Batch Write operation"));
        }
        WriteBatch.Builder<T> writerBuilder = WriteBatch.builder(tClass)
                .mappedTableResource(this.tableAsync);

        entities.parallelStream()
                .forEach(item -> writerBuilder.addPutItem(builder -> builder.item(item)));

        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder()
                .writeBatches(writerBuilder.build()).build();

        return Mono.fromFuture(this.dynamoDbEnhancedAsyncClient.batchWriteItem(batchWriteItemEnhancedRequest))
                .flatMap(result -> {
                    List<T> unprocesseds = result.unprocessedPutItemsForTable(tableAsync);
                    if (unprocesseds.isEmpty()) return Mono.just("").then();
                    return this.batchWriter(entities, attempt+1);
                });
    }

    protected Mono<Integer> getCounterQuery(Map<String, AttributeValue> values, String filterExpression, String keyConditionExpression, String index){
        QueryRequest.Builder qeRequest = QueryRequest
                .builder()
                .select(Select.COUNT)
                .tableName(tableName)
                .keyConditionExpression(keyConditionExpression)
                .expressionAttributeValues(values);

        if (!StringUtils.isBlank(filterExpression)){
            qeRequest.filterExpression(filterExpression);
        }

        if (!StringUtils.isBlank(index)){
           qeRequest.indexName(index);
        }

        return Mono.fromFuture(dynamoDbAsyncClient.query(qeRequest.build()).thenApply(QueryResponse::count));
    }

    protected Flux<T> getByFilter(QueryConditional conditional, String index, Map<String, AttributeValue> values, String filterExpression, Integer maxElements){
        QueryEnhancedRequest.Builder qeRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(conditional);
        if (maxElements != null) {
            qeRequest.limit(maxElements);
        }
        if (!StringUtils.isBlank(filterExpression)){
            qeRequest.filterExpression(Expression.builder().expression(filterExpression).expressionValues(values).build());
        }
        if (StringUtils.isNotBlank(index)){
            return Flux.from(this.tableAsync.index(index).query(qeRequest.build()).flatMapIterable(Page::items));
        }
        return Flux.from(this.tableAsync.query(qeRequest.build()).flatMapIterable(Page::items));
    }

    protected Mono<T> putItemWithConditions(T entity, Expression expression, Class<T> entityClass) {
        PutItemEnhancedRequest<T> putItemEnhancedRequest = PutItemEnhancedRequest.builder(entityClass)
                .item(entity)
                .conditionExpression(expression)
                .build();

        return Mono.fromFuture(this.tableAsync.putItem(putItemEnhancedRequest)).thenReturn(entity)
                .onErrorResume(ConditionalCheckFailedException.class, e -> {
                            log.warn("ConditionalCheckFailed for putting entity: {}", entity);
                            return Mono.error(new TransactionAlreadyExistsException());
                        }
                );
    }

    public Mono<UpdateItemResponse> updateZipAttachments(ImmutableMap<String, AttributeValue> key, String updateExpression, ImmutableMap<String, AttributeValue> expressionAttributeValues) {
        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression(updateExpression)
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        return Mono.fromFuture(dynamoDbAsyncClient.updateItem(updateItemRequest));
    }
}
