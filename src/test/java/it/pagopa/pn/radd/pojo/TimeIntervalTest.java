package it.pagopa.pn.radd.pojo;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class TimeIntervalTest {
    /**
     * Method under test: {@link TimeInterval#canEqual(Object)}
     */
    @Test
    void testCanEqual() {
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        assertFalse((new TimeInterval(start, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()))
                .canEqual("Other"));
    }

    /**
     * Method under test: {@link TimeInterval#canEqual(Object)}
     */
    @Test
    void testCanEqual2() {
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        TimeInterval timeInterval = new TimeInterval(start,
                LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        Instant start2 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        assertTrue(timeInterval.canEqual(
                new TimeInterval(start2, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant())));
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link TimeInterval#TimeInterval(Instant, Instant)}
     *   <li>{@link TimeInterval#toString()}
     *   <li>{@link TimeInterval#getEnd()}
     *   <li>{@link TimeInterval#getStart()}
     * </ul>
     */
    @Test
    void testConstructor() {
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        Instant end = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        TimeInterval actualTimeInterval = new TimeInterval(start, end);
        String actualToStringResult = actualTimeInterval.toString();
        Instant expectedEnd = end.EPOCH;
        Instant end2 = actualTimeInterval.getEnd();
        assertSame(expectedEnd, end2);
        assertSame(end2, actualTimeInterval.getStart());
        assertEquals("TimeInterval(start=1970-01-01T00:00:00Z, end=1970-01-01T00:00:00Z)", actualToStringResult);
    }

    /**
     * Method under test: {@link TimeInterval#equals(Object)}
     */
    @Test
    void testEquals() {
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        assertNotEquals(
                new TimeInterval(start, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()), null);
    }

    /**
     * Method under test: {@link TimeInterval#equals(Object)}
     */
    @Test
    void testEquals2() {
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        assertNotEquals(
                new TimeInterval(start, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()),
                "Different type to TimeInterval");
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link TimeInterval#equals(Object)}
     *   <li>{@link TimeInterval#hashCode()}
     * </ul>
     */
    @Test
    void testEquals3() {
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        TimeInterval timeInterval = new TimeInterval(start,
                LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertEquals(timeInterval, timeInterval);
        int expectedHashCodeResult = timeInterval.hashCode();
        assertEquals(expectedHashCodeResult, timeInterval.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link TimeInterval#equals(Object)}
     *   <li>{@link TimeInterval#hashCode()}
     * </ul>
     */
    @Test
    void testEquals4() {
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        TimeInterval timeInterval = new TimeInterval(start,
                LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        Instant start2 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        TimeInterval timeInterval2 = new TimeInterval(start2,
                LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        assertEquals(timeInterval, timeInterval2);
        int expectedHashCodeResult = timeInterval.hashCode();
        assertEquals(expectedHashCodeResult, timeInterval2.hashCode());
    }

    /**
     * Method under test: {@link TimeInterval#equals(Object)}
     */
    @Test
    void testEquals5() {
        Instant start = LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        TimeInterval timeInterval = new TimeInterval(start,
                LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        Instant start2 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        assertNotEquals(timeInterval,
                new TimeInterval(start2, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
    }

    /**
     * Method under test: {@link TimeInterval#equals(Object)}
     */
    @Test
    void testEquals6() {
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        TimeInterval timeInterval = new TimeInterval(start,
                LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        Instant start2 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        assertNotEquals(timeInterval,
                new TimeInterval(start2, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
    }
}

