package it.pagopa.pn.radd.middleware.db.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.instrument.util.StringUtils;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.entities.NormalizedAddressEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.PnLastEvaluatedKey;
import it.pagopa.pn.radd.pojo.ResultPaginationDto;
import lombok.CustomLog;
import org.springframework.stereotype.Repository;
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
import java.util.StringJoiner;
import java.util.function.Function;

import static it.pagopa.pn.radd.pojo.PnLastEvaluatedKey.ERROR_CODE_PN_RADD_ALT_UNSUPPORTED_LAST_EVALUATED_KEY;
import static it.pagopa.pn.radd.utils.Const.REQUEST_ID_PREFIX;
import static it.pagopa.pn.radd.utils.DateUtils.getStartOfDayToday;


@Repository
@CustomLog
public class RaddRegistryDAOImpl extends BaseDao<RaddRegistryEntity> implements RaddRegistryDAO {

    private final PnRaddFsuConfig pnRaddFsuConfig;

    public RaddRegistryDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                               DynamoDbAsyncClient dynamoDbAsyncClient,
                               PnRaddFsuConfig raddFsuConfig) {
        super(dynamoDbEnhancedAsyncClient,
                dynamoDbAsyncClient,
                raddFsuConfig.getDao().getRaddRegistryTable(),
                raddFsuConfig,
                RaddRegistryEntity.class
        );
        pnRaddFsuConfig = raddFsuConfig;
    }

    private final static Function<RaddRegistryEntity, PnLastEvaluatedKey> SELF_REGISTRY_REQUEST_LAST_EVALUATED_KEY_MAKER = (keyEntity) -> {
        PnLastEvaluatedKey pageLastEvaluatedKey = new PnLastEvaluatedKey();
        pageLastEvaluatedKey.setExternalLastEvaluatedKey(keyEntity.getCxId());
        pageLastEvaluatedKey.setInternalLastEvaluatedKey(Map.of(
                RaddRegistryEntity.COL_REGISTRY_ID, AttributeValue.builder().s(keyEntity.getRegistryId()).build(),
                RaddRegistryEntity.COL_CXID, AttributeValue.builder().s(keyEntity.getCxId()).build(),
                RaddRegistryEntity.COL_REQUEST_ID, AttributeValue.builder().s(keyEntity.getRequestId()).build()
        ));
        return pageLastEvaluatedKey;
    };

    @Override
    public Mono<RaddRegistryEntity> find(String registryId, String cxId) {
        Key key = Key.builder().partitionValue(registryId).sortValue(cxId).build();
        return findFromKey(key);
    }

    @Override
    public Mono<RaddRegistryEntity> updateRegistryEntity(RaddRegistryEntity registryEntity) {
        return this.updateItem(registryEntity);
    }

    @Override
    public Mono<RaddRegistryEntity> putItemIfAbsent(RaddRegistryEntity newRegistry) {

        Expression condition = Expression.builder()
                .expression("attribute_not_exists(registryId) AND attribute_not_exists(cxId)")
                .build();

        return this.putItemWithConditions(newRegistry, condition, RaddRegistryEntity.class);
    }

    @Override
    public Flux<RaddRegistryEntity> findByCxIdAndRequestId(String cxId, String requestId) {
        Key key = Key.builder().partitionValue(cxId).sortValue(requestId).build();
        QueryConditional conditional = requestId.startsWith(REQUEST_ID_PREFIX) ? QueryConditional.sortBeginsWith(key) : QueryConditional.keyEqualTo(key);

        return getByFilter(conditional, RaddRegistryEntity.CXID_REQUESTID_INDEX, null, null, null, null);
    }

    @Override
    public Flux<RaddRegistryEntity> findPaginatedByCxIdAndRequestId(String cxId, String requestId) {
        Key key = Key.builder().partitionValue(cxId).sortValue(requestId).build();
        QueryConditional conditional = requestId.startsWith(REQUEST_ID_PREFIX) ? QueryConditional.sortBeginsWith(key) : QueryConditional.keyEqualTo(key);
        return getAllPaginatedItems(conditional, RaddRegistryRequestEntity.CXID_REQUESTID_INDEX, null, null, null, pnRaddFsuConfig.getMaxQuerySize())
                .flatMapIterable(page -> page);
    }

    @Override
    public Flux<RaddRegistryEntity> getRegistriesByZipCode(String zipCode) {
        Key key = Key.builder().partitionValue(zipCode).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        String index = RaddRegistryEntity.ZIPCODE_INDEX;

        Map<String, String> names = new HashMap<>();
        names.put("#endValidity", RaddRegistryEntity.COL_END_VALIDITY);
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":today", AttributeValue.builder().s(String.valueOf(getStartOfDayToday())).build());
        String expression = "attribute_not_exists(#endValidity) OR #endValidity >= :today";

        return this.getByFilter(conditional, index, expression, values, names, null);
    }

    @Override
    public Mono<ResultPaginationDto<RaddRegistryEntity, String>> findByFilters(String xPagopaPnCxId, Integer limit, String cap, String city, String pr, String externalCode, String lastKey) {
        log.info("Start findAll RaddRegistryEntity - xPagopaPnCxId={} and limit: [{}] and cap: [{}] and city: [{}] and pr: [{}] and externalCode: [{}].", xPagopaPnCxId, limit, cap, city, pr, externalCode);

        PnLastEvaluatedKey lastEvaluatedKey = null;
        if (StringUtils.isNotEmpty(lastKey)) {
            try {
                lastEvaluatedKey = PnLastEvaluatedKey.deserializeInternalLastEvaluatedKey(lastKey);
            } catch (JsonProcessingException e) {
                throw new PnInternalException("Unable to deserialize lastEvaluatedKey",
                        ERROR_CODE_PN_RADD_ALT_UNSUPPORTED_LAST_EVALUATED_KEY,
                        e);
            }
        } else {
            log.debug("First page search");
        }

        //Creazione key per fare la query
        Key key = Key.builder().partitionValue(xPagopaPnCxId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        //Creazione query filtrata e mappa dei valori per i filtri se presenti
        Map<String, AttributeValue> map = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        StringJoiner query = new StringJoiner(" AND ");
        if (io.micrometer.core.instrument.util.StringUtils.isNotEmpty(cap)) {
            map.put(":" + RaddRegistryEntity.COL_ZIP_CODE, AttributeValue.builder().s(cap).build());
            names.put("#" + RaddRegistryEntity.COL_ZIP_CODE, RaddRegistryEntity.COL_ZIP_CODE);
            query.add(String.format("#%s = :%s", RaddRegistryEntity.COL_ZIP_CODE, RaddRegistryEntity.COL_ZIP_CODE));
        }
        if (io.micrometer.core.instrument.util.StringUtils.isNotEmpty(city)) {
            map.put(":" + NormalizedAddressEntity.COL_CITY, AttributeValue.builder().s(city).build());
            names.put("#" + RaddRegistryEntity.COL_NORMALIZED_ADDRESS, RaddRegistryEntity.COL_NORMALIZED_ADDRESS);
            names.put("#" + NormalizedAddressEntity.COL_CITY, NormalizedAddressEntity.COL_CITY);
            query.add(String.format("#%s.#%s = :%s", RaddRegistryEntity.COL_NORMALIZED_ADDRESS, NormalizedAddressEntity.COL_CITY, NormalizedAddressEntity.COL_CITY));
        }
        if (io.micrometer.core.instrument.util.StringUtils.isNotEmpty(pr)) {
            map.put(":" + NormalizedAddressEntity.COL_PR, AttributeValue.builder().s(pr).build());
            names.put("#" + NormalizedAddressEntity.COL_PR, NormalizedAddressEntity.COL_PR);
            names.put("#" + RaddRegistryEntity.COL_NORMALIZED_ADDRESS, RaddRegistryEntity.COL_NORMALIZED_ADDRESS);
            query.add(String.format("#%s.#%s = :%s", RaddRegistryEntity.COL_NORMALIZED_ADDRESS, NormalizedAddressEntity.COL_PR, NormalizedAddressEntity.COL_PR));
        }
        if (StringUtils.isNotEmpty(externalCode)) {
            map.put(":" + RaddRegistryEntity.COL_EXTERNAL_CODE, AttributeValue.builder().s(externalCode).build());
            names.put("#" + RaddRegistryEntity.COL_EXTERNAL_CODE, RaddRegistryEntity.COL_EXTERNAL_CODE);
            query.add(String.format("#%s = :%s", RaddRegistryEntity.COL_EXTERNAL_CODE, RaddRegistryEntity.COL_EXTERNAL_CODE));
        }

        return getByFilterPaginated(conditional, RaddRegistryEntity.CXID_REQUESTID_INDEX, map, names, query.toString(), limit, lastEvaluatedKey != null ? lastEvaluatedKey.getInternalLastEvaluatedKey() : null, SELF_REGISTRY_REQUEST_LAST_EVALUATED_KEY_MAKER);
    }
}
