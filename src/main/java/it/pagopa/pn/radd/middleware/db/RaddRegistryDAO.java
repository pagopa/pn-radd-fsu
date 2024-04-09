package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RaddRegistryDAO {

    Mono<RaddRegistryEntity> find(String registryId, String cxId);

    Mono<RaddRegistryEntity> updateRegistryEntity(RaddRegistryEntity registryEntity);

    Mono<RaddRegistryEntity> putItemIfAbsent(RaddRegistryEntity newItem);

    Flux<RaddRegistryEntity> findByCxIdAndRequestId(String cxId, String requestId);

    Flux<RaddRegistryEntity> getRegistriesByZipCode(String zipCode);
}
