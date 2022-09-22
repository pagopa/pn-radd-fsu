package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.Select;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;

// ./mvnw clean install  quando aggiungo classe
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
    void testGetTransactionsFromIun() {

    }

    @Test
    void testCountTransactionIunIdPractice() {
        AwsConfigs awsConfigs = new AwsConfigs();
        String iun = "ium", idPractice = "0", table = awsConfigs.getDynamodbTable();;
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

        DynamoDbAsyncClient localDynamoDbAsyncClient = mock(DynamoDbAsyncClient.class);
//        CompletableFuture<QueryResponse> queryResponseCompletableFuture = mock(CompletableFuture.class);


        CompletableFuture<QueryResponse> queryResponseCompletableFuture = new CompletableFuture<>();
        Mockito.when(localDynamoDbAsyncClient.query(qeRequest)).thenReturn(queryResponseCompletableFuture);

        QueryResponse queryResponse = QueryResponse.builder().build();
        Mockito.when(queryResponseCompletableFuture.thenApply(QueryResponse::count));
        CompletableFuture<Integer> integerCompletableFuture = raddTransactionDAO.countTransactionIunIdPractice(iun, idPractice);

    }
}
