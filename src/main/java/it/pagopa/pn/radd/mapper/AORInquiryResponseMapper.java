package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.rest.radd.v1.dto.AORInquiryResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.ResponseStatus;

public class AORInquiryResponseMapper {

    public static AORInquiryResponse fromResult(){
        AORInquiryResponse response = new AORInquiryResponse();
        ResponseStatus status = new ResponseStatus();
        response.setResult(true);
        response.setStatus(status);
        status.setCode(ResponseStatus.CodeEnum.NUMBER_0);
        return response;
    }

    public static AORInquiryResponse fromException(RaddGenericException ex){
        AORInquiryResponse response = new AORInquiryResponse();
        ResponseStatus status = new ResponseStatus();
        response.setResult(false);
        response.setStatus(status);
        status.setCode(ResponseStatus.CodeEnum.NUMBER_99);
        status.setMessage((ex.getExceptionType() != null) ? ex.getExceptionType().getMessage() : ex.getMessage());
        return response;
    }

}
