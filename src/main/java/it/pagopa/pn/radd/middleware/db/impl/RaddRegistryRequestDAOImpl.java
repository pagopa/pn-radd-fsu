package it.pagopa.pn.radd.middleware.db.impl;

import io.micrometer.core.instrument.util.StringUtils;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.NormalizedAddressEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.ImportStatus;
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
import java.util.StringJoiner;


@Repository
@CustomLog
public class RaddRegistryRequestDAOImpl extends BaseDao<RaddRegistryRequestEntity> implements RaddRegistryRequestDAO {

    public RaddRegistryRequestDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                      DynamoDbAsyncClient dynamoDbAsyncClient,
                                      PnRaddFsuConfig raddFsuConfig) {
        super(dynamoDbEnhancedAsyncClient,
                dynamoDbAsyncClient,
                raddFsuConfig.getDao().getRaddRegistryRequestTable(),
                raddFsuConfig,
                RaddRegistryRequestEntity.class
        );
    }

    @Override
    public Flux<RaddRegistryRequestEntity> findByCorrelationIdWithStatus(String correlationId, ImportStatus status) throws IllegalArgumentException {
        Key key = Key.builder().partitionValue(correlationId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> map = new HashMap<>();

        String query = getQueryAndPopulateMapForStatusFilter(status.name(), map);

        Map<String,String> expressionName = new HashMap<>();
        expressionName.put("#status", RaddRegistryRequestEntity.COL_STATUS);

        return getByFilter(conditional, RaddRegistryRequestEntity.CORRELATIONID_INDEX, query,map,expressionName, null);
    }

    @Override
    public Mono<RaddRegistryRequestEntity> updateStatusAndError(RaddRegistryRequestEntity raddRegistryRequestEntity, ImportStatus importStatus, String error) throws IllegalArgumentException {
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

        return getByFilter(conditional, RaddRegistryRequestEntity.CORRELATIONID_INDEX, query,map,expressionName, null);
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

    private String getQueryAndPopulateMapForStatusFilter(String status, Map<String, AttributeValue> map) {
        String query = "";
        if (StringUtils.isNotEmpty(status)) {
            map.put(":status", AttributeValue.builder().s(status).build());
            query = "#status = :status";
        }
        return query;
    }

    @Override
    public Mono<ResultPaginationDto<RaddRegistryEntity, PnLastEvaluatedKey>> findAll(String xPagopaPnCxId, Integer limit, String cap, String city, String pr, String externalCode, PnLastEvaluatedKey lastEvaluatedKey) {
        log.info("Start findAll RaddRegistryEntity - xPagopaPnCxId={} and limit: [{}] and cap: [{}] and city: [{}] and pr: [{}] and externalCode: [{}].", xPagopaPnCxId, limit, cap, city, pr, externalCode);

        //Creazione key per fare la query
        Key key = Key.builder().sortValue(xPagopaPnCxId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        //Creazione query filtrata e mappa dei valori per i filtri se presenti
        Map<String, AttributeValue> map = new HashMap<>();
        StringJoiner query = new StringJoiner(" AND ");
        if (StringUtils.isNotEmpty(cap)) {
            map.put(":" + RaddRegistryEntity.COL_ZIP_CODE, AttributeValue.builder().s(cap).build());
            query.add(String.format("#%s = :%s", RaddRegistryEntity.COL_ZIP_CODE, RaddRegistryEntity.COL_ZIP_CODE));
        }
        if (StringUtils.isNotEmpty(city)) {
            map.put(":" + NormalizedAddressEntity.COL_CITY, AttributeValue.builder().s(city).build());
            query.add(String.format("#%s.%s = :%s", RaddRegistryEntity.COL_NORMALIZED_ADDRESS, NormalizedAddressEntity.COL_CITY, NormalizedAddressEntity.COL_CITY));
        }
        if (StringUtils.isNotEmpty(pr)) {
            map.put(":" + NormalizedAddressEntity.COL_PR, AttributeValue.builder().s(pr).build());
            query.add(String.format("#%s.%s = :%s", RaddRegistryEntity.COL_NORMALIZED_ADDRESS, NormalizedAddressEntity.COL_PR, NormalizedAddressEntity.COL_PR));
        }
        if (StringUtils.isNotEmpty(externalCode)) {
            map.put(":" + RaddRegistryEntity.COL_EXTERNAL_CODE, AttributeValue.builder().s(externalCode).build());
            query.add(String.format("#%s = :%s", RaddRegistryEntity.COL_EXTERNAL_CODE, RaddRegistryEntity.COL_EXTERNAL_CODE));
        }

        ResultPaginationDto<RaddRegistryRequestDAO, PnLastEvaluatedKey> resultPaginationDto = new ResultPaginationDto<>();
        //FIXME va bene mettere CXID_REQUESTID_INDEX anche se faccio la query solamente con la sortKey?
        return getByFilterPaginated(conditional, RaddRegistryEntity.CXID_REQUESTID_INDEX, map, query.toString(), limit,  lastEvaluatedKey.getInternalLastEvaluatedKey());
    }

    @Override
    public Mono<ResultPaginationDto<RaddRegistryRequestEntity, PnLastEvaluatedKey>> getRegistryByCxIdAndRequestId(String xPagopaPnCxId, String requestId, Integer limit, PnLastEvaluatedKey lastEvaluatedKey) {
        Key key = Key.builder().partitionValue(xPagopaPnCxId).sortValue(requestId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        ResultPaginationDto<RaddRegistryRequestDAO, PnLastEvaluatedKey> resultPaginationDto = new ResultPaginationDto<>();
        return getByFilterPaginated(conditional, RaddRegistryRequestEntity.CXID_REQUESTID_INDEX, null, null, limit,  lastEvaluatedKey.getInternalLastEvaluatedKey());
    }
}
