package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.MandateEntity;
import it.pagopa.pn.radd.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Repository
@Slf4j
@Import(PnAuditLogBuilder.class)
public class DocumentInquiryDao extends BaseDao {

    private final PnAuditLogBuilder auditLogBuilder;
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    DynamoDbAsyncClient dynamoDbAsyncClient;
    DynamoDbAsyncTable<MandateEntity> mandateTable;

    String table;

    public DocumentInquiryDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                              DynamoDbAsyncClient dynamoDbAsyncClient,
                              AwsConfigs awsConfigs,
                              PnAuditLogBuilder pnAuditLogBuilder) {
        this.mandateTable = dynamoDbEnhancedAsyncClient.table(awsConfigs.getDynamodbTable(), TableSchema.fromBean(MandateEntity.class));
        this.table = awsConfigs.getDynamodbTable();
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.auditLogBuilder = pnAuditLogBuilder;
    }

    //#region public methods

    /**
     * Ritorna la lista delle deleghe per delegato
     *
     * @param delegateInternaluserid internaluserid del delegato
     * @param status stato da usare nel filtro (OPZIONALE)
     * @return lista delle deleghe
     */
    public Flux<MandateEntity> actInquiry(String delegateInternaluserid, Integer status, String mandateId) {
        // devo sempre filtrare. Se lo stato è passato, vuol dire che voglio filtrare solo per quello stato.
        // altrimenti, è IMPLICITO il fatto di filtrare per le deleghe pendenti e attive (ovvero < 20)
        // NB: listMandatesByDelegate e listMandatesByDelegator si assomigliano, ma a livello di query fanno
        // affidamento ad indici diversi e query diverse
        // listMandatesByDelegate si affida all'indice GSI delegate-state, che filtra per utente delegato E stato.
        log.info("listMandatesByDelegate uid={} status={}", delegateInternaluserid, status);

        QueryConditional queryConditional = QueryConditional.sortLessThanOrEqualTo(getKeyBuild(delegateInternaluserid, "pending"));
        if (status != null)
        {
                queryConditional = QueryConditional.keyEqualTo(getKeyBuild(delegateInternaluserid, status));    // si noti keyEqualTo al posto di sortLessThanOrEqualTo
        }        

        // devo sempre mettere un filtro di salvaguardia per quanto riguarda la scadenza della delega.
        // infatti se una delega è scaduta, potrebbe rimanere a sistema per qualche ora/giorno prima di essere svecchiata
        // e quindi va sempre previsto un filtro sulla data di scadenza
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":now", AttributeValue.builder().s(DateUtils.formatDate(ZonedDateTime.now().toInstant())).build());
        String expression = MandateEntity.COL_D_VALIDTO + " > :now OR attribute_not_exists(" + MandateEntity.COL_D_VALIDTO + ")";

        if (mandateId != null)
        {
            expressionValues.put(":mandateId", AttributeValue.builder().s(mandateId).build());
            expression += " AND " + MandateEntity.COL_S_MANDATEID + " = :mandateId";
        }



        Expression exp = Expression.builder()
            .expression(expression)
            .expressionValues(expressionValues)
            .build();
         
        
        // il filtro cambia in base al fatto se ho chiesto uno stato specifico (uso =)
        //   o se invece non chiedo lo stato (e quindi mi interessano pendenti e attive, uso <=)
        QueryEnhancedRequest qeRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(queryConditional)
                .filterExpression(exp)                
                .scanIndexForward(true)                
                .build();


        // viene volutamente ignorata la gestione della paginazione, che per ora non serve.
        // si suppone infatti che la lista delle deleghe non sia troppo lunga e quindi non vada a sforare il limite di 1MB di paginazione
        return Flux.from(mandateTable.index(GSI_INDEX_DELEGATE_STATE).query(qeRequest)
                .flatMapIterable(Page::items));
    }
}
