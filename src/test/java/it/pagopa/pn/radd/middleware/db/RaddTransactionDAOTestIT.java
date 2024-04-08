package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.exception.TransactionAlreadyExistsException;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class RaddTransactionDAOTestIT extends BaseTest.WithLocalStack {


    @Autowired
    private RaddTransactionDAO raddTransactionDAO;

    @Test
    void putTransactionWithConditionsForAct() {
        String operationId = "operationId1ACT" + Math.random();
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setOperationId(operationId);
        entity.setIun("iun1");
        entity.setTransactionId("RADD#cxId#"+operationId);
        entity.setQrCode("qrCode1");
        entity.setFileKey("fileKey1");
        entity.setRecipientId("rec1");
        entity.setOperationType(OperationTypeEnum.ACT.name());
        entity.setStatus(Const.STARTED);

        StepVerifier.create(raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.ACT))
                .expectErrorMatches(ex ->
                        ex instanceof RaddGenericException raddExc && raddExc.getExceptionType() == ExceptionTypeEnum.TRANSACTION_NOT_EXIST
                )
                .verify();

        raddTransactionDAO.putTransactionWithConditions(entity).block();

        RaddTransactionEntity returnEntity = raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.ACT).block();

        assertThat(returnEntity).isEqualTo(entity);

        entity.setIun("iun2");

        StepVerifier.create(raddTransactionDAO.putTransactionWithConditions(entity))
                .expectErrorMatches(ex ->
                        ex instanceof TransactionAlreadyExistsException
                )
                .verify();

        entity.setIun("iun1");

        StepVerifier.create(raddTransactionDAO.putTransactionWithConditions(entity))
                .expectNext(entity)
                .verifyComplete();

    }

    @Test
    void putTransactionWithConditionsForAor() {
        String operationId = "operationIdAOR" + Math.random();
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setOperationId(operationId);
        entity.setFileKey("fileKey1");
        entity.setTransactionId("RADD#cxId#"+operationId);
        entity.setRecipientId("rec1");
        entity.setOperationType(OperationTypeEnum.AOR.name());
        entity.setStatus(Const.STARTED);

        StepVerifier.create(raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.AOR))
                .expectErrorMatches(ex ->
                        ex instanceof RaddGenericException raddExc && raddExc.getExceptionType() == ExceptionTypeEnum.TRANSACTION_NOT_EXIST
                )
                .verify();

        raddTransactionDAO.putTransactionWithConditions(entity).block();

        RaddTransactionEntity returnEntity = raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.AOR).block();

        assertThat(returnEntity).isEqualTo(entity);

        entity.setRecipientId("rec2");

        StepVerifier.create(raddTransactionDAO.putTransactionWithConditions(entity))
                .expectErrorMatches(ex ->
                        ex instanceof TransactionAlreadyExistsException
                )
                .verify();

        entity.setRecipientId("rec1");

        StepVerifier.create(raddTransactionDAO.putTransactionWithConditions(entity))
                .expectNext(entity)
                .verifyComplete();

    }

    @Test
    void putTransactionWithConditionsForAorAndActSameOperationId() {
        String operationId = "operationId" + Math.random();
        RaddTransactionEntity entityAOR = new RaddTransactionEntity();
        entityAOR.setOperationId(operationId);
        entityAOR.setFileKey("fileKey1");
        entityAOR.setTransactionId("RADD#cxId#"+operationId);
        entityAOR.setRecipientId("rec1");
        entityAOR.setOperationType(OperationTypeEnum.AOR.name());
        entityAOR.setStatus(Const.STARTED);


        StepVerifier.create(raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.AOR))
                .expectErrorMatches(ex ->
                        ex instanceof RaddGenericException raddExc && raddExc.getExceptionType() == ExceptionTypeEnum.TRANSACTION_NOT_EXIST
                )
                .verify();

        raddTransactionDAO.putTransactionWithConditions(entityAOR).block();

        RaddTransactionEntity returnEntityAOR = raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.AOR).block();

        assertThat(returnEntityAOR).isEqualTo(entityAOR);

        RaddTransactionEntity entityACT = new RaddTransactionEntity();
        entityACT.setOperationId(operationId);
        entityACT.setFileKey("fileKey1");
        entityACT.setRecipientId("rec1");
        entityACT.setTransactionId("RADD#cxId#"+operationId);
        entityACT.setIun("iun1");
        entityACT.setQrCode("qrCode1");
        entityACT.setOperationType(OperationTypeEnum.ACT.name());
        entityACT.setStatus(Const.STARTED);

        StepVerifier.create(raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.ACT))
                .expectErrorMatches(ex ->
                        ex instanceof RaddGenericException raddExc && raddExc.getExceptionType() == ExceptionTypeEnum.TRANSACTION_NOT_EXIST
                )
                .verify();

        raddTransactionDAO.putTransactionWithConditions(entityACT).block();

        RaddTransactionEntity returnEntityACT = raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.ACT).block();
        assertThat(returnEntityACT).isEqualTo(entityACT);

        returnEntityAOR = raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.AOR).block();
        assertThat(returnEntityAOR).isEqualTo(entityAOR);
    }

    @Test
    void putTransactionWithConditionsForAorWithStatusCompletedInDB() {
        String operationId = "operationIdAOR" + Math.random();
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setOperationId(operationId);
        entity.setFileKey("fileKey1");
        entity.setTransactionId("RADD#cxId#"+operationId);
        entity.setRecipientId("rec1");
        entity.setOperationType(OperationTypeEnum.AOR.name());
        entity.setStatus(Const.COMPLETED);

        StepVerifier.create(raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.AOR))
                .expectErrorMatches(ex ->
                        ex instanceof RaddGenericException raddExc && raddExc.getExceptionType() == ExceptionTypeEnum.TRANSACTION_NOT_EXIST
                )
                .verify();

        raddTransactionDAO.putTransactionWithConditions(entity).block();

        RaddTransactionEntity returnEntity = raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.AOR).block();

        assertThat(returnEntity).isEqualTo(entity);

        entity.setStatus(Const.STARTED);

        StepVerifier.create(raddTransactionDAO.putTransactionWithConditions(entity))
                .expectErrorMatches(ex ->
                        ex instanceof TransactionAlreadyExistsException
                )
                .verify();

    }

    @Test
    void putTransactionWithConditionsForActWithStatusAbortedInDB() {
        String operationId = "operationId1ACT" + Math.random();
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setOperationId(operationId);
        entity.setIun("iun1");
        entity.setTransactionId("RADD#cxId#"+operationId);
        entity.setQrCode("qrCode1");
        entity.setFileKey("fileKey1");
        entity.setRecipientId("rec1");
        entity.setOperationType(OperationTypeEnum.ACT.name());
        entity.setStatus(Const.ABORTED);

        StepVerifier.create(raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.ACT))
                .expectErrorMatches(ex ->
                        ex instanceof RaddGenericException raddExc && raddExc.getExceptionType() == ExceptionTypeEnum.TRANSACTION_NOT_EXIST
                )
                .verify();

        raddTransactionDAO.putTransactionWithConditions(entity).block();

        RaddTransactionEntity returnEntity = raddTransactionDAO.getTransaction("RADD", "cxId", operationId, OperationTypeEnum.ACT).block();

        assertThat(returnEntity).isEqualTo(entity);

        entity.setStatus(Const.STARTED);

        StepVerifier.create(raddTransactionDAO.putTransactionWithConditions(entity))
                .expectErrorMatches(ex ->
                        ex instanceof TransactionAlreadyExistsException
                )
                .verify();

    }
}
