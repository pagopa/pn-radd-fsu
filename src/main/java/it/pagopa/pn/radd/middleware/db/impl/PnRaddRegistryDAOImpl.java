package it.pagopa.pn.radd.middleware.db.impl;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.PnRaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;


@Component
public class PnRaddRegistryDAOImpl extends BaseDao<RaddRegistryEntity> implements PnRaddRegistryDAO {

    public PnRaddRegistryDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                 DynamoDbAsyncClient dynamoDbAsyncClient,
                                 PnRaddFsuConfig raddFsuConfig) {
        super(dynamoDbEnhancedAsyncClient,
                dynamoDbAsyncClient,
                raddFsuConfig.getDao().getRaddRegistryTable(),
                raddFsuConfig,
                RaddRegistryEntity.class
        );
    }

    @Override
    public Flux<RaddRegistryEntity> find(String registryId, String cxId) {
        Key key = Key.builder().partitionValue(registryId).sortValue(cxId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        return this.getByFilter(conditional, RaddRegistryEntity.CXID_REQUESTID_INDEX, null, null, null);
    }

    @Override
    public Mono<RaddRegistryEntity> updateRegistryEntity(RaddRegistryEntity registryEntity) {
        if (registryEntity == null || StringUtils.isBlank(registryEntity.getRegistryId()) || StringUtils.isBlank(registryEntity.getCxId()))
            throw new IllegalArgumentException();

        return this.updateItem(registryEntity);
    }

    @Override
    public Mono<RaddRegistryEntity> createNewRegistryEntity(RaddRegistryEntity newRegistry) {
        if (newRegistry == null || StringUtils.isBlank(newRegistry.getRegistryId()) || StringUtils.isBlank(newRegistry.getCxId()))
            throw new IllegalArgumentException();

        return this.putItem(newRegistry);
    }
}
