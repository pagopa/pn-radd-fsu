package it.pagopa.pn.radd.services.radd.fsu.v1;


import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.mapper.RaddTransactionEntityNotificationResponse;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationActResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationAorResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationResponseStatus;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationsResponse;
import it.pagopa.pn.radd.utils.DateUtils;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
class OperationServiceTest extends BaseTest {
    private final Duration d = Duration.ofMillis(3000);
    @InjectMocks
    private OperationService operationService;
    @Mock
    private RaddTransactionDAO dao;
    @Autowired
    @Spy
    private RaddTransactionEntityNotificationResponse mapperToNotificationResponse;

    // ACT TRANSACTION FOR OPERATION ID //
    @Test
    void testWhenNoActTransactionForOperationId(){
        OperationActResponse r = new OperationActResponse();
        r.setResult(false);
        OperationResponseStatus status = new OperationResponseStatus();
        status.setMessage(ExceptionTypeEnum.TRANSACTION_NOT_EXIST.getMessage());
        status.setCode(OperationResponseStatus.CodeEnum.NUMBER_1);
        r.setStatus(status);
        Mockito.when(dao.getTransaction("TestNoActTransaction", OperationTypeEnum.ACT)).thenReturn(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST)));
        StepVerifier.create(operationService.getTransactionActByOperationIdAndType("TestNoActTransaction"))
                .expectNext(r).verifyComplete();
    }

    @Test
    void testWhenNoActTransactionDaoThrowOtherException(){
        Mockito.when(dao.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.error(new NullPointerException()));
        StepVerifier.create(operationService.getTransactionActByOperationIdAndType("TestThrow"))
                .expectError(NullPointerException.class).verify();
    }

    @Test
    void testWhenRetrieveActTransactionWithOperationIdThenReturnResponse(){
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setOperationId("testOperation");
        entity.setIun("iunTest");
        entity.setFileKey("FileKey test");
        entity.setQrCode("qrcodeTest");
        entity.setRecipientId("FiscalCodeTest");
        entity.setRecipientType("PF");
        entity.setDelegateId("delegateId");
        entity.setUid("uidTest");
        entity.setStatus("COMPLETED");
        entity.setOperationStartDate(DateUtils.formatDate(new Date()));
        entity.setOperationEndDate(DateUtils.formatDate(new Date()));
        entity.setVersionToken("VersionTokenOK");
        entity.setErrorReason("errorReadon");

        Mockito.when(dao.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entity));

        OperationActResponse response = operationService.getTransactionActByOperationIdAndType("err").block(d);

        assertNotNull(response);
        assertNotNull(response.getElement());
        assertEquals(entity.getOperationId(), response.getElement().getOperationId());
        assertEquals(entity.getIun(), response.getElement().getIun());
        assertEquals(entity.getFileKey(), response.getElement().getFileKey());
        assertEquals(entity.getQrCode(), response.getElement().getQrCode());
        assertEquals(entity.getRecipientId(), response.getElement().getRecipientTaxId());
        assertEquals(entity.getRecipientType(), response.getElement().getRecipientType());
        assertEquals(entity.getDelegateId(), response.getElement().getDelegateTaxId());
        assertEquals(entity.getUid(), response.getElement().getUid());
        assertEquals(entity.getStatus(), response.getElement().getOperationStatus());
        assertEquals(entity.getOperationStartDate(), DateUtils.formatDate(response.getElement().getOperationStartDate()));
        assertEquals(entity.getOperationEndDate(), DateUtils.formatDate(response.getElement().getOperationEndDate()));
        assertEquals(entity.getErrorReason(), response.getElement().getErrorReason());
    }

    // ------------------------------ //


    // ACT TRANSACTION FOR IUN //
    @Test
    void testGetActTransactionWithIunWhenDaoListIsEmpty(){
        Mockito.when(dao.getTransactionsFromIun(Mockito.any())).thenReturn(Flux.empty());

        OperationsResponse response = operationService.getTransactionActByIun("IunTestEmpty").block(d);

        assertNotNull(response);
        assertEquals(OperationResponseStatus.CodeEnum.NUMBER_1, response.getStatus().getCode());
        assertFalse(response.getResult());
    }

    @Test
    void testGetActTransactionWithIunReturnListOfOperationId(){
        RaddTransactionEntity entity1 = new RaddTransactionEntity();
        entity1.setIun("Iun 1");
        entity1.setOperationId("Operation id 1");
        entity1.setOperationType(OperationTypeEnum.ACT.name());
        RaddTransactionEntity entity2 = new RaddTransactionEntity();
        entity2.setIun("Iun 1");
        entity2.setOperationId("Operation id 2");
        entity2.setOperationType(OperationTypeEnum.ACT.name());
        Mockito.when(dao.getTransactionsFromIun(Mockito.any())).thenReturn(Flux.just(entity1, entity2));

        OperationsResponse response = operationService.getTransactionActByIun("Iun 1").block(d);

        assertNotNull(response);
        assertEquals(OperationResponseStatus.CodeEnum.NUMBER_0, response.getStatus().getCode());
        assertTrue(response.getResult());
        assertFalse(response.getOperationIds().isEmpty());
        assertEquals(2, response.getOperationIds().size());
        for (String item : response.getOperationIds()) {
            if (!item.equals(entity1.getOperationId()) && !item.equals(entity2.getOperationId())){
                fail("Operation id not mapped");
            }
        }
    }

    // ------------------------------ //


    // AOR TRANSACTION FOR OPERATION ID //
    @Test
    void testWhenNoAorTransactionForOperationId(){
        OperationAorResponse r = new OperationAorResponse();
        r.setResult(false);
        OperationResponseStatus status = new OperationResponseStatus();
        status.setMessage(ExceptionTypeEnum.TRANSACTION_NOT_EXIST.getMessage());
        status.setCode(OperationResponseStatus.CodeEnum.NUMBER_1);
        r.setStatus(status);
        Mockito.when(dao.getTransaction("TestNoAorTransaction", OperationTypeEnum.AOR)).thenReturn(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST)));
        StepVerifier.create(operationService.getTransactionAorByOperationIdAndType("TestNoAorTransaction"))
                .expectNext(r).verifyComplete();
    }

    @Test
    void testWhenNoAorTransactionDaoThrowOtherException(){
        Mockito.when(dao.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.error(new NullPointerException()));
        StepVerifier.create(operationService.getTransactionActByOperationIdAndType("TestThrow"))
                .expectError(NullPointerException.class).verify();
    }

    @Test
    void testWhenRetrieveAorTransactionWithOperationIdThenReturnResponse(){
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setOperationId("testOperation");
        entity.setIun("[iunTest, testIun]");
        entity.setFileKey("FileKey test");
        entity.setQrCode("qrcodeTest");
        entity.setRecipientId("FiscalCodeTest");
        entity.setRecipientType("PF");
        entity.setDelegateId("delegateId");
        entity.setUid("uidTest");
        entity.setStatus("COMPLETED");
        entity.setOperationType(OperationTypeEnum.AOR.name());
        entity.setOperationStartDate(DateUtils.formatDate(new Date()));
        entity.setOperationEndDate(DateUtils.formatDate(new Date()));
        entity.setVersionToken("VersionTokenOK");
        entity.setErrorReason("errorReadon");

        Mockito.when(dao.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entity));

        OperationAorResponse response = operationService.getTransactionAorByOperationIdAndType("err").block(d);

        assertNotNull(response);
        assertNotNull(response.getElement());
        assertEquals(entity.getOperationId(), response.getElement().getOperationId());
        assertNotNull(response.getElement().getIuns());
        assertFalse(response.getElement().getIuns().isEmpty());
        assertEquals(entity.getFileKey(), response.getElement().getFileKey());
        assertEquals(entity.getQrCode(), response.getElement().getQrCode());
        assertEquals(entity.getRecipientId(), response.getElement().getRecipientTaxId());
        assertEquals(entity.getRecipientType(), response.getElement().getRecipientType());
        assertEquals(entity.getDelegateId(), response.getElement().getDelegateTaxId());
        assertEquals(entity.getUid(), response.getElement().getUid());
        assertEquals(entity.getOperationType(), response.getElement().getOperationType());
        assertEquals(entity.getStatus(), response.getElement().getOperationStatus());
        assertEquals(entity.getOperationStartDate(), DateUtils.formatDate(response.getElement().getOperationStartDate()));
        assertEquals(entity.getOperationEndDate(), DateUtils.formatDate(response.getElement().getOperationEndDate()));
        assertEquals(entity.getErrorReason(), response.getElement().getErrorReason());
    }

    // ------------------------------ //

}
