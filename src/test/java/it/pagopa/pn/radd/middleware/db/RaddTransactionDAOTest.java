package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.RaddTransactionNoExistedException;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RaddTransactionDAOTest extends BaseTest {

    @InjectMocks
    private RaddTransactionDAO raddTransactionDAO;

    @Mock
    PnAuditLogBuilder auditLogBuilder;
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


    @BeforeEach
    public void setUp() {
        Mockito.when(auditLogBuilder.build())
                .thenReturn(new PnAuditLogEvent(PnAuditLogEventType.AUD_DL_CREATE, new HashMap<>(), "", new Object()));

        Mockito.when(auditLogBuilder.uid(Mockito.any()))
                .thenReturn(auditLogBuilder);

        Mockito.when(auditLogBuilder.before(Mockito.any(), Mockito.any()))
                .thenReturn(auditLogBuilder);
    }
    //TODO test commentato temporaneamente
    //@Test
    void testCreateRaddTransaction() {
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setIun("iun");
        entity.setOperationId("operationId");

        QueryResponse queryResponse = QueryResponse.builder().count(0).build();
        Mockito.when(dynamoDbAsyncClient.query((QueryRequest) Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(queryResponse));

        StepVerifier.create(raddTransactionDAO.createRaddTransaction(entity))
                .expectNext(entity).verifyComplete();
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

//    @Test
    void testGetTransactionOnThrow() {
        RaddTransactionEntity entity = null;
        CompletableFuture<RaddTransactionEntity> completableFuture = new CompletableFuture<>();
        completableFuture.complete(entity);
        Mockito.when(raddTable.getItem((GetItemEnhancedRequest) Mockito.any())).thenReturn(completableFuture);
        StepVerifier.create(raddTransactionDAO.getTransaction("operationId")).expectError(RaddTransactionNoExistedException.class).verify();
    }

    @Test
    void testGetTransactionFromIun() {
        RaddTransactionEntity entity = new RaddTransactionEntity();

        Page<RaddTransactionEntity> page = Page.create(List.of(entity));
        SdkPublisher<Page<RaddTransactionEntity>> publisher = (PagePublisher<RaddTransactionEntity>) subscriber -> { subscriber.onNext(page); };
        DynamoDbAsyncIndex<RaddTransactionEntity> dynamoDbAsyncIndex = mock(DynamoDbAsyncIndex.class);

        Mockito.when(dynamoDbAsyncIndex.query((QueryEnhancedRequest) Mockito.any())).thenReturn(publisher);
        Mockito.when(raddTable.index(RaddTransactionEntity.IUN_INDEX)).thenReturn(dynamoDbAsyncIndex);

        raddTransactionDAO
                .getTransactionsFromIun("operationId")
                .collectList()
                .doOnNext(element -> assertEquals(entity, element.get(0)));
    }

    @Test
    void testCountFromIunAndOperationIdAndStatus() throws ExecutionException, InterruptedException {
        QueryResponse queryResponse = QueryResponse.builder().count(10).build();
        Mockito.when(dynamoDbAsyncClient.query((QueryRequest) Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(queryResponse));
        assertEquals(10, raddTransactionDAO.countFromIunAndOperationIdAndStatus("iun", "operationId").get().intValue());
    }
}