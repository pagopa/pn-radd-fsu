package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactCategoryDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactDownloadMetadataResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactDownloadMetadataWithContentTypeResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactListElementDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PnDeliveryPushInternalClientTest extends BaseTest.WithMockServer {
    @Autowired
    PnDeliveryPushInternalClient pnDeliveryPushInternalClient;

    @Test
    void testGetNotificationLegalFacts() {
        String recipientInternalId = "854Bgs31a", iun = "LJLH-GNTJ-DVXR-202209-J-1";
        Flux<LegalFactListElementDto> fluxResponse = pnDeliveryPushInternalClient.getNotificationLegalFacts(recipientInternalId, iun);
        fluxResponse.collectList().map(response -> {
            assertNotEquals(0, response.size());
            response.forEach(element -> {
                assertEquals("LJLH-GNTJ-DVXR-202209-J-1", element.getIun());
                assertEquals("abc", element.getLegalFactsId().getKey());
                assertEquals("1234567890", element.getTaxId());
            });
            return Mono.empty();
        }).block();
    }

    @Test
    void testGetNotificationLegalFactsCode400() {
        String recipientInternalId = "", iun = "LJLH-GNTJ-DVXR-202209-J-1";
        Flux<LegalFactListElementDto> response = pnDeliveryPushInternalClient.getNotificationLegalFacts(recipientInternalId, iun);
        response.onErrorResume(exception -> {
            if (exception instanceof PnRaddException){
                assertEquals(400, ((PnRaddException) exception).getWebClientEx().getStatusCode().value());
                return Mono.empty();
            }
            fail("Badly type exception");
            return Mono.empty();
        }).blockFirst();

    }

    @Test
    void testGetLegalFacts() {
        String recipientInternalId = "854Bgs31a", iun = "LJLH-GNTJ-DVXR-202209-J-1", legalFactId = "98765";
        LegalFactCategoryDto categoryDto = LegalFactCategoryDto.PEC_RECEIPT;
        Mono<LegalFactDownloadMetadataWithContentTypeResponseDto> monoResponse = pnDeliveryPushInternalClient.getLegalFact(recipientInternalId, iun, categoryDto, legalFactId);
        monoResponse.map(response -> {
            assertEquals("document", response.getFilename());
            assertEquals(new BigDecimal(54092), response.getContentLength());
            assertEquals(new BigDecimal(0), response.getRetryAfter());
            assertEquals("http://down", response.getUrl());
            return Mono.empty();
        }).block();
    }

    @Test
    void testGetLegalFactsCode400() {
        String recipientInternalId = "", iun = "LJLH-GNTJ-DVXR-202209-J-1", legalFactId = "98765";
        LegalFactCategoryDto categoryDto = LegalFactCategoryDto.PEC_RECEIPT;
        Mono<LegalFactDownloadMetadataWithContentTypeResponseDto> monoResponse = pnDeliveryPushInternalClient.getLegalFact(recipientInternalId, iun, categoryDto, legalFactId);
        monoResponse.onErrorResume(exception -> {
            if (exception instanceof PnRaddException){
                assertEquals(400, ((PnRaddException) exception).getWebClientEx().getStatusCode().value());
                return Mono.empty();
            }
            fail("Badly type exception");
            return Mono.empty();
        }).block();
    }
}
