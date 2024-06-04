package it.pagopa.pn.radd.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class TransactionAlreadyExistsExceptionTest {
    /**
     * Method under test: default or parameterless constructor of {@link TransactionAlreadyExistsException}
     */
    @Test
    void testConstructor() {
        TransactionAlreadyExistsException actualTransactionAlreadyExistsException = new TransactionAlreadyExistsException();
        assertEquals(HttpStatus.BAD_REQUEST, actualTransactionAlreadyExistsException.getStatus());
        assertNull(actualTransactionAlreadyExistsException.getMessage());
        assertNull(actualTransactionAlreadyExistsException.getExtra());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_EXIST,
                actualTransactionAlreadyExistsException.getExceptionType());
    }
}

