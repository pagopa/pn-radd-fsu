package it.pagopa.pn.radd.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaperNotificationFailedEmptyExceptionTest {
    /**
     * Method under test: default or parameterless constructor of {@link PaperNotificationFailedEmptyException}
     */
    @Test
    void testConstructor() {
        PaperNotificationFailedEmptyException actualPaperNotificationFailedEmptyException = new PaperNotificationFailedEmptyException();
        assertNull(actualPaperNotificationFailedEmptyException.getStatus());
        assertNotNull(actualPaperNotificationFailedEmptyException.getMessage());
        assertNull(actualPaperNotificationFailedEmptyException.getExtra());
        assertEquals(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF,
                actualPaperNotificationFailedEmptyException.getExceptionType());
    }
}

