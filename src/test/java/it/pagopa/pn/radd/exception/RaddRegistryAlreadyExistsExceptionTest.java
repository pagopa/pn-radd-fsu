package it.pagopa.pn.radd.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class RaddRegistryAlreadyExistsExceptionTest {

    @Test
    void testConstructor_setsCorrectExceptionType() {
        RaddRegistryAlreadyExistsException ex = new RaddRegistryAlreadyExistsException();
        assertNotNull(ex);
        assertEquals(ExceptionTypeEnum.RADD_REGISTRY_ALREADY_EXISTS, ex.getExceptionType());
    }

    @Test
    void testHttpStatus_isBadRequestOrExpected() {
        RaddRegistryAlreadyExistsException ex = new RaddRegistryAlreadyExistsException();
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testMessage_isCorrect() {
        RaddRegistryAlreadyExistsException ex = new RaddRegistryAlreadyExistsException();
        assertEquals(ExceptionTypeEnum.RADD_REGISTRY_ALREADY_EXISTS.getMessage(), ex.getMessage());
    }

    @Test
    void testToString_andOtherDefaults() {
        RaddRegistryAlreadyExistsException ex = new RaddRegistryAlreadyExistsException();
        assertTrue(ex.toString().contains("RaddRegistryAlreadyExistsException"));
    }
}