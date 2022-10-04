package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponseStatus;
import it.pagopa.pn.radd.utils.Const;

public class ActInquiryResponseMapper {

    public static ActInquiryResponse fromResult(){
        ActInquiryResponse actInquiryResponse = new ActInquiryResponse();
        actInquiryResponse.setResult(true);
        ActInquiryResponseStatus status = new ActInquiryResponseStatus();
        status.setMessage(Const.OK);
        status.code(ActInquiryResponseStatus.CodeEnum.NUMBER_0);
        actInquiryResponse.setStatus(status);
        return actInquiryResponse;
    }


    public static ActInquiryResponse fromException(RaddGenericException ex){
        ActInquiryResponse r = new ActInquiryResponse();
        r.setResult(false);
        ActInquiryResponseStatus status = new ActInquiryResponseStatus();
        status.setMessage((ex.getExceptionType() == null) ? ex.getMessage() : ex.getExceptionType().getMessage());

        if (ex.getExceptionType() == ExceptionTypeEnum.QR_CODE_VALIDATION) {
            status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_1);
        } else if (ex.getExceptionType() == ExceptionTypeEnum.DOCUMENT_NOT_FOUND) {
            status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_2);
        } else if (ex.getExceptionType() == ExceptionTypeEnum.ALREADY_COMPLETE_PRINT) {
            status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_3);
        } else if (ex.getExceptionType() == ExceptionTypeEnum.CF_OR_QRCODE_NOT_VALID) {
            status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_1);
        } else {
            status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_99);
        }

        r.setStatus(status);
        return r;
    }
}
