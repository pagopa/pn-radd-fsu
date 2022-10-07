package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.exception.ExceptionCodeEnum;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.rest.radd.v1.dto.CompleteTransactionResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.TransactionResponseStatus;
import org.junit.jupiter.api.Test;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompleteTransactionResponseMapperTest {

    @Test
    public void fromResult() {
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage("OK");

        CompleteTransactionResponse response = CompleteTransactionResponseMapper.fromResult();
        assertNotNull(response);
        assertEquals(status.getCode(), response.getStatus().getCode());
        assertEquals(status.getMessage(), response.getStatus().getMessage());
    }

    @Test
    public void fromException() {
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_1);

        RaddGenericException ex = new RaddGenericException(TRANSACTION_NOT_EXIST, ExceptionCodeEnum.NUMBER_2);
        CompleteTransactionResponse response = CompleteTransactionResponseMapper.fromException(ex);
        assertNotNull(response);
        assertEquals(status.getCode(), response.getStatus().getCode());
        assertEquals(ex.getExceptionType().getMessage(), response.getStatus().getMessage());

        ex = new RaddGenericException(TRANSACTION_ALREADY_COMPLETED, ExceptionCodeEnum.NUMBER_1);
        response = CompleteTransactionResponseMapper.fromException(ex);
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_2);
        assertEquals(status.getCode(), response.getStatus().getCode());

        ex = new RaddGenericException(TRANSACTION_ALREADY_ABORTED, ExceptionCodeEnum.NUMBER_1);
        response = CompleteTransactionResponseMapper.fromException(ex);
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_2);
        assertEquals(status.getCode(), response.getStatus().getCode());

        ex = new RaddGenericException(GENERIC_ERROR, ExceptionCodeEnum.NUMBER_1);
        response = CompleteTransactionResponseMapper.fromException(ex);
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_99);
        assertEquals(status.getCode(), response.getStatus().getCode());
    }
}