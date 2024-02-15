package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.AbortTransactionResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.TransactionResponseStatus;
import it.pagopa.pn.radd.exception.RaddGenericException;
import org.junit.jupiter.api.Test;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AbortTransactionResponseMapperTest {

    @Test
    void fromResult() {
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage("OK");

        AbortTransactionResponse response = AbortTransactionResponseMapper.fromResult();
        assertNotNull(response);
        assertEquals(status.getCode(), response.getStatus().getCode());
        assertEquals(status.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void fromException() {
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_1);

        RaddGenericException ex = new RaddGenericException(TRANSACTION_NOT_EXIST);
        AbortTransactionResponse response = AbortTransactionResponseMapper.fromException(ex);
        assertNotNull(response);
        assertEquals(status.getCode(), response.getStatus().getCode());
        assertEquals(ex.getExceptionType().getMessage(), response.getStatus().getMessage());

        ex = new RaddGenericException(TRANSACTION_ALREADY_COMPLETED);
        response = AbortTransactionResponseMapper.fromException(ex);
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_2);
        assertEquals(status.getCode(), response.getStatus().getCode());

        ex = new RaddGenericException(GENERIC_ERROR);
        response = AbortTransactionResponseMapper.fromException(ex);
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_99);
        assertEquals(status.getCode(), response.getStatus().getCode());

        ex = new RaddGenericException("Error");
        response = AbortTransactionResponseMapper.fromException(ex);
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_99);
        assertEquals("Error", response.getStatus().getMessage());
    }
}