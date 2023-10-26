package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

public interface RaddTransactionDAO {

    Mono<RaddTransactionEntity> createRaddTransaction(RaddTransactionEntity entity, List<OperationsIunsEntity> iunsEntities);

    Mono<RaddTransactionEntity> getTransaction(String operationId, OperationTypeEnum operationType);


    Mono<RaddTransactionEntity> updateStatus(RaddTransactionEntity entity);


    Mono<Integer> countFromIunAndOperationIdAndStatus(String operationId, String iun);

    Mono<Integer> countFromQrCodeCompleted(String qrCode);

    Flux<RaddTransactionEntity> getTransactionsFromIun(String iun);
    Flux<RaddTransactionEntity> getTransactionsFromFiscalCode(String ensureFiscalCode, Date from, Date to);
}
