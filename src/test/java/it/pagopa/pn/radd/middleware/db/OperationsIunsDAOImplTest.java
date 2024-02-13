package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: Test disabilitati da riparare in fase di aggiornamento rispettiva API


class OperationsIunsDAOImplTest extends BaseTest.WithLocalStack {
    private static final String IUN_TEST = "IUN-TEST";
    private static final String TRANSACTION_ID_TEST = "cxId#PF#operationId";
    @Autowired
    private OperationsIunsDAO operationsIunsDAO;
    @Spy
    private DynamoDbEnhancedAsyncClient dbEnhancedAsyncClient;


    @BeforeEach
    void setUp(){
        List<OperationsIunsEntity> entities = this.createOperations(IUN_TEST, "cxId#PF#operationId", 99);
        operationsIunsDAO.putWithBatch(entities).block();
    }

    @Test
    void whenOperationsIsEmptyThenNotSave(){
        operationsIunsDAO.putWithBatch(new ArrayList<>()).block();

        Mockito.verify(dbEnhancedAsyncClient, Mockito.timeout(1000).times(0))
                .batchWriteItem((BatchWriteItemEnhancedRequest) Mockito.any());
    }

    @Test
    void whenSaveOver25ItemsThenRetrieveOver25Items(){
        List<OperationsIunsEntity> operationsIunsEntities = operationsIunsDAO.getAllOperationFromIun(IUN_TEST)
                .map(operationsIunsEntity -> operationsIunsEntity)
                .collectList().block();
        assertNotNull(operationsIunsEntities);

    }
    @Test
    void getAllOperationFromTransactionId(){
        List<OperationsIunsEntity> operationsIunsEntities = operationsIunsDAO.getAllIunsFromTransactionId(TRANSACTION_ID_TEST)
                .map(operationsIunsEntity -> operationsIunsEntity)
                .collectList().block();
        assertNotNull(operationsIunsEntities);
    }

    @Test
    void getAllOperationFromTransactionIdException(){
        StepVerifier.create(operationsIunsDAO.getAllIunsFromTransactionId(null))
                .expectError()
                .verify();
    }

    @Test
    void getAllOperationFromIunExceptionTest(){
        StepVerifier.create(operationsIunsDAO.getAllOperationFromIun(null))
                .expectError()
                .verify();
    }

    private List<OperationsIunsEntity> createOperations(String iun, String transactionId, int size) {
        List<OperationsIunsEntity> list = new ArrayList<>();
        for(int i=0; i<size; i++){
            OperationsIunsEntity op = new OperationsIunsEntity();
            op.setTransactionId(transactionId);
            op.setIun(iun+i);
            list.add(op);
        }
        return list;
    }

}
