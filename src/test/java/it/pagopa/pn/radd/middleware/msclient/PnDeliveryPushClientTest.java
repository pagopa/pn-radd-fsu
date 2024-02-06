package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndatavault.v1.dto.RecipientTypeDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PnDeliveryPushClientTest extends BaseTest.WithMockServer {
    @Autowired
    PnDeliveryPushClient pnDeliveryPushClient;

    @Test
    void testNotifyNotificationViewed() {
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setIun("LJLH-GNTJ-DVXR-202209-J-1");
        entity.setRecipientType(RecipientTypeDto.PF.getValue());
        entity.setOperationStartDate("2022-09-30T13:57:00.000");
        entity.setRecipientId("1924814");
        Mono<ResponseNotificationViewedDtoDto> monoResponse = pnDeliveryPushClient.notifyNotificationRaddRetrieved(entity, new Date());
        monoResponse.map(response -> {
            assertNotNull(entity);
            assertEquals(entity.getIun(), response.getIun());
            return Mono.empty();
        }).block();
    }

    @Test
    void testNotifyNotificationViewedCode400() {
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setIun("LJLH-GNTJ-DVXR-202209-J-1");
        entity.setRecipientType(RecipientTypeDto.PF.getValue());
        entity.setOperationStartDate("2022-09-30T13:57:00.000");
        entity.setRecipientId("");
        Mono<ResponseNotificationViewedDtoDto> response = pnDeliveryPushClient.notifyNotificationRaddRetrieved(entity, new Date());

        response.onErrorResume(exception -> {
            if (exception instanceof PnRaddException){
                assertEquals(400, ((PnRaddException) exception).getWebClientEx().getStatusCode().value());
                return Mono.empty();
            }
            fail("Badly type exception");
            return null;
        }).block();
    }

    @Test
    void testPaperNotificationFailed() {
        String recipientInternalId  = "854Bgs31a";
        Mono<List<ResponsePaperNotificationFailedDtoDto>> monoResponse = pnDeliveryPushClient.getPaperNotificationFailed(recipientInternalId).collectList();
        monoResponse.map(response -> {
            assertNotEquals(0, response.size());
            response.forEach(element -> {
                assertEquals("LJLH-GNTJ-DVXR-202209-J-1", element.getIun());
                assertEquals("854Bgs31a", element.getRecipientInternalId());
                assertEquals("http://aarUrl", element.getAarUrl());
            });
            return Mono.empty();
        }).block();
    }
    @Test
    void testPaperNotificationFailedCode400() {
        String recipientInternalId  = "854";
        Flux<ResponsePaperNotificationFailedDtoDto> response = pnDeliveryPushClient.getPaperNotificationFailed(recipientInternalId);
        response.onErrorResume(exception -> {
            if (exception instanceof PnRaddException){
                assertEquals(400, ((PnRaddException) exception).getWebClientEx().getStatusCode().value());
                return Mono.empty();
            }
            fail("Badly type exception");
            return Mono.empty();
        }).blockFirst();
    }
}
