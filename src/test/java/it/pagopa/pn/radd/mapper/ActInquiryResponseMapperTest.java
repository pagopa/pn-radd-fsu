package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.ActInquiryResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.ActInquiryResponseStatus;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.IunAlreadyExistsException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import org.junit.jupiter.api.Test;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        RaddGenericException ex = new RaddGenericException(INVALID_INPUT);
        ActInquiryResponse response = ActInquiryResponseMapper.fromException(ex);
        status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_10);
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

    /**
     * Method under test: {@link ActInquiryResponseMapper#fromException(RaddGenericException)}
     */
    @Test
    void testFromException() {
        ActInquiryResponse actualFromExceptionResult = ActInquiryResponseMapper
                .fromException(new RaddGenericException(ExceptionTypeEnum.IUN_NOT_FOUND));
        assertFalse(actualFromExceptionResult.getResult());
        ActInquiryResponseStatus status = actualFromExceptionResult.getStatus();
        assertEquals(ActInquiryResponseStatus.CodeEnum.NUMBER_99, status.getCode());
        assertEquals("Iun not found with params", status.getMessage());
    }

    /**
     * Method under test: {@link ActInquiryResponseMapper#fromException(RaddGenericException)}
     */
    @Test
    void testFromException2() {
        ActInquiryResponse actualFromExceptionResult = ActInquiryResponseMapper
                .fromException(new IunAlreadyExistsException());
        assertFalse(actualFromExceptionResult.getResult());
        ActInquiryResponseStatus status = actualFromExceptionResult.getStatus();
        assertEquals(ActInquiryResponseStatus.CodeEnum.NUMBER_3, status.getCode());
        assertEquals("Stampa già eseguita", status.getMessage());
    }


    /**
     * Method under test: {@link ActInquiryResponseMapper#fromException(RaddGenericException)}
     */
    @Test
    void testFromException4() {
        ActInquiryResponse actualFromExceptionResult = ActInquiryResponseMapper
                .fromException(new RaddGenericException("An error occurred"));
        assertFalse(actualFromExceptionResult.getResult());
        ActInquiryResponseStatus status = actualFromExceptionResult.getStatus();
        assertEquals(ActInquiryResponseStatus.CodeEnum.NUMBER_99, status.getCode());
        assertEquals("An error occurred", status.getMessage());
    }

    /**
     * Method under test: {@link ActInquiryResponseMapper#fromException(RaddGenericException)}
     */
    @Test
    void testFromException5() {
        ActInquiryResponse actualFromExceptionResult = ActInquiryResponseMapper
                .fromException(new RaddGenericException(ExceptionTypeEnum.DOCUMENT_UNAVAILABLE));
        assertFalse(actualFromExceptionResult.getResult());
        ActInquiryResponseStatus status = actualFromExceptionResult.getStatus();
        assertEquals(ActInquiryResponseStatus.CodeEnum.NUMBER_4, status.getCode());
        assertEquals("Documenti non disponibili", status.getMessage());
    }

    /**
     * Method under test: {@link ActInquiryResponseMapper#fromException(RaddGenericException)}
     */
    @Test
    void testFromException6() {
        ActInquiryResponse actualFromExceptionResult = ActInquiryResponseMapper
                .fromException(new RaddGenericException(INVALID_INPUT));
        assertFalse(actualFromExceptionResult.getResult());
        ActInquiryResponseStatus status = actualFromExceptionResult.getStatus();
        assertEquals(ActInquiryResponseStatus.CodeEnum.NUMBER_10, status.getCode());
        assertEquals("Input non valido", status.getMessage());
    }

    /**
     * Method under test: {@link ActInquiryResponseMapper#fromException(RaddGenericException)}
     */
    @Test
    void testFromException7() {
        ActInquiryResponse actualFromExceptionResult = ActInquiryResponseMapper
                .fromException(new RaddGenericException(ExceptionTypeEnum.DOCUMENT_NOT_FOUND));
        assertFalse(actualFromExceptionResult.getResult());
        ActInquiryResponseStatus status = actualFromExceptionResult.getStatus();
        assertEquals(ActInquiryResponseStatus.CodeEnum.NUMBER_2, status.getCode());
        assertEquals("Documenti non più disponibili", status.getMessage());
    }
}