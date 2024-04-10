package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.PnLastEvaluatedKey;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import it.pagopa.pn.radd.pojo.ResultPaginationDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RaddRegistryRequestDAO {

    Mono<Void> writeCsvAddresses(List<RaddRegistryRequestEntity> raddRegistryRequestEntities, String correlationId);

    Flux<RaddRegistryRequestEntity> getAllFromCorrelationId(String correlationId, String state);

    Flux<RaddRegistryRequestEntity> getAllFromCxidAndRequestIdWithState(String cxId, String requestId, String state);

    Flux<RaddRegistryRequestEntity> findByCorrelationIdWithStatus(String cxId, RegistryRequestStatus status) throws IllegalArgumentException;

    Mono<RaddRegistryRequestEntity> updateStatusAndError(RaddRegistryRequestEntity raddRegistryRequestEntity, RegistryRequestStatus importStatus, String error) throws IllegalArgumentException;

    Mono<RaddRegistryRequestEntity> updateRegistryRequestStatus(RaddRegistryRequestEntity id, RegistryRequestStatus importStatus);

    Mono<Void> updateRecordsInPending(List<RaddRegistryRequestEntity> addresses);

    Flux<RaddRegistryRequestEntity> findByCxIdAndRegistryId(String cxId, String registryId);

    Mono<RaddRegistryRequestEntity> putRaddRegistryRequestEntity(RaddRegistryRequestEntity raddRegistryRequestEntity);

    Mono<ResultPaginationDto<RaddRegistryRequestEntity, PnLastEvaluatedKey>> getRegistryByCxIdAndRequestId(String xPagopaPnCxId, String requestId, Integer limit, String lastEvaluatedKey);


    Flux<RaddRegistryRequestEntity> findByCxIdAndRequestIdAndStatusNotIn(String cxId, String requestId, List<RegistryRequestStatus> statusList);

    Mono<RaddRegistryRequestEntity> createEntity(RaddRegistryRequestEntity entity);

    Mono<RaddRegistryRequestEntity> updateRegistryRequestData(RaddRegistryRequestEntity raddRegistryRequestEntity);
}
