package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RaddRegistryRequestDAO {

    Flux<RaddRegistryRequestEntity> getAllFromCorrelationId(String correlationId, String state);

    Flux<RaddRegistryRequestEntity> getAllFromCxidAndRequestIdWithState(String cxId, String requestId, String state);

    Flux<RaddRegistryRequestEntity> findByCorrelationIdWithStatus(String cxId, RegistryRequestStatus status) throws IllegalArgumentException;

    Mono<RaddRegistryRequestEntity> updateStatusAndError(RaddRegistryRequestEntity raddRegistryRequestEntity, RegistryRequestStatus importStatus, String error) throws IllegalArgumentException;

    Mono<RaddRegistryRequestEntity> updateRegistryRequestStatus(RaddRegistryRequestEntity id, RegistryRequestStatus importStatus);

    Mono<Void> updateRecordsInPending(List<RaddRegistryRequestEntity> addresses);

    Flux<RaddRegistryRequestEntity> findByCxIdAndRequestIdAndStatusNotIn(String cxId, String requestId, List<RegistryRequestStatus> statusList);
}
