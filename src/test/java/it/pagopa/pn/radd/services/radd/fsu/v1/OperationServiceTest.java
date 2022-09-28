package it.pagopa.pn.radd.services.radd.fsu.v1;


import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.RaddTransactionNoExistedException;
import it.pagopa.pn.radd.mapper.RaddTransactionEntityNotificationResponse;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationResponseStatus;
import it.pagopa.pn.radd.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


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

    @Test
    void testWhenNoTransactionForOperationId(){
        OperationResponse r = new OperationResponse();
        OperationResponseStatus status = new OperationResponseStatus();
        status.setMessage("Transazione non trovata");
        status.setCode(OperationResponseStatus.CodeEnum.NUMBER_1);
        r.setStatus(status);
        Mockito.when(dao.getTransaction(Mockito.any())).thenReturn(Mono.error(new RaddTransactionNoExistedException()));
        StepVerifier.create(operationService.getTransaction("erer"))
                .expectNext(r).verifyComplete();
    }

    @Test
    void testWhenDaoThrowOtherException(){
        Mockito.when(dao.getTransaction(Mockito.any())).thenReturn(Mono.error(new NullPointerException()));
        StepVerifier.create(operationService.getTransaction("erer"))
                .expectError(NullPointerException.class).verify();
    }

    @Test
    void testWhenRetrieveTransactionThenReturnResponse(){
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

        Mockito.when(dao.getTransaction(Mockito.any())).thenReturn(Mono.just(entity));

        OperationResponse response = operationService.getTransaction("err").block(d);

        assertNotNull(response);
        assertNotNull(response.getElement());
        assertEquals(entity.getOperationId(), response.getElement().getOperationId());
        assertEquals(entity.getIun(), response.getElement().getIun());
        assertEquals(entity.getFileKey(), response.getElement().getFileKey());
        assertEquals(entity.getQrCode(), response.getElement().getQrCode());
        assertEquals(entity.getRecipientId(), response.getElement().getRecipientTaxId());
        assertEquals(entity.getRecipientType(), response.getElement().getOperationType());
        assertEquals(entity.getDelegateId(), response.getElement().getDelegateTaxId());
        assertEquals(entity.getUid(), response.getElement().getUid());
        assertEquals(entity.getStatus(), response.getElement().getOperationStatus());
        assertEquals(entity.getOperationStartDate(), DateUtils.formatDate(response.getElement().getOperationStartDate()));
        assertEquals(entity.getOperationEndDate(), DateUtils.formatDate(response.getElement().getOperationEndDate()));
        assertEquals(entity.getErrorReason(), response.getElement().getErrorReason());
    }

}
