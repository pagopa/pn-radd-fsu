package it.pagopa.pn.radd.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class IunAlreadyExistsExceptionTest {
    /**
     * Method under test: default or parameterless constructor of {@link IunAlreadyExistsException}
     */
    @Test
    void testConstructor() {
        int maxPrintRequest = 1;
        IunAlreadyExistsException actualIunAlreadyExistsException = new IunAlreadyExistsException(maxPrintRequest);
        assertEquals(HttpStatus.BAD_REQUEST, actualIunAlreadyExistsException.getStatus());
        assertNotNull(actualIunAlreadyExistsException.getMessage());
        assertNull(actualIunAlreadyExistsException.getExtra());
        assertEquals(actualIunAlreadyExistsException.getMessage(),String.format(ExceptionTypeEnum.ALREADY_COMPLETE_PRINT.getMessage(), maxPrintRequest, "stampa"));
        assertEquals(ExceptionTypeEnum.ALREADY_COMPLETE_PRINT, actualIunAlreadyExistsException.getExceptionType());
    }
}

