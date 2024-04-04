package it.pagopa.pn.radd.middleware.db.impl;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.RaddRegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.pojo.ImportStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

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
        return getByFilter(conditional, null, null, null, null);
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
    public Flux<RaddRegistryImportEntity> getRegistryImportByCxIdAndRequestIdFilterByStatus(String cxId, String requestId, ImportStatus importStatus) {
        Key key = Key.builder().partitionValue(cxId).sortValue(requestId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> map = new HashMap<>();
        map.put(":status", AttributeValue.builder().s(importStatus.name()).build());
        String filterExpression = RaddRegistryImportEntity.COL_STATUS + " = :status";

        return getByFilter(conditional, null, map, filterExpression, null);
    }
}
