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
        IunAlreadyExistsException actualIunAlreadyExistsException = new IunAlreadyExistsException();
        assertEquals(HttpStatus.BAD_REQUEST, actualIunAlreadyExistsException.getStatus());
        assertNotNull(actualIunAlreadyExistsException.getMessage());
        assertNull(actualIunAlreadyExistsException.getExtra());
        assertEquals(ExceptionTypeEnum.ALREADY_COMPLETE_PRINT, actualIunAlreadyExistsException.getExceptionType());
    }
}

