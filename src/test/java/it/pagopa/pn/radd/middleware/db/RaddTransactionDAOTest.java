package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void testCreateRaddTransaction() {
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setIuns(List.of("iun"));
        entity.setOperationId("operationId");

        QueryResponse queryResponse = QueryResponse.builder().count(0).build();
        Mockito.when(dynamoDbAsyncClient.query((QueryRequest) Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(queryResponse));

        PutItemEnhancedRequest<RaddTransactionEntity> putRequest = PutItemEnhancedRequest.builder(RaddTransactionEntity.class)
                .item(entity)
                .build();

        Mockito.when(raddTable.putItem(putRequest)).thenReturn(CompletableFuture.completedFuture(null));

        RaddTransactionEntity response = raddTransactionDAO.createRaddTransaction(entity).block(d);
        assertEquals(response, entity);
    }

    @Test
    void testCreateRaddTransactionThrow() {
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setIuns(List.of("iun"));
        entity.setOperationId("operationId");

        QueryResponse queryResponse = QueryResponse.builder().count(1).build();
        Mockito.when(dynamoDbAsyncClient.query((QueryRequest) Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(queryResponse));

        StepVerifier.create(raddTransactionDAO.createRaddTransaction(entity))
                .expectError(RaddGenericException.class).verify();
    }

    @Test
    void testUpdateStatus() {
        RaddTransactionEntity entityToUpdate = new RaddTransactionEntity();
        entityToUpdate.setIuns(List.of("iun"));
        entityToUpdate.setOperationId("operationId");
        entityToUpdate.setStatus("completed");

        RaddTransactionEntity entityUpdated = new RaddTransactionEntity();
        entityUpdated.setIuns(List.of("iun"));
        entityUpdated.setOperationId("operationId");
        entityUpdated.setStatus("completed");
        Mockito.when(raddTable.updateItem((UpdateItemEnhancedRequest<RaddTransactionEntity>) Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(entityUpdated));

        RaddTransactionEntity response = raddTransactionDAO.updateStatus(entityToUpdate).block(d);
        assertEquals(response, entityToUpdate);
    }

    @Test
    void testUpdateStatusOnThrow() {
        RaddTransactionEntity entityToUpdate = new RaddTransactionEntity();
        entityToUpdate.setIuns(List.of("iun"));
        entityToUpdate.setOperationId("operationId");
        entityToUpdate.setStatus("completed");

        UpdateItemEnhancedRequest<RaddTransactionEntity> updateRequest = UpdateItemEnhancedRequest
                .builder(RaddTransactionEntity.class).item(entityToUpdate).build();

        RaddTransactionEntity entityUpdated = new RaddTransactionEntity();
        entityUpdated.setStatus("progress");
        Mockito.when(raddTable.updateItem(updateRequest))
                .thenReturn(CompletableFuture.completedFuture(entityUpdated));

        StepVerifier.create(raddTransactionDAO.updateStatus(entityToUpdate))
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