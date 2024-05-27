package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistriesStoreResponse;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.pojo.PnLastEvaluatedKey;
import it.pagopa.pn.radd.utils.RaddRegistryUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@CustomLog
public class StoreLocatorService {

    private final RaddRegistryDAO raddRegistryDAO;
    private final RaddRegistryUtils raddRegistryUtils;

    private final static Function<Map<String, AttributeValue>, PnLastEvaluatedKey> STORE_REGISTRY_LAST_EVALUATED_KEY = (stringAttributeValueMap) -> {
        PnLastEvaluatedKey pageLastEvaluatedKey = new PnLastEvaluatedKey();
        pageLastEvaluatedKey.setExternalLastEvaluatedKey(stringAttributeValueMap.get(RaddRegistryEntity.COL_CXID).s());
        pageLastEvaluatedKey.setInternalLastEvaluatedKey(Map.of(
                RaddRegistryEntity.COL_REGISTRY_ID, AttributeValue.builder().s(stringAttributeValueMap.get(RaddRegistryEntity.COL_REGISTRY_ID).s()).build(),
                RaddRegistryEntity.COL_CXID, AttributeValue.builder().s(stringAttributeValueMap.get(RaddRegistryEntity.COL_CXID).s()).build()
        ));
        return pageLastEvaluatedKey;
    };


    public Mono<RegistriesStoreResponse> retrieveStoreRegistries(Integer limit, String lastKey) {
        log.info("Retrieving store registries with limit {} and lastKey {}", limit, lastKey);
        return raddRegistryDAO.scanRegistries(limit, lastKey)
                .map(registries -> new RegistriesStoreResponse().lastKey(STORE_REGISTRY_LAST_EVALUATED_KEY.apply(registries.lastEvaluatedKey()).serializeInternalLastEvaluatedKey())
                        .registries(raddRegistryUtils.mapRegistryEntityToRegistryStore(registries.items())));
    }

}
