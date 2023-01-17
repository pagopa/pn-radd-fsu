package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Repository
@Slf4j
public class OperationsIunsDAO extends BaseDao {

    private final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    private final DynamoDbAsyncTable<OperationsIunsEntity> operationsIunsTable;
    private final String table;

    public OperationsIunsDAO(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                             DynamoDbAsyncClient dynamoDbAsyncClient,
                             AwsConfigs awsConfigs) {
        this.operationsIunsTable = dynamoDbEnhancedAsyncClient.table(awsConfigs.getDynamodbIunsoperationsTable(), TableSchema.fromBean(OperationsIunsEntity.class));
        this.table = awsConfigs.getDynamodbIunsoperationsTable();
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
    }


    public Flux<OperationsIunsEntity> getAllOperationFromIun(String iun){

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

        return Flux.from(
                operationsIunsTable
                        .index(OperationsIunsEntity.SECONDARY_INDEX)
                        .query(qeRequest)
                        .flatMapIterable(Page::items)
        );
    }


}
