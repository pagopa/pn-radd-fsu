package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PnRaddRegistryDAO {

    Flux<RaddRegistryEntity> find(String registryId, String cxId);

    Mono<RaddRegistryEntity> updateRegistryEntity(RaddRegistryEntity registryEntity);

    Mono<RaddRegistryEntity> createNewRegistryEntity(RaddRegistryEntity newItem);
}
