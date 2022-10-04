package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationDetailResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationResponseStatus;

public class OperationResponseMapper {

    public static OperationResponse fromResult(OperationDetailResponse result){
        OperationResponse response = new OperationResponse();
        response.setElement(result);
        response.setResult(true);
        OperationResponseStatus status = new OperationResponseStatus();
        status.setCode(OperationResponseStatus.CodeEnum.NUMBER_0);
        response.setStatus(status);
        return response;
    }

    public static OperationResponse fromException(RaddGenericException ex) {
        OperationResponse r = new OperationResponse();
        OperationResponseStatus status = new OperationResponseStatus();
        status.setMessage(ex.getExceptionType().getMessage());
        status.setCode((ex.getExceptionType() == ExceptionTypeEnum.TRANSACTION_NOT_EXIST) ?
                OperationResponseStatus.CodeEnum.NUMBER_1 : OperationResponseStatus.CodeEnum.NUMBER_99);

        r.setStatus(status);
        return r;
    }
}
