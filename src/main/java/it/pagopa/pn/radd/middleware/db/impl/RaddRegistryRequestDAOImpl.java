package it.pagopa.pn.radd.middleware.db.impl;

import io.micrometer.core.instrument.util.StringUtils;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RaddRegistryRequestDAOImpl extends BaseDao<RaddRegistryRequestEntity> implements RaddRegistryRequestDAO {
    public RaddRegistryRequestDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                  DynamoDbAsyncClient dynamoDbAsyncClient,
                                  PnRaddFsuConfig pnRaddFsuConfig) {
        super(dynamoDbEnhancedAsyncClient,
                dynamoDbAsyncClient,
                pnRaddFsuConfig.getDao().getRaddRegistryRequestTable(),
                pnRaddFsuConfig,
                RaddRegistryRequestEntity.class);
    }

    @Override
    public Flux<RaddRegistryRequestEntity> getAllFromCorrelationId(String correlationId, String state) {
        Key key = Key.builder().partitionValue(correlationId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        String index = RaddRegistryRequestEntity.CORRELATIONID_INDEX;

        Map<String, AttributeValue> map = new HashMap<>();
        String query = "";

        if (StringUtils.isNotEmpty(state)) {
            map.put(":"+RaddRegistryRequestEntity.COL_STATUS, AttributeValue.builder().s(state).build());
            query = RaddRegistryRequestEntity.COL_STATUS + " = :"+RaddRegistryRequestEntity.COL_STATUS ;
        }

        return this.getByFilter(conditional, index, map, query, null);

    }

    @Override
    public Mono<Void> updateRecordsInPendig(List<RaddRegistryRequestEntity> addresses) {
        addresses.forEach(address -> address.setStatus(RegistryRequestStatus.PENDING.name()));
        return this.transactWriteItems(addresses, RaddRegistryRequestEntity.class);
    }
}
