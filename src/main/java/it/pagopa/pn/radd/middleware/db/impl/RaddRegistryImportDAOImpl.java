package it.pagopa.pn.radd.middleware.db.impl;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.RaddRegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.RaddRegistryImportStatus;
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
import java.util.Map;

@Repository
public class RaddRegistryImportDAOImpl extends BaseDao<RaddRegistryImportEntity> implements RaddRegistryImportDAO {

    public RaddRegistryImportDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                     DynamoDbAsyncClient dynamoDbAsyncClient,
                                     PnRaddFsuConfig pnRaddFsuConfig) {
        super(dynamoDbEnhancedAsyncClient,
                dynamoDbAsyncClient,
                pnRaddFsuConfig.getDao().getRaddRegistryImportTable(),
                pnRaddFsuConfig,
                RaddRegistryImportEntity.class);
    }


    @Override
    public Flux<RaddRegistryImportEntity> getRegistryImportByCxId(String xPagopaPnCxId) {
        Key key = Key.builder().partitionValue(xPagopaPnCxId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        return getByFilter(conditional, null, null, null, null, null);
    }

    @Override
    public Mono<RaddRegistryImportEntity> putRaddRegistryImportEntity(RaddRegistryImportEntity raddRegistryImportEntity) {
        return putItem(raddRegistryImportEntity);
    }

    @Override
    public Mono<RaddRegistryImportEntity> getRegistryImportByCxIdAndRequestId(String xPagopaPnCxId, String requestId) {
        Key key = Key.builder().partitionValue(xPagopaPnCxId).sortValue(requestId).build();
        return findFromKey(key);
    }


    @Override
    public Flux<RaddRegistryImportEntity> getRegistryImportByCxIdAndRequestIdFilterByStatus(String cxId, String requestId, RaddRegistryImportStatus importStatus) {
        Key key = Key.builder().partitionValue(cxId).sortValue(requestId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> map = new HashMap<>();
        map.put(":status", AttributeValue.builder().s(importStatus.name()).build());
        String filterExpression = "#status = :status";
        Map<String,String> expressionName = new HashMap<>();
        expressionName.put("#status", RaddRegistryImportEntity.COL_STATUS);
        return getByFilter(conditional, null, filterExpression,map,expressionName, null);
    }

    @Override
    public Mono<RaddRegistryImportEntity> updateStatus(RaddRegistryImportEntity entity, RaddRegistryImportStatus status, String error) {
        entity.setStatus(status.name());
        entity.setError(error);
        entity.setUpdatedAt(Instant.now());
        return updateItem(entity);
    }


    @Override
    public Flux<RaddRegistryImportEntity> findWithStatusPending() {
        Key key = Key.builder().partitionValue(RaddRegistryImportStatus.PENDING.name()).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        return getByFilter(conditional, RaddRegistryImportEntity.STATUS_INDEX, null, null, null, null);
    }

    @Override
    public Mono<RaddRegistryImportEntity> updateStatusAndTtl(RaddRegistryImportEntity entity, Long ttl, RaddRegistryImportStatus status) {
        entity.setStatus(status.name());
        entity.setTtl(ttl);
        return this.updateItem(entity);
    }
}
