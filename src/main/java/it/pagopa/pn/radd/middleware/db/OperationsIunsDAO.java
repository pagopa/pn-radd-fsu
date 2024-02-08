package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OperationsIunsDAO {

    Mono<Void> putWithBatch(List<OperationsIunsEntity> operations);

    Flux<OperationsIunsEntity> getAllOperationFromIun(String iun);

    Flux<OperationsIunsEntity> getAllIunsFromTransactionId(String transactionId);


}
