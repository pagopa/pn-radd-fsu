package it.pagopa.pn.radd.middleware.db.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.instrument.util.StringUtils;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.PnLastEvaluatedKey;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import it.pagopa.pn.radd.pojo.ResultPaginationDto;
import lombok.CustomLog;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.radd.pojo.PnLastEvaluatedKey.ERROR_CODE_PN_RADD_ALT_UNSUPPORTED_LAST_EVALUATED_KEY;


@Repository
@CustomLog
public class RaddRegistryRequestDAOImpl extends BaseDao<RaddRegistryRequestEntity> implements RaddRegistryRequestDAO {

    private final PnRaddFsuConfig pnRaddFsuConfig;
    public RaddRegistryRequestDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                      DynamoDbAsyncClient dynamoDbAsyncClient,
                                      PnRaddFsuConfig raddFsuConfig) {
        super(dynamoDbEnhancedAsyncClient,
                dynamoDbAsyncClient,
                raddFsuConfig.getDao().getRaddRegistryRequestTable(),
                raddFsuConfig,
                RaddRegistryRequestEntity.class
        );
        pnRaddFsuConfig = raddFsuConfig;
    }

    @Override
    public Flux<RaddRegistryRequestEntity> findByCorrelationIdWithStatus(String correlationId, RegistryRequestStatus status) throws IllegalArgumentException {
        Key key = Key.builder().partitionValue(correlationId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> map = new HashMap<>();

        String query = getQueryAndPopulateMapForStatusFilter(status.name(), map);

        Map<String,String> expressionName = new HashMap<>();
        expressionName.put("#status", RaddRegistryRequestEntity.COL_STATUS);

        return getByFilter(conditional, RaddRegistryRequestEntity.CORRELATIONID_INDEX, query,map,expressionName, null);
    }

    @Override
    public Mono<RaddRegistryRequestEntity> updateStatusAndError(RaddRegistryRequestEntity raddRegistryRequestEntity, RegistryRequestStatus importStatus, String error) throws IllegalArgumentException {
        raddRegistryRequestEntity.setStatus(importStatus.name());
        raddRegistryRequestEntity.setError(error);
        raddRegistryRequestEntity.setUpdatedAt(Instant.now());
        return putItem(raddRegistryRequestEntity);
    }

    @Override
    public Mono<RaddRegistryRequestEntity> updateRegistryRequestStatus(RaddRegistryRequestEntity entity, RegistryRequestStatus importStatus) {
        entity.setStatus(importStatus.name());
        entity.setUpdatedAt(Instant.now());
        return putItem(entity);
    }

    @Override
    public Flux<RaddRegistryRequestEntity> getAllFromCorrelationId(String correlationId, String state) {
        Key key = Key.builder().partitionValue(correlationId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> map = new HashMap<>();
        String query = getQueryAndPopulateMapForStatusFilter(state, map);

        Map<String,String> expressionName = new HashMap<>();
        expressionName.put("#status", RaddRegistryRequestEntity.COL_STATUS);

        return getByFilter(conditional, RaddRegistryRequestEntity.CORRELATIONID_INDEX, query, map, expressionName, null);
    }

    @Override
    public Mono<Void> updateRecordsInPending(List<RaddRegistryRequestEntity> addresses) {
        addresses.forEach(address -> address.setStatus(RegistryRequestStatus.PENDING.name()));
        return this.transactWriteItems(addresses, RaddRegistryRequestEntity.class);
    }

    @Override
    public Flux<RaddRegistryRequestEntity> findByCxIdAndRequestIdAndStatusNotIn(String cxId, String requestId, List<RegistryRequestStatus> statusList) {
        Key key = Key.builder().partitionValue(cxId).sortValue(requestId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> valueMap = new HashMap<>();

        String query = addStatusFilterExpression(valueMap, statusList);

        Map<String,String> expressionName = new HashMap<>();
        expressionName.put("#status", RaddRegistryRequestEntity.COL_STATUS);

        return getByFilter(conditional, RaddRegistryRequestEntity.CXID_REQUESTID_INDEX, query, valueMap, expressionName, null);
    }

    @Override
    public Flux<RaddRegistryRequestEntity> getAllFromCxidAndRequestIdWithState(String cxId, String requestId, String state) {
        Key key = Key.builder().partitionValue(cxId).sortValue(requestId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> map = new HashMap<>();

        Map<String,String> expressionName = new HashMap<>();
        expressionName.put("#status", RaddRegistryRequestEntity.COL_STATUS);

        String query = getQueryAndPopulateMapForStatusFilter(state, map);

        return getAllPaginatedItems(conditional, RaddRegistryRequestEntity.CXID_REQUESTID_INDEX, query,map,expressionName, pnRaddFsuConfig.getMaxQuerySize())
                .flatMapIterable(page -> page);
    }


    private String addStatusFilterExpression(Map<String, AttributeValue> valueMap, List<RegistryRequestStatus> status) {
        List<String> statusPlaceHolders = status.stream().map(registryRequestStatus -> {
            String statusPlaceHolder = ":status" + registryRequestStatus.name();
            valueMap.put(statusPlaceHolder, AttributeValue.builder().s(registryRequestStatus.name()).build());
            return statusPlaceHolder;
        }).toList();

        String query = " NOT #status IN (" + String.join(",", statusPlaceHolders) + ")";
        log.info("query: {}", query);
        return query;
    }

    @Override
    public Mono<RaddRegistryRequestEntity> createEntity(RaddRegistryRequestEntity entity) {
        return this.putItem(entity);
    }

    private String getQueryAndPopulateMapForStatusFilter(String status, Map<String, AttributeValue> map) {
        String query = "";
        if (StringUtils.isNotEmpty(status)) {
            map.put(":status", AttributeValue.builder().s(status).build());
            query = "#status = :status";
        }
        return query;
    }

    @Override
    public Mono<Void> writeCsvAddresses(List<RaddRegistryRequestEntity> raddRegistryRequestEntities, String correlationId) {
        raddRegistryRequestEntities.forEach(raddRegistryRequestEntity -> raddRegistryRequestEntity.setCorrelationId(correlationId));
        return batchWriter(raddRegistryRequestEntities, 0, true);
    }

    @Override
    public Mono<ResultPaginationDto<RaddRegistryRequestEntity, PnLastEvaluatedKey>> getRegistryByCxIdAndRequestId(String xPagopaPnCxId, String requestId, Integer limit, String lastEvaluatedKey) {
        PnLastEvaluatedKey lastKey = null;
        if (lastEvaluatedKey != null) {
            try {
                lastKey = PnLastEvaluatedKey.deserializeInternalLastEvaluatedKey(lastEvaluatedKey);
            } catch (JsonProcessingException e) {
                throw new PnInternalException("Unable to deserialize lastEvaluatedKey",
                        ERROR_CODE_PN_RADD_ALT_UNSUPPORTED_LAST_EVALUATED_KEY,
                        e);
            }
        } else {
            log.debug("First page search");
        }

        Key key = Key.builder().partitionValue(xPagopaPnCxId).sortValue(requestId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        return getByFilterPaginated(conditional, RaddRegistryRequestEntity.CXID_REQUESTID_INDEX, null, null, limit,  lastEvaluatedKey == null ? null : lastKey.getInternalLastEvaluatedKey());
    }
}
