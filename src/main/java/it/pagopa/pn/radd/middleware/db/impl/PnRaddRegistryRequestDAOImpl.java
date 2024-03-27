package it.pagopa.pn.radd.middleware.db.impl;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.PnRaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.PnRaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.ImportStatus;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;


@Service
public class PnRaddRegistryRequestDAOImpl extends BaseDao<RaddRegistryRequestEntity> implements PnRaddRegistryRequestDAO {

    private static final String MISSING_STATUS = "Missing status param";

    public PnRaddRegistryRequestDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
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
    public Flux<RaddRegistryRequestEntity> findWithStatus(String correlationId, ImportStatus status) throws IllegalArgumentException {
        if (correlationId == null)
            throw new IllegalArgumentException("Missing correlationId param");
        if (status == null)
            throw new IllegalArgumentException(MISSING_STATUS);
        
        Key key = Key.builder().partitionValue(correlationId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> map = new HashMap<>();
        map.put(":status", AttributeValue.builder().s(status.name()).build());
        String query = PnRaddRegistryImportEntity.COL_STATUS + " = :status";

        return this.getByFilter(conditional, RaddRegistryRequestEntity.CORRELATIONID_INDEX, map, query, null);
    }

    @Override
    public Mono<RaddRegistryRequestEntity> updateStatusAndError(RaddRegistryRequestEntity raddRegistryRequestEntity, ImportStatus importStatus, String error) throws IllegalArgumentException {
        if (importStatus == null)
            throw new IllegalArgumentException(MISSING_STATUS);
        if (raddRegistryRequestEntity == null)
            throw new IllegalArgumentException("Missing RegistryRequest param");

        Expression expression = createExpression(raddRegistryRequestEntity);
        raddRegistryRequestEntity.setStatus(importStatus.name());
        raddRegistryRequestEntity.setError(error);
        return super.putItemWithConditions(raddRegistryRequestEntity, expression, RaddRegistryRequestEntity.class);
    }

    @Override
    public Mono<RaddRegistryRequestEntity> updateRichiesteSediRaddStatus(RaddRegistryRequestEntity entity, RegistryRequestStatus importStatus) {
        if (importStatus == null)
            throw new IllegalArgumentException(MISSING_STATUS);

        Expression expression = createExpression(entity);
        entity.setStatus(importStatus.name());
        return super.putItemWithConditions(entity, expression, RaddRegistryRequestEntity.class);
    }

    private Expression createExpression(RaddRegistryRequestEntity entity) {
        return  Expression.builder()
                .putExpressionValue(":"+RaddRegistryRequestEntity.COL_PK, AttributeValue.builder().s(entity.getPk()).build())
                .build();
    }
}
