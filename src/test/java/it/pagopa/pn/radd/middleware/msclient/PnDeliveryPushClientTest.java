package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.PnDocumentException;
import it.pagopa.pn.radd.exception.PnNotificationException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndatavault.v1.dto.RecipientTypeDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.SentNotificationDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.utils.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PnDeliveryPushClientTest extends BaseTest {

    @Autowired
    PnDeliveryPushClient pnDeliveryPushClient;

//    @Test
    void testNotifyNotificationViewed() {
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setIun("LJLH-GNTJ-DVXR-202209-J-1");
        entity.setRecipientType(RecipientTypeDto.PF.getValue());
        entity.setOperationStartDate("2022-09-30T13:57:00");
        entity.setRecipientId("1924814");
        Mono<ResponseNotificationViewedDtoDto> monoResponse = pnDeliveryPushClient.notifyNotificationViewed(entity);
        monoResponse.doOnNext(response -> {
            assertEquals(entity.getIun(), response.getIun());
        }).block();
    }

//    @Test
    void testNotifyNotificationViewedCode400() {
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setIun("LJLH-GNTJ-DVXR-202209-J-1");
        entity.setRecipientType(RecipientTypeDto.PF.getValue());
        entity.setOperationStartDate(DateUtils.formatDate(new Date()));
        entity.setRecipientId("1924814");
        Mono<ResponseNotificationViewedDtoDto> response = pnDeliveryPushClient.notifyNotificationViewed(entity);
        response.onErrorResume(PnNotificationException.class, exception -> {
            assertEquals(400, exception.getWebClientEx().getStatusCode().value());
            return Mono.empty();
        }).block();
    }
}
