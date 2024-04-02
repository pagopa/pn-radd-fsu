package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.ImportStatus;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PnRaddRegistryRequestDAO {

    Flux<RaddRegistryRequestEntity> findByCorrelationIdWithStatus(String cxId, ImportStatus status) throws IllegalArgumentException;

    Mono<RaddRegistryRequestEntity> updateStatusAndError(RaddRegistryRequestEntity richiesteSediRaddItem, ImportStatus importStatus, String error) throws IllegalArgumentException;

    Mono<RaddRegistryRequestEntity> updateRichiesteSediRaddStatus(RaddRegistryRequestEntity id, RegistryRequestStatus importStatus);
}
