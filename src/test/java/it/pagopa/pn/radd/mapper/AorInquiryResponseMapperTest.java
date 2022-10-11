package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.rest.radd.v1.dto.AORInquiryResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.ResponseStatus;
import org.junit.jupiter.api.Test;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.QR_CODE_VALIDATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AorInquiryResponseMapperTest {

    @Test
    void fromResult() {
        ResponseStatus status = new ResponseStatus();
        status.setCode(ResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage("OK");

        AORInquiryResponse response = AorInquiryResponseMapper.fromResult();
        assertNotNull(response);
        assertEquals(status.getCode(), response.getStatus().getCode());
        assertEquals(status.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void fromException() {
        ResponseStatus status = new ResponseStatus();
        status.setCode(ResponseStatus.CodeEnum.NUMBER_99);

        RaddGenericException ex = new RaddGenericException(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF);
        AORInquiryResponse response = AorInquiryResponseMapper.fromException(ex);
        assertEquals(ex.getExceptionType().getMessage(), response.getStatus().getMessage());

        ex = new RaddGenericException("Error message");
        response = AorInquiryResponseMapper.fromException(ex);
        assertEquals(ex.getMessage(), response.getStatus().getMessage());
    }
}