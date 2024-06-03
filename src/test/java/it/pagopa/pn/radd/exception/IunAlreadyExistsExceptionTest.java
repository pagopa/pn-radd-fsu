package it.pagopa.pn.radd.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IunAlreadyExistsExceptionTest {
    /**
     * Method under test: default or parameterless constructor of {@link IunAlreadyExistsException}
     */
    @Test
    void testConstructor() {
        IunAlreadyExistsException actualIunAlreadyExistsException = new IunAlreadyExistsException();
        assertNull(actualIunAlreadyExistsException.getStatus());
        assertNotNull(actualIunAlreadyExistsException.getMessage());
        assertNull(actualIunAlreadyExistsException.getExtra());
        assertEquals(ExceptionTypeEnum.ALREADY_COMPLETE_PRINT, actualIunAlreadyExistsException.getExceptionType());
    }
}

