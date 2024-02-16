package it.pagopa.pn.radd.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class PnZipExceptionTest {
    /**
     * Method under test: {@link PnZipException#PnZipException(String, Throwable)}
     */
    @Test
    void testConstructor() {
        Throwable cause = new Throwable();
        PnZipException actualPnZipException = new PnZipException("An error occurred", cause);

        Throwable cause2 = actualPnZipException.getCause();
        assertSame(cause, cause2);
        Throwable[] suppressed = actualPnZipException.getSuppressed();
        assertEquals(0, suppressed.length);
        assertEquals("An error occurred", actualPnZipException.getLocalizedMessage());
        assertEquals("An error occurred", actualPnZipException.getMessage());
        assertNull(cause2.getLocalizedMessage());
        assertNull(cause2.getCause());
        assertNull(cause2.getMessage());
        assertSame(suppressed, cause2.getSuppressed());
    }
}

