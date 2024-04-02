package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.AddressManagerRequestAddress;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RaddRegistryRequestDAO {

    Flux<RaddRegistryRequestEntity> getAllFromCorrelationId(String correlationId, String state);

    Mono<Void> updateRecordsInPendig(List<RaddRegistryRequestEntity> addresses);
}
