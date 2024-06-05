package it.pagopa.pn.radd.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ZipAttachmentNotFoundExceptionTest {
    /**
     * Method under test: default or parameterless constructor of {@link ZipAttachmentNotFoundException}
     */
    @Test
    void testConstructor() {
        ZipAttachmentNotFoundException actualIunAlreadyExistsException = new ZipAttachmentNotFoundException();
        assertEquals(HttpStatus.NOT_FOUND, actualIunAlreadyExistsException.getStatus());
        assertNotNull(actualIunAlreadyExistsException.getMessage());
        assertNull(actualIunAlreadyExistsException.getExtra());
        assertEquals(ExceptionTypeEnum.ZIP_ATTACHMENT_URL_NOT_FOUND, actualIunAlreadyExistsException.getExceptionType());
    }
}

