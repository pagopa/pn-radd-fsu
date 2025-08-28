package it.pagopa.pn.radd.utils;

import lombok.CustomLog;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@CustomLog
class OpeningHoursParserTest {

    @Test
    void testValidSingleDaySingleRange() {
        String input = "Lun 09:00-12:00";
        assertDoesNotThrow(() -> OpeningHoursParser.validateOpenHours(input));
    }

    @Test
    void testValidMultipleDaysAndRanges() {
        String input = """
                Lun-Mer 09:00-12:00, 14:00-18:00
                Gio 08:30-13:00
                Ven 10:00-13:00
                """;
        assertDoesNotThrow(() -> OpeningHoursParser.validateOpenHours(input));
    }

    @Test
    void testValidAllWeek() {
        String input = "Lun-Dom 08:00-20:00";
        assertDoesNotThrow(() -> OpeningHoursParser.validateOpenHours(input));
    }

    @Test
    void testValidDaysWithSemicolonSeparator() {
        String input = "Lun 08:00-12:00; Mar 08:00-12:00";
        assertDoesNotThrow(() -> OpeningHoursParser.validateOpenHours(input));
    }

    @Test
    void parseOpeningHours() {
        String input = "Lun 09:00-12:00, 14:00-18:00; Mar 14:00-18:00";
        var result = assertDoesNotThrow(() -> OpeningHoursParser.parseOpeningHours(input));
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void serializeOpeningHours() {
        Map<String, String> input = Map.of(
                "Lun", "09:00-12:00, 14:00-18:00",
                "Mar", "14:00-18:00"
                                          );
        var result = assertDoesNotThrow(() -> OpeningHoursParser.serializeOpeningHours(input));
        assertNotNull(result);
        log.info(result);
        assertTrue(result.equalsIgnoreCase("Lun 09:00-12:00, 14:00-18:00; Mar 14:00-18:00"));
    }

    @Test
    void testInvalidFormat_noMatch() {
        String input1 = "lunedì 09-12";
        String input2 = "Ven-Mar 09:00-12:00";
        String input3 = "Lun 9:00-12:00";
        String input4 = "Lun 14:00-09:00";
        String input5 = """
                Lun 09:00-12:00
                Lun 13:00-15:00
                """;
        String input6 = "Lun 25:00-26:00";

        assertDoesNotThrow(() -> OpeningHoursParser.validateOpenHours(input1));
        assertDoesNotThrow(() -> OpeningHoursParser.validateOpenHours(input2));
        assertDoesNotThrow(() -> OpeningHoursParser.validateOpenHours(input3));
        assertDoesNotThrow(() -> OpeningHoursParser.validateOpenHours(input4));
        assertDoesNotThrow(() -> OpeningHoursParser.validateOpenHours(input5));
        assertDoesNotThrow(() -> OpeningHoursParser.validateOpenHours(input6));
    }

}