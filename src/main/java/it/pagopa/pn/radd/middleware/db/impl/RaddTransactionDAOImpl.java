package it.pagopa.pn.radd.middleware.db.impl;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.OperationsIunsDAO;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.config.AwsConfigs;
import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.pojo.RaddTransactionStatusEnum;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.DateUtils;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DATE_VALIDATION_ERROR;
import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.OPERATION_TYPE_UNKNOWN;
import static it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity.ITEMS_SEPARATOR;

@Repository
@Slf4j
public class RaddTransactionDAOImpl extends BaseDao<RaddTransactionEntity> implements RaddTransactionDAO {
    private final OperationsIunsDAO operationsIunsDAO;

    public RaddTransactionDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                  DynamoDbAsyncClient dynamoDbAsyncClient,
                                  OperationsIunsDAO operationsIunsDAO,
                                  PnRaddFsuConfig pnRaddFsuConfig,
                                  AwsConfigs awsConfigs) {
        super(dynamoDbEnhancedAsyncClient,
                dynamoDbAsyncClient,
                awsConfigs.getDynamodbTable(),
                pnRaddFsuConfig,
                RaddTransactionEntity.class);
        this.operationsIunsDAO = operationsIunsDAO;
    }

    @Override
    public Mono<RaddTransactionEntity> createRaddTransaction(RaddTransactionEntity entity, List<OperationsIunsEntity> iunsEntities){
        return putTransactionWithConditions(entity)
                .doOnNext(raddTransaction -> log.debug("[{} - {}] radd transaction created", raddTransaction.getOperationId(), raddTransaction.getIun()))
                .flatMap(raddTransaction -> operationsIunsDAO.putWithBatch(iunsEntities).thenReturn(entity))
                .flatMap(raddTransaction -> updateStatus(raddTransaction, RaddTransactionStatusEnum.STARTED));
    }

    public Mono<RaddTransactionEntity> putTransactionWithConditions(RaddTransactionEntity entity) {
        Expression expression = createExpression(entity);
        return super.putItemWithConditions(entity, expression, RaddTransactionEntity.class);
    }

    private Expression createExpression(RaddTransactionEntity entity) {
        if(OperationTypeEnum.ACT.name().equals(entity.getOperationType())) {
            return buildExpressionForAct(entity);
        }
        else if(OperationTypeEnum.AOR.name().equals(entity.getOperationType())) {
            return buildExpressionForAor(entity);
        }
        else {
            throw new RaddGenericException(OPERATION_TYPE_UNKNOWN);
        }
    }

    /**
     * Condizione utile per la PUT condizionata di una operazione di tipo ACT.
     * Siccome le operazioni di tipo ACT hanno, rispetto a quelle AOR, i campi iun e qrCode valorizzati,
     * viene ri-utilizzato il metodo {@link #buildExpressionForAor(RaddTransactionEntity)} aggiungendo in AND
     * la condizione dei due campi iun e qrCode (a loro volta legati in AND)
     * @param entity
     *
     * @return una espressione per la PUT condizionale per una operazione ACT
     */
    private Expression buildExpressionForAct(RaddTransactionEntity entity) {

        Expression expressionPK = buildExpressionForPK();

        String expressionIunAndQrCode = "iun = :expectedIun AND qrCode = :expectedQrCode";

        Expression expressionForAor = buildCommonConditionsAORAndACT(entity).build();

        Expression expressionOnlyFieldsAct = Expression.builder()
                .expression(expressionIunAndQrCode)
                .putExpressionValue(":expectedIun", AttributeValue.builder().s(entity.getIun()).build())
                .putExpressionValue(":expectedQrCode", AttributeValue.builder().s(entity.getQrCode()).build())
                .build();

        Expression finalExpressionACT = Expression.join(expressionForAor, expressionOnlyFieldsAct, "AND");

        return Expression.join(expressionPK, finalExpressionACT, "OR");

    }

    /**
     * Questa condizione simula il putIfAbsent
     * @return una espressione che simula il putIfAbsent
     */
    private Expression buildExpressionForPK() {
        return Expression.builder()
                .expression("attribute_not_exists(operationId) AND attribute_not_exists(operationType)")
                .build();
    }

    /**
     * Condizione utile per la PUT condizionata di una operazione di tipo AOR.
     * @param entity
     *
     * @return una espressione per la PUT condizionale per una operazione ACT
     */
    private Expression buildExpressionForAor(RaddTransactionEntity entity) {
        Expression expressionPK = buildExpressionForPK();

        Expression expressionCommonFields = buildCommonConditionsAORAndACT(entity).build();
        return Expression.join(expressionPK, expressionCommonFields, "OR");
    }

    /**
     * Crea una espressione coi campi comuni per le operazioni sia ACT che AOR (non è inclusa la PK)
     * @param entity
     *
     * @return una espressione coi campi comuni per le operazioni sia ACT che AOR (non è inclusa la PK)
     */
    private Expression.Builder buildCommonConditionsAORAndACT(RaddTransactionEntity entity) {
        StringBuilder expressionFieldsNotPK = new StringBuilder().append(
                "fileKey = :expectedFileKey AND recipientId = :expectedRecipientId AND operation_status <> :expectedCompleted AND operation_status <> :expectedAborted");

        if(entity.getDelegateId() != null) {
            expressionFieldsNotPK.append(" AND delegateId = :expectedDelegateId");
        }

        if(entity.getOperationStartDate() != null) {
            expressionFieldsNotPK.append(" AND operationStartDate = :expectedOperationStartDate");
        }

        Expression.Builder builder = Expression.builder()
                .expression(expressionFieldsNotPK.toString())
                .putExpressionValue(":expectedFileKey", AttributeValue.builder().s(entity.getFileKey()).build())
                .putExpressionValue(":expectedRecipientId", AttributeValue.builder().s(entity.getRecipientId()).build())
                .putExpressionValue(":expectedCompleted", AttributeValue.builder().s(Const.COMPLETED).build())
                .putExpressionValue(":expectedAborted", AttributeValue.builder().s(Const.ABORTED).build());

        if(entity.getDelegateId() != null) {
            builder.putExpressionValue(":expectedDelegateId", AttributeValue.builder().s(entity.getDelegateId()).build());
        }

        if(entity.getOperationStartDate() != null) {
            builder.putExpressionValue(":expectedOperationStartDate", AttributeValue.builder().s(entity.getOperationStartDate()).build());
        }

        return builder;
    }


    @Override
    public Mono<RaddTransactionEntity> getTransaction(String cxType, String cxId, String operationId, OperationTypeEnum operationType) {
        Key key = Key.builder()
                    .partitionValue(cxType + ITEMS_SEPARATOR + cxId + ITEMS_SEPARATOR + operationId)
                    .sortValue(operationType.name())
                    .build();
        return this.findFromKey(key)
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST)));
    }

    @Override
    public Mono<RaddTransactionEntity> getTransaction(String transactionId, OperationTypeEnum operationType) {
        Key key = Key.builder()
                .partitionValue(transactionId)
                .sortValue(operationType.name())
                .build();
        return this.findFromKey(key)
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST)));
    }

    @Override
    public Mono<RaddTransactionEntity> updateStatus(RaddTransactionEntity entity, RaddTransactionStatusEnum status) {
        entity.setStatus(status.name());
        return this.updateItem(entity)
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST)))
                .filter(updated -> updated.getStatus().equals(entity.getStatus()))
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS)));
    }

    @Override
    public Mono<Integer> countFromIunAndOperationIdAndStatus(String operationId, String iun) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        String query = "(" + RaddTransactionEntity.COL_STATUS + " = :completed" + " OR " +
                RaddTransactionEntity.COL_STATUS + " = :aborted )" +
                " AND ( " + RaddTransactionEntity.COL_IUN + " = :iun)";

        expressionValues.put(":iun", AttributeValue.builder().s(iun).build());
        expressionValues.put(":operationId",  AttributeValue.builder().s(operationId).build());
        expressionValues.put(":completed",  AttributeValue.builder().s(Const.COMPLETED).build());
        expressionValues.put(":aborted",  AttributeValue.builder().s(Const.ABORTED).build());

        log.trace("COUNT DAO TICK {}", new Date().getTime());

        return this.getCounterQuery(expressionValues, query, RaddTransactionEntity.COL_OPERATION_ID + " = :operationId", null)
                .doOnNext(response -> log.trace("COUNT DAO TOCK {}", new Date().getTime()));
    }

    @Override
    public Mono<Integer> countFromQrCodeCompleted(String qrCode) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        String query = "(" + RaddTransactionEntity.COL_STATUS + " = :completed AND " + RaddTransactionEntity.COL_OPERATION_TYPE +  " = :type)";
        expressionValues.put(":completed", AttributeValue.builder().s(Const.COMPLETED).build());
        expressionValues.put(":type", AttributeValue.builder().s(OperationTypeEnum.ACT.name()).build());
        expressionValues.put(":qrcodevalue",  AttributeValue.builder().s(qrCode).build());

        log.trace("COUNT QUERY DAO TICK {}", new Date().getTime());

        return this.getCounterQuery(expressionValues, query, RaddTransactionEntity.COL_QR_CODE + " = :qrcodevalue", RaddTransactionEntity.QRCODE_SECONDARY_INDEX)
                .doOnNext(result ->  log.trace("COUNT QUERY DAO TOCK {}", new Date().getTime()))
                .doOnError(ex ->  log.trace("COUNT QUERY DAO TOCK {}", new Date().getTime()));
    }


    @Override
    public Flux<RaddTransactionEntity> getTransactionsFromIun(String iun) {
        Key key = Key.builder().partitionValue(iun).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        String index = RaddTransactionEntity.IUN_SECONDARY_INDEX;
        return this.getByFilter(conditional, index, null, null, null);
    }

    @Override
    public Flux<RaddTransactionEntity> getTransactionsFromFiscalCode(String ensureFiscalCode, Date from, Date to) {
        if (from != null && to != null && from.after(to)){
            return Flux.error(new RaddGenericException(DATE_VALIDATION_ERROR));
        }

        Map<String, AttributeValue> map = new HashMap<>();
        String query = "";

        if (from != null) {
            map.put(":fromDate", AttributeValue.builder().s(DateUtils.formatDate(from)).build());
            query = RaddTransactionEntity.COL_OPERATION_START_DATE + " >= :fromDate";
        }

        if (from != null && to != null) {
            map.put(":toDate", AttributeValue.builder().s(DateUtils.formatDate(to)).build());
            query += " AND " + RaddTransactionEntity.COL_OPERATION_START_DATE + " <= :toDate";
        }

        Key key = Key.builder().partitionValue(ensureFiscalCode).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        String indexRecipient = RaddTransactionEntity.RECIPIENT_SECONDARY_INDEX;
        String indexDelegate = RaddTransactionEntity.DELEGATE_SECONDARY_INDEX;

        return this.getByFilter(conditional, indexRecipient, map, query, null)
                .concatWith(this.getByFilter(conditional, indexDelegate, map, query, null))
                .distinct(RaddTransactionEntity::getOperationId);
    }

}
