package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.RaddFiscalCodeEnsureException;
import it.pagopa.pn.radd.middleware.msclient.*;
import it.pagopa.pn.radd.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Slf4j
public class ActServiceTest extends BaseTest {

    @InjectMocks
    ActService actService;

    @Mock
    PnDataVaultClient pnDataVaultClient;




    @Test
    void testWhenBundleIdIsEmpty(){
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        PnDataVaultClient pnDataVaultClient = new PnDataVaultClient(pnRaddFsuConfig);
        Mono<String> response = actService.getEnsureFiscalCode("", Const.PF, pnDataVaultClient);
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Parametri non validi", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    void testWhenFiscalCodeIsNotCorrect(){
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        PnDataVaultClient pnDataVaultClient = new PnDataVaultClient(pnRaddFsuConfig);
        Mono<String> response = actService.getEnsureFiscalCode("test", "fiscalcodeNotCorrect", pnDataVaultClient);
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Parametri non validi", exception.getMessage());
            return Mono.empty();
        }).block();
    }


    @Test
    void testWhenResponseIsEmpty(){

        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any())
        ).thenReturn(Mono.just(""));
        Mono<String> response = actService.getEnsureFiscalCode("test", Const.PF, pnDataVaultClient);
        response.onErrorResume( RaddFiscalCodeEnsureException.class, exception ->{
            assertEquals(409, exception.getStatusCode());
            return Mono.empty();
        }).block();
    }


    @Test
    void testWhenResponseIsFull(){

        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any())
        ).thenReturn( Mono.just("data"));
        Mono<String> response = actService.getEnsureFiscalCode("test", Const.PF, pnDataVaultClient);

        assertTrue(!response.toString().isEmpty());

    }

}
