package it.pagopa.pn.radd.middleware.db.impl;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.BaseDao;
import it.pagopa.pn.radd.middleware.db.OperationsIunsDAO;
import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.util.List;


@Repository
@Slf4j
public class OperationsIunsDAOImpl extends BaseDao<OperationsIunsEntity> implements OperationsIunsDAO {

    public OperationsIunsDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                 DynamoDbAsyncClient dynamoDbAsyncClient,
                                 PnRaddFsuConfig raddFsuConfig) {
        super(dynamoDbEnhancedAsyncClient,
                dynamoDbAsyncClient,
                raddFsuConfig.getDao().getIunsOperationsTable(),
                raddFsuConfig,
                OperationsIunsEntity.class
        );
    }


    @Override
    public Mono<Void> putWithBatch(List<OperationsIunsEntity> operations) {
        if (operations == null) return Mono.just("").then();

        Flux<OperationsIunsEntity> fluxIuns = Flux.fromStream(operations.stream());
        return fluxIuns.buffer(24)
                .flatMap(list -> batchWriter(list, 0))
                .then();
    }

    @Override
    public Flux<OperationsIunsEntity> getAllOperationFromIun(String iun) {
        Key key = Key.builder().partitionValue(iun).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        return this.getByFilter(conditional, OperationsIunsEntity.IUN_TRANSACTION_INDEX, null, null, null);
    }

   @Override
    public Flux<OperationsIunsEntity> getAllIunsFromTransactionId(String transactionId) {
        Key key = Key.builder().partitionValue(transactionId).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);
        return this.getByFilter(conditional, null, null, null, null);
    }


}
