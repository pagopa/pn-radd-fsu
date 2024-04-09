package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.exception.TransactionAlreadyExistsException;
import it.pagopa.pn.radd.pojo.ResultPaginationDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Collection;
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

    public static final int FILTER_EXPRESSION_APPLIED_MULTIPLIER = 4;

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

    protected Flux<T> getByFilter(QueryConditional conditional, String index, String expression, Map<String, AttributeValue> expressionValues, Map<String, String> expressionNames, Integer maxElements) {
        QueryEnhancedRequest.Builder qeRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(conditional);
        if (maxElements != null) {
            qeRequest.limit(maxElements);
        }
        if (!StringUtils.isBlank(expression)){
            qeRequest.filterExpression(Expression.builder().expression(expression).expressionValues(expressionValues).expressionNames(expressionNames).build());
        }
        if (StringUtils.isNotBlank(index)){
            return Flux.from(this.tableAsync.index(index).query(qeRequest.build()).flatMapIterable(Page::items));
        }
        return Flux.from(this.tableAsync.query(qeRequest.build()).flatMapIterable(Page::items));
    }

    protected <T, K> Mono<ResultPaginationDto<T, K>> getByFilterPaginated(QueryConditional conditional, String index, Map<String, AttributeValue> values, String filterExpression, Integer pageSize, Map<String, AttributeValue> lastEvaluatedKey) {
        QueryEnhancedRequest.Builder query = QueryEnhancedRequest
                .builder()
                .queryConditional(conditional);

        int totalElements = pageSize * raddFsuConfig.getMaxPageNumber();
        if (!StringUtils.isBlank(filterExpression)) {
            query.filterExpression(Expression.builder().expression(filterExpression).expressionValues(values).build());
            totalElements *= (values.size() + 1) * 2;
        }
        if (totalElements > raddFsuConfig.getMaxDynamoDBQuerySize()) {
            totalElements = raddFsuConfig.getMaxDynamoDBQuerySize();
        }
        query.limit(totalElements);

        return query(index, query, new ResultPaginationDto<T, K>(), totalElements, lastEvaluatedKey);
    }

    private <T, K> Mono<ResultPaginationDto<T, K>> query(String index,
                              QueryEnhancedRequest.Builder queryEnhancedRequestBuilder,
                              ResultPaginationDto<T, K> resultPaginationDto,
                              int limit,
                              Map<String, AttributeValue> lastEvaluatedKey) {
        if (lastEvaluatedKey != null) {
            queryEnhancedRequestBuilder.exclusiveStartKey(lastEvaluatedKey);
        }

        return Mono.from(this.tableAsync.index(index).query(queryEnhancedRequestBuilder.build()))
                .flatMap(tPage -> {
                    Map<String, AttributeValue> lastKey = tPage.lastEvaluatedKey();

                    resultPaginationDto.getResultsPage().addAll((Collection<? extends T>) tPage.items());

                    if (resultPaginationDto.getResultsPage().size() >= limit || lastKey == null) {
                        resultPaginationDto.setMoreResult(lastKey != null);
                        return Mono.just(resultPaginationDto);
                    }

                    return query(index, queryEnhancedRequestBuilder, resultPaginationDto, limit, tPage.lastEvaluatedKey());
                });
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

    protected Mono<Void> transactWriteItems(List<T> entityList, Class<T> entityClass) {
        TransactWriteItemsEnhancedRequest.Builder transactionWriteRequest = TransactWriteItemsEnhancedRequest.builder();
        entityList.forEach(entity ->
            transactionWriteRequest.addUpdateItem(this.tableAsync,
                    TransactUpdateItemEnhancedRequest.builder(entityClass).item(entity).build())
        );

        return Mono.fromFuture(this.dynamoDbEnhancedAsyncClient.transactWriteItems(transactionWriteRequest.build()));
    }

}
