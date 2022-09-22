package it.pagopa.pn.radd.exception;

import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.radd.exception.ExceptionHelper;
import it.pagopa.pn.radd.exception.RaddTransactionAlreadyExist;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExceptionHelperTest {

    @Test
    void handlePnException() {
        Problem res = ExceptionHelper.handleException(new RaddTransactionAlreadyExist(), HttpStatus.BAD_REQUEST);
        assertNotNull(res);
        assertEquals(400, res.getStatus());
    }

    @Test
    void handleException() {
        Problem res = ExceptionHelper.handleException(new NullPointerException(), HttpStatus.BAD_REQUEST);
        assertNotNull(res);
        assertEquals(HttpStatus.BAD_REQUEST.value(), res.getStatus());
    }
}