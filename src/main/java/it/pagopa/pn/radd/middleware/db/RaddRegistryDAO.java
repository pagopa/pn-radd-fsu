package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.pojo.ResultPaginationDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RaddRegistryDAO {

    Mono<RaddRegistryEntity> find(String registryId, String cxId);

    Mono<RaddRegistryEntity> updateRegistryEntity(RaddRegistryEntity registryEntity);

    Mono<RaddRegistryEntity> putItemIfAbsent(RaddRegistryEntity newItem);

    Flux<RaddRegistryEntity> findByCxIdAndRequestId(String cxId, String requestId);

    Flux<RaddRegistryEntity> findPaginatedByCxIdAndRequestId(String cxId, String requestId);

    Flux<RaddRegistryEntity> getRegistriesByZipCode(String zipCode);

    Mono<ResultPaginationDto<RaddRegistryEntity, String>> findByFilters(String xPagopaPnCxId, Integer limit, String cap, String city, String pr, String externalCode, String lastEvaluatedKey);

}
