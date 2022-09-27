package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.RaddTransactionNoExistedException;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RaddTransactionDAOTest extends BaseTest {

    @InjectMocks
    private RaddTransactionDAO raddTransactionDAO;

    private PnAuditLogBuilder auditLogBuilder;
    @Mock
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    @Mock
    DynamoDbAsyncClient dynamoDbAsyncClient;
    @Mock
    DynamoDbAsyncTable<RaddTransactionEntity> raddTable;
    @Mock
    AwsConfigs awsConfigs;

    @Mock
    Map<String, AttributeValue> expressionValues;


    @Test
    void testCreateRaddTransaction() {
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setIun("iun");
        entity.setOperationId("operationId");
//        PnAuditLogBuilder pnAuditLogBuilder = new PnAuditLogBuilder();
//        Mockito.when(auditLogBuilder.before(PnAuditLogEventType.AUD_DL_CREATE, String.format("message")))
//                        .thenReturn(pnAuditLogBuilder);
//
//        Mockito.when(auditLogBuilder.uid(entity.getIun()))
//                .thenReturn(pnAuditLogBuilder);
//
//        Mockito.when(auditLogBuilder.build())
//                .thenReturn(new PnAuditLogEvent(PnAuditLogEventType.AUD_DL_CREATE, new HashMap<>(), "", new Object()));

        QueryResponse queryResponse = QueryResponse.builder().count(0).build();
        Mockito.when(dynamoDbAsyncClient.query((QueryRequest) Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(queryResponse));

//        StepVerifier.create(raddTransactionDAO.createRaddTransaction(entity))
//                .expectNext(entity).verifyComplete();
    }

    @Test
    void testUpdateStatusOnThrow() {
    }

    @Test
    void testGetTransaction() {
        Key key = Key.builder().partitionValue("operationId").build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();

        RaddTransactionEntity entity = new RaddTransactionEntity();
        CompletableFuture<RaddTransactionEntity> completableFuture = new CompletableFuture<>();
        completableFuture.complete(entity);
        Mockito.when(raddTable.getItem(request)).thenReturn(completableFuture);
        StepVerifier.create(raddTransactionDAO.getTransaction("operationId")).expectNext(entity).verifyComplete();
    }

    @Test
    void testGetTransactionOnThrow() {
        Key key = Key.builder().partitionValue("operationId").build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();

        RaddTransactionEntity entity = null;
        CompletableFuture<RaddTransactionEntity> completableFuture = new CompletableFuture<>();
        completableFuture.complete(entity);
        Mockito.when(raddTable.getItem(request)).thenReturn(completableFuture);
        StepVerifier.create(raddTransactionDAO.getTransaction("opertationId")).expectError(RaddTransactionNoExistedException.class).verify();
    }

    @Test
    void testCountFromIunAndOperationIdAndStatus() throws ExecutionException, InterruptedException {
        QueryResponse queryResponse = QueryResponse.builder().count(10).build();
        Mockito.when(dynamoDbAsyncClient.query((QueryRequest) Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(queryResponse));
        assertEquals(10, raddTransactionDAO.countFromIunAndOperationIdAndStatus("iun", "operationId").get().intValue());
//        StepVerifier.create(raddTransactionDAO.countFromIunAndOperationIdAndStatus("iun", "operationId"))
//                .expectNext(10).verifyComplete();
    }
}