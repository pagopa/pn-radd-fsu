package it.pagopa.pn.radd.middleware.db.impl;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.RegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.entities.PnRaddRegistryImportEntity;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Repository
public class RegistryImportDAOImpl extends BaseDao<PnRaddRegistryImportEntity> implements RegistryImportDAO {

    public RegistryImportDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                 DynamoDbAsyncClient dynamoDbAsyncClient,
                                 PnRaddFsuConfig pnRaddFsuConfig) {
        super(dynamoDbEnhancedAsyncClient,
                dynamoDbAsyncClient,
                pnRaddFsuConfig.getDao().getRaddRegistryImportTable(),
                pnRaddFsuConfig,
                PnRaddRegistryImportEntity.class);
    }


    @Override
    public Flux<PnRaddRegistryImportEntity> getRegistryImportByCxId(String xPagopaPnCxId) {
        Key key = Key.builder().partitionValue(xPagopaPnCxId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        return this.getByFilter(conditional, null, null, null, null);
    }

    @Override
    public Mono<PnRaddRegistryImportEntity> putRaddRegistryImportEntity(PnRaddRegistryImportEntity pnRaddRegistryImportEntity) {
       return super.putItem(pnRaddRegistryImportEntity);
    }
}
