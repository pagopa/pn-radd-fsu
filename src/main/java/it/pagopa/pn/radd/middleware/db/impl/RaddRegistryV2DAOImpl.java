package it.pagopa.pn.radd.middleware.db.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.exception.RaddRegistryAlreadyExistsException;
import it.pagopa.pn.radd.exception.TransactionAlreadyExistsException;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.RaddRegistryV2DAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntityV2;
import it.pagopa.pn.radd.pojo.PnLastEvaluatedKey;
import it.pagopa.pn.radd.pojo.RaddRegistryPage;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.ERROR_CODE_PN_RADD_ALT_UNSUPPORTED_LAST_EVALUATED_KEY;
import static it.pagopa.pn.radd.utils.DateUtils.getStartOfDayToday;

@Repository
@CustomLog
public class RaddRegistryV2DAOImpl extends BaseDao<RaddRegistryEntityV2> implements RaddRegistryV2DAO {

    public RaddRegistryV2DAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                 DynamoDbAsyncClient dynamoDbAsyncClient,
                                 PnRaddFsuConfig raddFsuConfig) {
        super(dynamoDbEnhancedAsyncClient,
              dynamoDbAsyncClient,
              raddFsuConfig.getDao().getRaddRegistryTableV2(),
              raddFsuConfig,
              RaddRegistryEntityV2.class
             );
    }

    @Override
    public Mono<RaddRegistryEntityV2> find(String partnerId, String locationId) {
        Key key = Key.builder().partitionValue(partnerId).sortValue(locationId).build();
        return findFromKey(key);
    }

    @Override
    public Mono<RaddRegistryEntityV2> updateRegistryEntity(RaddRegistryEntityV2 registryEntity) {
        return this.updateItem(registryEntity);
    }

    @Override
    public Mono<RaddRegistryEntityV2> putItemIfAbsent(RaddRegistryEntityV2 newRegistry) {
        Expression condition = Expression.builder()
                                         .expression("attribute_not_exists(partnerId) AND attribute_not_exists(locationId)")
                                         .build();

        return this.putItemWithConditions(newRegistry, condition, RaddRegistryEntityV2.class)
                   .onErrorMap(TransactionAlreadyExistsException.class, e -> new RaddRegistryAlreadyExistsException());
    }

    @Override
    public Flux<RaddRegistryEntityV2> findByPartnerId(String partnerId) {
        Key key = Key.builder().partitionValue(partnerId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        return getByFilter(conditional, null, null, null, null, null);
    }

    @Override
    public Mono<RaddRegistryPage> findPaginatedByPartnerId(String partnerId, Integer limit, String lastKey) {
        Key key = Key.builder().partitionValue(partnerId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> lastEvaluatedKey = new HashMap<>();

        QueryEnhancedRequest.Builder qeRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(conditional);

        if (limit != null) {
            qeRequest.limit(limit);
        }

        if (StringUtils.isNotBlank(lastKey)) {
            lastEvaluatedKey = Map.of(RaddRegistryEntityV2.COL_PARTNER_ID, AttributeValue.builder().s(partnerId).build(),RaddRegistryEntityV2.COL_LOCATION_ID, AttributeValue.builder().s(lastKey).build());
        }

        return constructAndExecuteQuery(qeRequest, lastEvaluatedKey, null)
                .map(page -> {
                    RaddRegistryPage raddRegistryPage = new RaddRegistryPage();
                    raddRegistryPage.setItems(page.items());
                    raddRegistryPage.setLastKey(CollectionUtils.isEmpty(page.lastEvaluatedKey()) ? null : page.lastEvaluatedKey().get(RaddRegistryEntityV2.COL_LOCATION_ID).s());
                    return raddRegistryPage;
                });
    }

    @Override
    public Mono<Page<RaddRegistryEntityV2>> scanRegistries(Integer limit, String lastKey) {
        log.info("Start scan RaddRegistryEntity - limit: [{}] and lastKey: [{}].", limit, lastKey);

        Map<String, String> names = new HashMap<>();
        names.put("#endValidity", RaddRegistryEntity.COL_END_VALIDITY);
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":today", AttributeValue.builder().s(String.valueOf(getStartOfDayToday())).build());
        String query = ":today < #endValidity OR attribute_not_exists(#endValidity)";

        PnLastEvaluatedKey lastEvaluatedKey = null;
        if (io.micrometer.core.instrument.util.StringUtils.isNotEmpty(lastKey)) {
            try {
                lastEvaluatedKey = PnLastEvaluatedKey.deserializeInternalLastEvaluatedKey(lastKey);
            } catch (JsonProcessingException e) {
                throw new RaddGenericException(
                        ERROR_CODE_PN_RADD_ALT_UNSUPPORTED_LAST_EVALUATED_KEY,
                        HttpStatus.BAD_REQUEST);
            }
        } else {
            log.debug("First page search");
        }

        return scan(limit, lastEvaluatedKey != null ? lastEvaluatedKey.getInternalLastEvaluatedKey() : null, values, query, names);
    }

    @Override
    public Mono<RaddRegistryEntityV2> delete(String partnerId, String locationId) {
        return deleteItem(Key.builder().partitionValue(partnerId).sortValue(locationId).build());
    }
}