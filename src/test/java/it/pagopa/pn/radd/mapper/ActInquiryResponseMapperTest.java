package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.ActInquiryResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.ActInquiryResponseStatus;
import it.pagopa.pn.radd.exception.RaddGenericException;
import org.junit.jupiter.api.Test;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ActInquiryResponseMapperTest {

    @Test
    void fromResult() {
        ActInquiryResponseStatus status = new ActInquiryResponseStatus();
        status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage("OK");

        ActInquiryResponse response = ActInquiryResponseMapper.fromResult();
        assertNotNull(response);
        assertEquals(status.getCode(), response.getStatus().getCode());
        assertEquals(status.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void fromException() {
        ActInquiryResponseStatus status = new ActInquiryResponseStatus();
        status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_1);

        RaddGenericException ex = new RaddGenericException(QR_CODE_VALIDATION);
        ActInquiryResponse response = ActInquiryResponseMapper.fromException(ex);
        assertNotNull(response);
        assertEquals(status.getCode(), response.getStatus().getCode());
        assertEquals(ex.getExceptionType().getMessage(), response.getStatus().getMessage());

        status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_10);
        ex = new RaddGenericException(CF_OR_QRCODE_NOT_VALID);
        response = ActInquiryResponseMapper.fromException(ex);
        assertEquals(status.getCode(), response.getStatus().getCode());

        ex = new RaddGenericException(DOCUMENT_NOT_FOUND);
        response = ActInquiryResponseMapper.fromException(ex);
        status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_2);
        assertEquals(status.getCode(), response.getStatus().getCode());

        ex = new RaddGenericException(ALREADY_COMPLETE_PRINT);
        response = ActInquiryResponseMapper.fromException(ex);
        status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_3);
        assertEquals(status.getCode(), response.getStatus().getCode());

        ex = new RaddGenericException("Error");
        response = ActInquiryResponseMapper.fromException(ex);
        status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_99);
        assertEquals("Error", response.getStatus().getMessage());
    }
}