package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.pojo.RaddTransactionStatusEnum;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface RaddTransactionDAO {

    Mono<RaddTransactionEntity> createRaddTransaction(RaddTransactionEntity entity, List<OperationsIunsEntity> iunsEntities);

    Mono<RaddTransactionEntity> getTransaction(String cxType, String cxId, String operationId, OperationTypeEnum operationType);

    Mono<RaddTransactionEntity> getTransaction(String transactionId, OperationTypeEnum operationType);

    Mono<RaddTransactionEntity> updateStatus(RaddTransactionEntity entity, RaddTransactionStatusEnum status);

    Mono<Integer> countFromIunAndStatus(String iun);

    Flux<RaddTransactionEntity> getTransactionsFromIun(String iun);
    Flux<RaddTransactionEntity> getTransactionsFromFiscalCode(String ensureFiscalCode, Date from, Date to);

    Mono<RaddTransactionEntity> putTransactionWithConditions(RaddTransactionEntity entity);

    Mono<RaddTransactionEntity> updateZipAttachments(RaddTransactionEntity entity, Map<String, String> zipAttachments);
}
