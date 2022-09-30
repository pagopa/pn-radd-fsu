package it.pagopa.pn.radd.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionTest {

    @Test
    public void testCustomExceptionTest() {
        assertEquals(HttpStatus.CONFLICT.value(), new RaddDocumentStatusException("").getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(), new RaddFiscalCodeEnsureException().getStatusCode());
    }

    @Test
    public void testExceptionTest() {
        assertEquals(HttpStatus.BAD_REQUEST.value(), new RaddChecksumException().getStatus());
    }

}
