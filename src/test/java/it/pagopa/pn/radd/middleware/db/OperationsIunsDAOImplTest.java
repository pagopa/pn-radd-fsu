package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class OperationsIunsDAOImplTest extends BaseTest.WithLocalStack {

    @Autowired
    OperationsIunsDAO operationsIunsDAO;
    private final List<OperationsIunsEntity> iunsEntities = new ArrayList<>();

    @BeforeEach
    public void setUp(){
        OperationsIunsEntity entity = new OperationsIunsEntity();
        entity.setOperationId("operationId");
        entity.setIun("iun");
        entity.setId("id");
        iunsEntities.add(entity);
        OperationsIunsEntity entity1 = new OperationsIunsEntity();
        entity1.setOperationId("operationId");
        entity1.setIun("iun");
        entity1.setId("id1");
        iunsEntities.add(entity1);
        OperationsIunsEntity entity2 = new OperationsIunsEntity();
        entity2.setOperationId("operationId");
        entity2.setIun("iun");
        entity2.setId("id2");
        iunsEntities.add(entity2);
        OperationsIunsEntity entity3 = new OperationsIunsEntity();
        entity3.setOperationId("operationId");
        entity3.setIun("iun");
        entity3.setId("id3");
        iunsEntities.add(entity3);
        OperationsIunsEntity entity4 = new OperationsIunsEntity();
        entity4.setOperationId("operationId");
        entity4.setIun("iun");
        entity4.setId("id4");
        iunsEntities.add(entity4);
        OperationsIunsEntity entity5 = new OperationsIunsEntity();
        entity5.setOperationId("operationId");
        entity5.setIun("iun");
        entity5.setId("id5");
        iunsEntities.add(entity5);
        OperationsIunsEntity entity6 = new OperationsIunsEntity();
        entity6.setOperationId("operationId");
        entity6.setIun("iun");
        entity6.setId("id6");
        iunsEntities.add(entity6);
        OperationsIunsEntity entity7 = new OperationsIunsEntity();
        entity7.setOperationId("operationId");
        entity7.setIun("iun");
        entity7.setId("id7");
        iunsEntities.add(entity7);
        OperationsIunsEntity entity8 = new OperationsIunsEntity();
        entity8.setOperationId("operationId");
        entity8.setIun("iun");
        entity8.setId("id8");
        iunsEntities.add(entity8);
        OperationsIunsEntity entity9 = new OperationsIunsEntity();
        entity9.setOperationId("operationId");
        entity9.setIun("iun");
        entity9.setId("id9");
        iunsEntities.add(entity9);
        OperationsIunsEntity entity10 = new OperationsIunsEntity();
        entity10.setOperationId("operationId");
        entity10.setIun("iun");
        entity10.setId("id10");
        iunsEntities.add(entity10);
        OperationsIunsEntity entity11 = new OperationsIunsEntity();
        entity11.setOperationId("operationId");
        entity11.setIun("iun");
        entity11.setId("id11");
        iunsEntities.add(entity11);
        OperationsIunsEntity entity12 = new OperationsIunsEntity();
        entity12.setOperationId("operationId");
        entity12.setIun("iun");
        entity12.setId("id12");
        iunsEntities.add(entity12);
        OperationsIunsEntity entity13 = new OperationsIunsEntity();
        entity13.setOperationId("operationId");
        entity13.setIun("iun");
        entity13.setId("id13");
        iunsEntities.add(entity13);
        OperationsIunsEntity entity14 = new OperationsIunsEntity();
        entity14.setOperationId("operationId");
        entity14.setIun("iun");
        entity14.setId("id14");
        iunsEntities.add(entity14);
        OperationsIunsEntity entity15 = new OperationsIunsEntity();
        entity15.setOperationId("operationId");
        entity15.setIun("iun");
        entity15.setId("id15");
        iunsEntities.add(entity15);
        OperationsIunsEntity entity16 = new OperationsIunsEntity();
        entity16.setOperationId("operationId");
        entity16.setIun("iun");
        entity16.setId("id16");
        iunsEntities.add(entity16);
        OperationsIunsEntity entity17 = new OperationsIunsEntity();
        entity17.setOperationId("operationId");
        entity17.setIun("iun");
        entity17.setId("id17");
        iunsEntities.add(entity17);
        OperationsIunsEntity entity18 = new OperationsIunsEntity();
        entity18.setOperationId("operationId");
        entity18.setIun("iun");
        entity18.setId("id18");
        iunsEntities.add(entity18);
        OperationsIunsEntity entity19 = new OperationsIunsEntity();
        entity19.setOperationId("operationId");
        entity19.setIun("iun");
        entity19.setId("id19");
        iunsEntities.add(entity19);
        OperationsIunsEntity entity20 = new OperationsIunsEntity();
        entity20.setOperationId("operationId");
        entity20.setIun("iun");
        entity20.setId("id20");
        iunsEntities.add(entity20);
        OperationsIunsEntity entity21 = new OperationsIunsEntity();
        entity21.setOperationId("operationId");
        entity21.setIun("iun");
        entity21.setId("id21");
        iunsEntities.add(entity21);
        OperationsIunsEntity entity22 = new OperationsIunsEntity();
        entity22.setOperationId("operationId");
        entity22.setIun("iun");
        entity22.setId("id22");
        iunsEntities.add(entity22);
        OperationsIunsEntity entity23 = new OperationsIunsEntity();
        entity23.setOperationId("operationId");
        entity23.setIun("iun");
        entity23.setId("id23");
        iunsEntities.add(entity23);
        OperationsIunsEntity entity24 = new OperationsIunsEntity();
        entity24.setOperationId("operationId");
        entity24.setIun("iun");
        entity24.setId("id24");
        iunsEntities.add(entity24);
        OperationsIunsEntity entity25 = new OperationsIunsEntity();
        entity25.setOperationId("operationId");
        entity25.setIun("iun");
        entity25.setId("id25");
        iunsEntities.add(entity25);
        OperationsIunsEntity entity26 = new OperationsIunsEntity();
        entity26.setOperationId("operationId");
        entity26.setIun("iun");
        entity26.setId("id26");
        iunsEntities.add(entity26);
        OperationsIunsEntity entity27 = new OperationsIunsEntity();
        entity27.setOperationId("operationId");
        entity27.setIun("iun");
        entity27.setId("id27");
        iunsEntities.add(entity27);


    }

    @Test
    void putWithBatchTest() {

        StepVerifier.create(operationsIunsDAO.putWithBatch(iunsEntities))
                .expectComplete()
                .verify();
    }

    @Test
    void getAllOperationFromIunTest(){

        List<OperationsIunsEntity> operationsIunsEntities = operationsIunsDAO.getAllOperationFromIun("iun")
                .map(operationsIunsEntity -> operationsIunsEntity)
                .collectList().block();
        assertNotNull(operationsIunsEntities);
        assertEquals(28, operationsIunsEntities.size());
    }

    @Test
    void getAllOperationFromIunExceptionTest(){
        StepVerifier.create(operationsIunsDAO.getAllOperationFromIun(null))
                .expectError()
                .verify();
    }
}
