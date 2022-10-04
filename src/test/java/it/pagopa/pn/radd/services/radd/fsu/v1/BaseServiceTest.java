package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.ENSURE_FISCAL_CODE_EMPTY;
import static org.junit.jupiter.api.Assertions.*;


@Slf4j
class BaseServiceTest extends BaseTest {

    @InjectMocks
    BaseService baseService;

    @Mock
    PnDataVaultClient pnDataVaultClient;

    @Test
    void testWhenBundleIdIsEmpty(){
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        PnDataVaultClient pnDataVaultClient = new PnDataVaultClient(pnRaddFsuConfig);
        Mono<String> response = baseService.getEnsureFiscalCode("", Const.PF, pnDataVaultClient);
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("recipientTaxId o recipientType non valorizzato correttamente", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    void testWhenFiscalCodeIsNotCorrect(){
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        PnDataVaultClient pnDataVaultClient = new PnDataVaultClient(pnRaddFsuConfig);
        Mono<String> response = baseService.getEnsureFiscalCode("test", "fiscalcodeNotCorrect", pnDataVaultClient);
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("recipientTaxId o recipientType non valorizzato correttamente", exception.getMessage());
            return Mono.empty();
        }).block();
    }


    @Test
    void testWhenResponseIsEmpty(){

        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any())
        ).thenReturn(Mono.just(""));
        Mono<String> response = baseService.getEnsureFiscalCode("test", Const.PF, pnDataVaultClient);
        response.onErrorResume( RaddGenericException.class, exception ->{
            assertEquals(ENSURE_FISCAL_CODE_EMPTY, exception.getExceptionType());
            return Mono.empty();
        }).block();
    }


    @Test
    void testWhenResponseIsFull(){

        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any())
            ).thenReturn( Mono.just("data"));
        Mono<String> response = baseService.getEnsureFiscalCode("test", Const.PF, pnDataVaultClient);

        assertFalse(response.toString().isEmpty());

    }
}
