package it.pagopa.pn.radd.services.radd.fsu.v1;


import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddTransactionNoExistedException;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationResponseStatus;
import it.pagopa.pn.radd.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@Slf4j
class OperationServiceTest extends BaseTest {

    @InjectMocks
    private OperationService operationService;

    @Mock
    private RaddTransactionDAO dao;

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

    }

}
