package it.pagopa.pn.radd.pojo;

import it.pagopa.pn.api.dto.events.PnAttachmentsConfigEventItem;
import it.pagopa.pn.api.dto.events.PnAttachmentsConfigEventPayload;
import it.pagopa.pn.api.dto.events.PnEvaluatedZipCodeEvent;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaluatedZipCodeEventTest {

    @Test
    void testConstructorAndGetters() {
        PnAttachmentsConfigEventPayload detail = PnAttachmentsConfigEventPayload.builder()
                .configKey("testKey")
                .configType("ZIPCODE")
                .configs(Collections.singletonList(PnAttachmentsConfigEventItem.builder().build()))
                .build();

        PnEvaluatedZipCodeEvent event = PnEvaluatedZipCodeEvent.builder()
                .detail(detail)
                .build();

        assertEquals(detail, event.getDetail());
    }
}
