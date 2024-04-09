package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.ImportStatus;
import it.pagopa.pn.radd.pojo.PnLastEvaluatedKey;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import it.pagopa.pn.radd.pojo.ResultPaginationDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RaddRegistryRequestDAO {

    Flux<RaddRegistryRequestEntity> findByCorrelationIdWithStatus(String cxId, ImportStatus status) throws IllegalArgumentException;

    Mono<RaddRegistryRequestEntity> updateStatusAndError(RaddRegistryRequestEntity raddRegistryRequestEntity, ImportStatus importStatus, String error) throws IllegalArgumentException;

    Mono<RaddRegistryRequestEntity> updateRegistryRequestStatus(RaddRegistryRequestEntity id, RegistryRequestStatus importStatus);

    Flux<RaddRegistryRequestEntity> getAllFromCorrelationId(String correlationId, String state);

    Mono<Void> updateRecordsInPending(List<RaddRegistryRequestEntity> addresses);

    Flux<RaddRegistryRequestEntity> findByCxIdAndRequestIdAndStatusNotIn(String cxId, String requestId, List<RegistryRequestStatus> statusList);

    Mono<ResultPaginationDto<RaddRegistryEntity, PnLastEvaluatedKey>> findAll(String xPagopaPnCxId, Integer limit, String cap, String city, String pr, String externalCode, PnLastEvaluatedKey lastEvaluatedKey);

    Mono<ResultPaginationDto<RaddRegistryRequestEntity, PnLastEvaluatedKey>> getRegistryByCxIdAndRequestId(String xPagopaPnCxId, String requestId, Integer limit, PnLastEvaluatedKey lastEvaluatedKey);


}
