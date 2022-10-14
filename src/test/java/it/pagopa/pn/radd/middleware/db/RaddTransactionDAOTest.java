package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.RaddOperationIun;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RaddTransactionDAOTest extends BaseTest {

    private final Duration d = Duration.ofMillis(3000);
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
    DynamoDbAsyncTable<RaddOperationIun> raddOperationIunTable;
    @Mock
    AwsConfigs awsConfigs;
    @Mock
    Map<String, AttributeValue> expressionValues;
    @Mock
    TransactWriterInitializer transactWriterInitializer;
    RaddTransactionEntity baseEntity;


    @BeforeEach
    public void setUp() {
        Mockito.when(auditLogBuilder.build())
                .thenReturn(new PnAuditLogEvent(PnAuditLogEventType.AUD_DL_CREATE, new HashMap<>(), "", new Object()));

        Mockito.when(auditLogBuilder.uid(Mockito.any()))
                .thenReturn(auditLogBuilder);

        Mockito.when(auditLogBuilder.before(Mockito.any(), Mockito.any()))
                .thenReturn(auditLogBuilder);
        baseEntity = new RaddTransactionEntity();
        baseEntity.setIun("iun");
        baseEntity.setUid("uid");
        baseEntity.setOperationId("operationId");
        baseEntity.setStatus(Const.STARTED);
    }

    @Test
    void testCreateRaddTransaction() {

        QueryResponse queryResponse = QueryResponse.builder().count(0).build();
        Mockito.when(dynamoDbAsyncClient.query((QueryRequest) Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(queryResponse));

        Mockito.doNothing().when(transactWriterInitializer).init();
        Mockito.doNothing().when(transactWriterInitializer).addRequestTransaction(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(transactWriterInitializer).addRequestOperationAndIun(Mockito.any(), Mockito.any());
        Mockito.when(transactWriterInitializer.build()).thenReturn(null);

        Mockito.when(dynamoDbEnhancedAsyncClient.transactWriteItems((TransactWriteItemsEnhancedRequest) Mockito.any()))
                .thenReturn(CompletableFuture.allOf());



        RaddTransactionEntity response = raddTransactionDAO.createRaddTransaction(baseEntity, null).block(d);
        assertEquals(response, baseEntity);
    }

    @Test
    void testCreateRaddTransactionThrow() {

        QueryResponse queryResponse = QueryResponse.builder().count(1).build();
        Mockito.when(dynamoDbAsyncClient.query((QueryRequest) Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(queryResponse));

        StepVerifier.create(raddTransactionDAO.createRaddTransaction( baseEntity, null))
                .expectError(RaddGenericException.class).verify();
    }

    @Test
    void testUpdateStatus() {
        baseEntity.setStatus(Const.COMPLETED);
        Mockito.when(raddTable.updateItem((UpdateItemEnhancedRequest<RaddTransactionEntity>) Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(baseEntity));

        RaddTransactionEntity response = raddTransactionDAO.updateStatus(baseEntity).block(d);
        assertNotNull(response);
        assertEquals(response.getOperationId(), baseEntity.getOperationId());
        assertEquals(response.getStatus(), baseEntity.getStatus());
    }

    @Test
    void testUpdateStatusOnThrow() {
        baseEntity.setStatus(Const.STARTED);

        UpdateItemEnhancedRequest<RaddTransactionEntity> updateRequest = UpdateItemEnhancedRequest
                .builder(RaddTransactionEntity.class).item(baseEntity).build();

        RaddTransactionEntity entityUpdated = new RaddTransactionEntity();
        entityUpdated.setStatus(Const.ERROR);
        Mockito.when(raddTable.updateItem(updateRequest))
                .thenReturn(CompletableFuture.completedFuture(entityUpdated));

        StepVerifier.create(raddTransactionDAO.updateStatus(baseEntity))
                .expectError(RaddGenericException.class).verify();
    }

    @Test
    void testWhenGetActTransactionReturnEntity() {
        Key key = Key.builder()
                .partitionValue("operationId")
                .sortValue(OperationTypeEnum.ACT.name())
                .build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();

        RaddTransactionEntity entity = new RaddTransactionEntity();
        CompletableFuture<RaddTransactionEntity> completableFuture = new CompletableFuture<>();
        completableFuture.complete(entity);

        Mockito.when(raddTable.getItem(request)).thenReturn(completableFuture);

        StepVerifier.create(raddTransactionDAO.getTransaction("operationId", OperationTypeEnum.ACT))
                .expectNext(entity).verifyComplete();
    }

    @Test
    void testWhenGetActTransactionOnThrow() {
        CompletableFuture<RaddTransactionEntity> completableFuture = new CompletableFuture<>();
        completableFuture.complete(null);

        Mockito.when(raddTable.getItem((GetItemEnhancedRequest) Mockito.any())).thenReturn(completableFuture);

        StepVerifier.create(
                raddTransactionDAO.getTransaction("operationId", OperationTypeEnum.ACT)
                ).expectError(RaddGenericException.class).verify();
    }

    @Test
    void testCountFromIunAndOperationIdAndStatus() throws ExecutionException, InterruptedException {
        QueryResponse queryResponse = QueryResponse.builder().count(10).build();
        Mockito.when(dynamoDbAsyncClient.query((QueryRequest) Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(queryResponse));
        assertEquals(10, raddTransactionDAO.countFromIunAndOperationIdAndStatus("iun", "operationId").get().intValue());
    }
}