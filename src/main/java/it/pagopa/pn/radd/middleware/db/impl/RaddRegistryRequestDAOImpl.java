package it.pagopa.pn.radd.middleware.db.impl;

import io.micrometer.core.instrument.util.StringUtils;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.ImportStatus;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import lombok.extern.slf4j.Slf4j;
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


@Repository
@Slf4j
public class RaddRegistryRequestDAOImpl extends BaseDao<RaddRegistryRequestEntity> implements RaddRegistryRequestDAO {

    private static final String MISSING_STATUS = "Missing status param";

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
        if (correlationId == null)
            throw new IllegalArgumentException("Missing correlationId param");
        if (status == null)
            throw new IllegalArgumentException(MISSING_STATUS);

        Key key = Key.builder().partitionValue(correlationId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> map = new HashMap<>();
        map.put(":status", AttributeValue.builder().s(status.name()).build());
        String query = RaddRegistryImportEntity.COL_STATUS + " = :status";

        return getByFilter(conditional, RaddRegistryRequestEntity.CORRELATIONID_INDEX, map, query, null);
    }

    @Override
    public Mono<RaddRegistryRequestEntity> updateStatusAndError(RaddRegistryRequestEntity raddRegistryRequestEntity, ImportStatus importStatus, String error) throws IllegalArgumentException {
        if (importStatus == null)
            throw new IllegalArgumentException(MISSING_STATUS);
        if (raddRegistryRequestEntity == null)
            throw new IllegalArgumentException("Missing RegistryRequest param");

        raddRegistryRequestEntity.setStatus(importStatus.name());
        raddRegistryRequestEntity.setError(error);
        raddRegistryRequestEntity.setUpdatedAt(Instant.now());
        return putItem(raddRegistryRequestEntity);
    }

    @Override
    public Mono<RaddRegistryRequestEntity> updateRegistryRequestStatus(RaddRegistryRequestEntity entity, RegistryRequestStatus importStatus) {
        if (importStatus == null)
            throw new IllegalArgumentException(MISSING_STATUS);

        entity.setStatus(importStatus.name());
        entity.setUpdatedAt(Instant.now());
        return putItem(entity);
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
