package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.PnRaddRegistryImportEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RegistryImportDAO {
    Flux<PnRaddRegistryImportEntity> getRegistryImportByCxId(String xPagopaPnCxId);
    Mono<PnRaddRegistryImportEntity> putRaddRegistryImportEntity(PnRaddRegistryImportEntity pnRaddRegistryImportEntity);
}
