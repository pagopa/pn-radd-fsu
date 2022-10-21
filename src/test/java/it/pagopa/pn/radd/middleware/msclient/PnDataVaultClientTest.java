package it.pagopa.pn.radd.middleware.msclient;


import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndatavault.v1.dto.RecipientTypeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PnDataVaultClientTest extends BaseTest.WithMockServer {
    @Autowired
    private PnDataVaultClient pnDataVaultClient;

    @Test
    void testGetEnsureFiscalCode() {
        String fiscalCode = "" , type = RecipientTypeDto.PF.getValue();
        String responseFiscal = pnDataVaultClient.getEnsureFiscalCode(fiscalCode, type).block();
        assertEquals("\"PF-4fc75df3-0913-407e-bdaa-e50329708b7d\"", responseFiscal);
    }

    @Test
    void testGetEnsureFiscalCodeError400() {
        String fiscalCode = "" , type = RecipientTypeDto.PG.getValue();
        Mono<String> response = pnDataVaultClient.getEnsureFiscalCode(fiscalCode, type);
        response.onErrorResume(PnRaddException.class, exception -> {
            assertEquals(400, exception.getWebClientEx().getStatusCode().value());
            return Mono.empty();
        }).block();
    }
}
