package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.pojo.RaddRegistryImportStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RaddRegistryImportDAO {
    Flux<RaddRegistryImportEntity> getRegistryImportByCxId(String xPagopaPnCxId);
    Mono<RaddRegistryImportEntity> putRaddRegistryImportEntity(RaddRegistryImportEntity raddRegistryImportEntity);
    Mono<RaddRegistryImportEntity> getRegistryImportByCxIdAndRequestId(String xPagopaPnCxId, String requestId);
    Flux<RaddRegistryImportEntity> getRegistryImportByCxIdAndRequestIdFilterByStatus(String cxId, String requestId, RaddRegistryImportStatus importStatus);
    Mono<RaddRegistryImportEntity> updateStatus(RaddRegistryImportEntity entity, RaddRegistryImportStatus status, String error);
    Flux<RaddRegistryImportEntity> findWithStatusPending();
    Mono<RaddRegistryImportEntity> updateStatusAndTtl(RaddRegistryImportEntity entity, Long ttl, RaddRegistryImportStatus status);
}
