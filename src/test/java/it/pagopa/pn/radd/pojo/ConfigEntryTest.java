package it.pagopa.pn.radd.pojo;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigEntryTest {

    @Test
    void testConstructorAndGetters() {
        Instant startValidity = Instant.parse("2022-01-01T00:00:00Z");
        Instant endValidity = Instant.parse("2022-01-02T00:00:00Z");

        ConfigEntry configEntry = new ConfigEntry();
        configEntry.setStartValidity(startValidity);
        configEntry.setEndValidity(endValidity);

        assertEquals(startValidity, configEntry.getStartValidity());
        assertEquals(endValidity, configEntry.getEndValidity());
    }
}
