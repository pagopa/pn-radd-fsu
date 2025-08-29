package it.pagopa.pn.radd.mapper;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class AbstractRegistryMapperTest {

    static class TestRegistryMapper extends AbstractRegistryMapper {}

    private final AbstractRegistryMapper mapper = new TestRegistryMapper();

    @Test
    void testToInstant_withValidDate() {
        Date date = Date.from(Instant.parse("2025-08-01T10:15:30Z"));
        Instant result = mapper.toInstant(date);
        assertNotNull(result);
        assertEquals("2025-08-01T10:15:30Z", result.toString());
    }

    @Test
    void testToInstant_withNull_shouldReturnNull() {
        assertNull(mapper.toInstant(null));
    }

    @Test
    void testToDate_withValidInstant() {
        Instant instant = Instant.parse("2025-08-01T10:15:30Z");
        Date date = mapper.toDate(instant);
        assertNotNull(date);
        assertEquals(instant, date.toInstant());
    }

    @Test
    void testToDate_withNull_shouldReturnNull() {
        assertNull(mapper.toDate(null));
    }

    @Test
    void testToStringDate_withValidInstant() {
        Instant instant = LocalDate.of(2025, 8, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
        String result = mapper.toStringDate(instant);
        assertEquals("2025-08-01", result);
    }

    @Test
    void testToStringDate_withNull_shouldReturnNull() {
        assertNull(mapper.toStringDate(null));
    }

    @Test
    void testParseDateString_withValidString() {
        String dateStr = "2025-08-01";
        Instant expected = LocalDate.of(2025, 8, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant result = mapper.parseDateString(dateStr);
        assertEquals(expected, result);
    }

    @Test
    void testParseDateString_withInvalidString_shouldReturnNull() {
        String dateStr = "not-a-date";
        assertNull(mapper.parseDateString(dateStr));
    }

    @Test
    void testParseDateString_withNull_shouldReturnNull() {
        assertNull(mapper.parseDateString(null));
    }
}