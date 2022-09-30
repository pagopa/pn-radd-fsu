package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.mapper.TransactionDataMapper;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.msclient.*;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadResponse;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Slf4j
public class ActServiceTest extends BaseTest {

    @InjectMocks
    ActService actService;

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

}
