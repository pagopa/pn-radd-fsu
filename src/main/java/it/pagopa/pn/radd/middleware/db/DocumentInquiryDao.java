package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.exception.RaddTransactionAlreadyExist;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.Select;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
@Slf4j
@Import(PnAuditLogBuilder.class)
public class DocumentInquiryDao extends BaseDao {

    private final PnAuditLogBuilder auditLogBuilder;
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    DynamoDbAsyncClient dynamoDbAsyncClient;
    DynamoDbAsyncTable<RaddTransactionEntity> mandateTable;
    String table;


    public DocumentInquiryDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                              DynamoDbAsyncClient dynamoDbAsyncClient,
                              AwsConfigs awsConfigs,
                              PnAuditLogBuilder pnAuditLogBuilder) {
        this.mandateTable = dynamoDbEnhancedAsyncClient.table(awsConfigs.getDynamodbTable(), TableSchema.fromBean(RaddTransactionEntity.class));
        this.table = awsConfigs.getDynamodbTable();
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.auditLogBuilder = pnAuditLogBuilder;
    }


    public Mono<RaddTransactionEntity> createRaddTransaction(RaddTransactionEntity entity){
        String logMessage = String.format("create Radd Transaction=%s", entity);
        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_DL_CREATE, logMessage)
                .uid(entity.getIun())
                .build();

        logEvent.log();

        return Mono.fromFuture(
                countMandateForDelegateAndDelegator(entity.getIun(), entity.getIdPractice())
                        .thenCompose(total -> {
                            if (total == 0)
                            {
                                log.info("no current mandate for delegator-delegate pair, can proceed to create mandate");
                                PutItemEnhancedRequest<RaddTransactionEntity> putRequest = PutItemEnhancedRequest.builder(RaddTransactionEntity.class)
                                        .item(entity)
                                        //.conditionExpression()
                                        .build();
                                return mandateTable.putItem(putRequest).thenApply(x -> {
                                    log.info("saved Radd transaction object {}", entity);
                                    return entity;
                                });
                            }
                            else {
                                throw new RaddTransactionAlreadyExist();
                            }
                        }))
                .onErrorResume(throwable -> {
                    logEvent.generateFailure(throwable.getMessage()).log();
                    return Mono.error(throwable);
                })
                .map(item -> {
                    log.info("Radd transaction object={}", item);
                    logEvent.generateSuccess(String.format("created Radd transaction object=%s", item)).log();

                    return item;
                });
    }


    private CompletableFuture<Integer> countMandateForDelegateAndDelegator(String iun, String idPractice) {
        // qui ho entrambi gli id, mi serve sapere se ho già una delega per la coppia delegante-delegato, in pending/attiva e non scaduta ovviamente.

        // uso l'expression filter per filtrare le deleghe valide per il delegato
        // si accetta il costo di leggere più righe per niente
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":iun",  AttributeValue.builder().s(iun).build());
        expressionValues.put(":idPractice",  AttributeValue.builder().s(idPractice).build());

        QueryRequest qeRequest = QueryRequest
                .builder()
                .select(Select.COUNT)
                .tableName(table)
                .keyConditionExpression(RaddTransactionEntity.COL_PK + " = :iun AND begins_with(" + RaddTransactionEntity.COL_SK + ", :idPractice)")
                .expressionAttributeValues(expressionValues)
                .build();

        return dynamoDbAsyncClient.query(qeRequest).thenApply(QueryResponse::count);
    }


}
