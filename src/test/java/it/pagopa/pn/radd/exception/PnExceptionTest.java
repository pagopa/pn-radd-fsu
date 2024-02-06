package it.pagopa.pn.radd.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PnExceptionTest {
    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link PnException#PnException(String, String)}
     *   <li>{@link PnException#getDescription()}
     *   <li>{@link PnException#getStatus()}
     * </ul>
     */
    @Test
    void testConstructor() {
        PnException actualPnException = new PnException("An error occurred", "The characteristics of someone or something");

        assertEquals("The characteristics of someone or something", actualPnException.getDescription());
        assertEquals(400, actualPnException.getStatus());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link PnException#PnException(String, String, int)}
     *   <li>{@link PnException#getDescription()}
     *   <li>{@link PnException#getStatus()}
     * </ul>
     */
    @Test
    void testConstructor2() {
        PnException actualPnException = new PnException("An error occurred",
                "The characteristics of someone or something", 2);

        assertEquals("The characteristics of someone or something", actualPnException.getDescription());
        assertEquals(2, actualPnException.getStatus());
    }
}

