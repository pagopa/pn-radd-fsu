package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.exception.RaddTransactionNoExistedException;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.Select;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

// ./mvnw clean install  when add a class
//  mvnw verify(jacoco), mvnw test
// mvnw springboot :run
@SpringBootTest
public class RaddTransactionDAOTest {

    @InjectMocks
    private RaddTransactionDAO raddTransactionDAO;
    @Mock
    private PnAuditLogBuilder auditLogBuilder;
    @Mock
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    @Mock
    DynamoDbAsyncClient dynamoDbAsyncClient;
    @Mock
    DynamoDbAsyncTable<RaddTransactionEntity> raddTable;
    @Mock
    AwsConfigs awsConfigs;

    @Test
    void testUpdateStatusOnThrow() {
        RaddTransactionEntity entity = new RaddTransactionEntity();
        String logMessage = String.format("Update Radd Transaction=%s", entity);

        UpdateItemEnhancedRequest<RaddTransactionEntity> updateRequest = UpdateItemEnhancedRequest
                .builder(RaddTransactionEntity.class).item(entity).build();

        Throwable returnThrowable = new Throwable();

        Mono<CompletableFuture> monoCompletableFuture = new Mono<>() {
            @Override
            public void subscribe(CoreSubscriber<? super CompletableFuture> coreSubscriber) {

            }
        };

//        Mockito.when(monoCompletableFuture.onErrorResume(throwable -> Mono.error(throwable)))
//                .thenThrow(returnThrowable);

//        Mockito.when(Mono.fromFuture(raddTable.updateItem(updateRequest).thenApply(x->entity)).onErrorResume(throwable -> {return Mono.error(throwable);}))
//                .thenThrow(returnThrowable);
    }

    @Test
    void testGetTransaction() {
        String operationId = "0";
        Key key = Key.builder().partitionValue(operationId).build();

        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();

        CompletableFuture<RaddTransactionEntity> completableFuture = new CompletableFuture<>();
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        completableFuture.complete(raddTransactionEntity);

//        Mockito.when(completableFuture.thenApply(item -> {
//            if (item == null) {
//                throw new RaddTransactionNoExistedException();
//            }
//            return item;
//        })).thenReturn(completableFuture);

        Mono<RaddTransactionEntity> monoRaddTransactionEntity = new Mono<>() {
            @Override
            public void subscribe(CoreSubscriber<? super RaddTransactionEntity> coreSubscriber) {

            }
        };

//        Mockito.when(Mono.fromFuture(raddTable.getItem(request).thenApply(item -> {
//            if (item == null) {
//                throw new RaddTransactionNoExistedException();
//            }
//            return item;
//        }))).thenReturn(monoRaddTransactionEntity);
//        assertNotNull(raddTransactionDAO.getTransaction(operationId));
    }

    @Test
    void testGetTransactionOnThrow() {
        String operationId = "0";
        Key key = Key.builder().partitionValue(operationId).build();

        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();

        CompletableFuture completableFuture = new CompletableFuture();
        RaddTransactionEntity raddTransactionEntity = null;
        completableFuture.complete(raddTransactionEntity);

        Mockito.when(completableFuture.thenApply(item -> {
            if (item == null) {
                throw new RaddTransactionNoExistedException();
            }
            return item;
        })).thenThrow(new RaddTransactionNoExistedException());

        Mono<RaddTransactionEntity> monoRaddTransactionEntity = new Mono<>() {
            @Override
            public void subscribe(CoreSubscriber<? super RaddTransactionEntity> coreSubscriber) {

            }
        };

//        Mockito.when(Mono.fromFuture(raddTable.getItem(request).thenApply(item -> {
//            if (item == null) {
//                throw new RaddTransactionNoExistedException();
//            }
//            return item;
//        }))).thenReturn(monoRaddTransactionEntity);
//        assertThrows(RaddTransactionNoExistedException.class, () -> {raddTransactionDAO.getTransaction(operationId);});
    }

    @Test
    void testGetTransactionsFromIun() {
        String iun = "iun";
        QueryEnhancedRequest qeRequest = QueryEnhancedRequest
                .builder()
                .queryConditional( QueryConditional.keyEqualTo(Key.builder().partitionValue(iun).build()))
                .scanIndexForward(true)
                .build();

        org.reactivestreams.Publisher returnPublisher = subscriber -> {

        };
        SdkPublisher returnSdkPublisher = SdkPublisher.adapt(returnPublisher);
        SdkPublisher callSdkPublisher = mock(SdkPublisher.class);


        Mockito.when(callSdkPublisher.flatMapIterable(Mockito.any())).thenReturn(returnSdkPublisher);

        DynamoDbAsyncIndex callDynamoDbAsyncIndex = mock(DynamoDbAsyncIndex.class);
        Mockito.when(callDynamoDbAsyncIndex.query(qeRequest)).thenReturn(returnSdkPublisher);

        DynamoDbAsyncIndex returnDynamoDbAsyncIndex = mock(DynamoDbAsyncIndex.class);
        Mockito.when(raddTable.index(RaddTransactionEntity.IUN_INDEX)).thenReturn(returnDynamoDbAsyncIndex);

//        Flux<RaddTransactionEntity> raddTransactionEntityFlux = raddTransactionDAO.getTransactionsFromIun(iun);
    }

    @Test
    void testCountTransactionIunIdPractice() throws ExecutionException, InterruptedException {
        AwsConfigs awsConfigs = new AwsConfigs();
        String iun = "iun", idPractice = "0", table = awsConfigs.getDynamodbTable();
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":iun",  AttributeValue.builder().s(iun).build());
        expressionValues.put(":idPractice",  AttributeValue.builder().s(idPractice).build());

        QueryRequest qeRequest = QueryRequest
                .builder()
                .select(Select.COUNT)
                .tableName(table)
                .keyConditionExpression(RaddTransactionEntity.COL_OPERATION_ID + " = :idPractice")
                .filterExpression(":iun = "+ RaddTransactionEntity.COL_IUN)
                .expressionAttributeValues(expressionValues)
                .build();

        CompletableFuture<QueryResponse> firstResponseCompletableFuture = new CompletableFuture<>();
        QueryResponse queryResponse = QueryResponse.builder().count(10).build();
        firstResponseCompletableFuture.complete(queryResponse);
        CompletableFuture<QueryResponse> secondQueryResponseCompletableFuture = mock(CompletableFuture.class);
        Mockito.when(dynamoDbAsyncClient.query(qeRequest)).thenReturn(firstResponseCompletableFuture);
        CompletableFuture<Integer> returnCompletableFuture = new CompletableFuture<>();
        Mockito.when(secondQueryResponseCompletableFuture.thenApply(QueryResponse::count)).thenReturn(returnCompletableFuture);
        CompletableFuture<Integer> integerCompletableFuture = raddTransactionDAO.countTransactionIunIdPractice(iun, idPractice);
        assertNotNull(integerCompletableFuture);
        assertEquals(10, integerCompletableFuture.get().intValue());
    }
}