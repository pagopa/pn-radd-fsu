package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.rest.radd.v1.dto.CompleteTransactionResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.TransactionResponseStatus;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import org.junit.jupiter.api.Test;

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
}