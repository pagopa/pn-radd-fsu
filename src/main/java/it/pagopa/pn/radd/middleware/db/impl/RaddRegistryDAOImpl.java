package it.pagopa.pn.radd.middleware.db.impl;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.MISSING_REQUIRED_PARAMETER;


@Repository
@Slf4j
public class RaddRegistryDAOImpl extends BaseDao<RaddRegistryEntity> implements RaddRegistryDAO {

    public RaddRegistryDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
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
    public Mono<RaddRegistryEntity> find(String registryId, String cxId) {
        Key key = Key.builder().partitionValue(registryId).sortValue(cxId).build();

        return findFromKey(key);
    }

    @Override
    public Mono<RaddRegistryEntity> updateRegistryEntity(RaddRegistryEntity registryEntity) {
        if (registryEntity == null || StringUtils.isBlank(registryEntity.getRegistryId()) || StringUtils.isBlank(registryEntity.getCxId()))
            throw new RaddGenericException(MISSING_REQUIRED_PARAMETER, HttpStatus.BAD_REQUEST.value());

        return this.updateItem(registryEntity);
    }

    @Override
    public Mono<RaddRegistryEntity> putItemIfAbsent(RaddRegistryEntity newRegistry) {
        if (newRegistry == null || StringUtils.isBlank(newRegistry.getRegistryId()) || StringUtils.isBlank(newRegistry.getCxId())) {
            throw new IllegalArgumentException();
        }

        Expression condition = Expression.builder()
                .expression("attribute_not_exists(registryId) AND attribute_not_exists(cxId)")
                .build();

        return this.putItemWithConditions(newRegistry, condition, RaddRegistryEntity.class);
    }
}
