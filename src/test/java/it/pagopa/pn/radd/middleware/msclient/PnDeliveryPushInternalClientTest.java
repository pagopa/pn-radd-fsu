package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactCategoryDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactDownloadMetadataResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PnDeliveryPushInternalClientTest {
    @Autowired
    PnDeliveryPushInternalClient pnDeliveryPushInternalClient;

//    @Test
    void testGetNotificationLegalFacts() {
        String recipientInternalId = "12345", iun = "LJLH-GNTJ-DVXR-202209-J-1", legalFactId = "98765";
        LegalFactCategoryDto categoryDto = LegalFactCategoryDto.ANALOG_DELIVERY;
        Mono<LegalFactDownloadMetadataResponseDto> monoResponse = pnDeliveryPushInternalClient.getLegalFact(recipientInternalId, iun, categoryDto, legalFactId);
        monoResponse.doOnNext(response -> {
            assertNotNull(response);
        }).block();
    }

//    @Test
    void testGetNotificationLegalFactsCode400() {
        String recipientInternalId = "12345", iun = "LJLH-GNTJ-DVXR-202209-J-1", legalFactId = "98765";
        LegalFactCategoryDto categoryDto = LegalFactCategoryDto.ANALOG_DELIVERY;
        Mono<LegalFactDownloadMetadataResponseDto> response = pnDeliveryPushInternalClient.getLegalFact(recipientInternalId, iun, categoryDto, legalFactId);
        response.onErrorResume(WebClientResponseException.class, exception -> {
            assertEquals(HttpStatus.valueOf(400), exception.getStatusCode());
            exception.getMessage();
            return Mono.empty();
        }).block();
    }

//    @Test
    void testGetLegalFacts() {

    }

//    @Test
    void testGetLegalFactsCode400() {

    }
}
