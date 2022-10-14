package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.RaddOperationIun;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;


@Repository
@Slf4j
@Import(PnAuditLogBuilder.class)
public class RaddOperationIunDAO extends BaseDao {


    private final PnAuditLogBuilder auditLogBuilder;
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    DynamoDbAsyncClient dynamoDbAsyncClient;
    DynamoDbAsyncTable<RaddOperationIun> raddOperationIunTable;
    String table;

    TransactWriterInitializer transactWriterInitializer;

    public RaddOperationIunDAO(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                              DynamoDbAsyncClient dynamoDbAsyncClient,
                              AwsConfigs awsConfigs,
                              PnAuditLogBuilder pnAuditLogBuilder, TransactWriterInitializer transactWriterInitializer) {
        this.raddOperationIunTable = dynamoDbEnhancedAsyncClient.table(awsConfigs.getOperationIunDynamodbTable(), TableSchema.fromBean(RaddOperationIun.class));
        this.table = awsConfigs.getDynamodbTable();
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.auditLogBuilder = pnAuditLogBuilder;
        this.transactWriterInitializer = transactWriterInitializer;
    }

    public Flux<RaddOperationIun> getTransactionsFromIun(String iun) {
        String logMessage = String.format("Retrieve all operation id by iun=%s", iun);
        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_DL_CREATE, logMessage)
                .uid(iun)
                .build();

        logEvent.log();
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


        return Flux.from(raddOperationIunTable.index(RaddOperationIun.INDEX_SECONDARY_NAME)
                .query(qeRequest)
                .flatMapIterable(items -> {
                    logEvent.generateSuccess("Operation id retrieved");
                    return items.items();
                }))
                .onErrorResume(exception -> {
                    logEvent.generateFailure("Exception with retrieve by iun : %s", exception.getMessage());
                    return Flux.error(new RaddGenericException(ExceptionTypeEnum.ERROR_WITH_QUERY_OR_DB));
                });
    }

}
