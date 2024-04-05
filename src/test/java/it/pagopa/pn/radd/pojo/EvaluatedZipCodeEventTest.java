package it.pagopa.pn.radd.pojo;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaluatedZipCodeEventTest {

    @Test
    void testConstructorAndGetters() {
        EvaluatedZipCodeEvent.Detail detail = EvaluatedZipCodeEvent.Detail.builder()
                .configKey("testKey")
                .configType("ZIPCODE")
                .configs(Collections.singletonList(new ConfigEntry()))
                .build();

        EvaluatedZipCodeEvent event = EvaluatedZipCodeEvent.builder()
                .detail(detail)
                .build();

        assertEquals(detail, event.getDetail());
    }
}
