package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntityV2;
import it.pagopa.pn.radd.pojo.RaddRegistryPage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

public interface RaddRegistryV2DAO {

    Mono<RaddRegistryEntityV2> find(String partnerId, String locationId);

    Mono<RaddRegistryEntityV2> updateRegistryEntity(RaddRegistryEntityV2 registryEntity);

    Mono<RaddRegistryEntityV2> putItemIfAbsent(RaddRegistryEntityV2 newItem);

    Flux<RaddRegistryEntityV2> findByPartnerId(String partnerId);

    Mono<RaddRegistryPage> findPaginatedByPartnerId(String partnerId, Integer limit, String lastKey);

    Mono<Page<RaddRegistryEntityV2>> scanRegistries(Integer limit, String lastKey);

    Mono<RaddRegistryEntityV2> delete(String partnerId, String locationId);

}
