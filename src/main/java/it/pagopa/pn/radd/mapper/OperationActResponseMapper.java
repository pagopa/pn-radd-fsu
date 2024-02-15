package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.OperationActDetailResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.OperationActResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.OperationResponseStatus;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.DateUtils;

public class OperationActResponseMapper {

    private OperationActResponseMapper() {
        // do nothing
    }

    public static OperationActResponse fromResult(OperationActDetailResponse result){
        OperationActResponse response = new OperationActResponse();
        response.setElement(result);
        response.setResult(true);
        OperationResponseStatus status = new OperationResponseStatus();
        status.setCode(OperationResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage(Const.OK);
        response.setStatus(status);
        return response;
    }

    public static OperationActResponse fromException(RaddGenericException ex) {
        OperationActResponse r = new OperationActResponse();
        OperationResponseStatus status = new OperationResponseStatus();
        r.setResult(false);
        status.setMessage(ex.getExceptionType().getMessage());
        status.setCode((ex.getExceptionType() == ExceptionTypeEnum.TRANSACTION_NOT_EXIST) ?
                OperationResponseStatus.CodeEnum.NUMBER_1 : OperationResponseStatus.CodeEnum.NUMBER_99);
        r.setStatus(status);
        return r;
    }

    public static OperationActDetailResponse getDetail(RaddTransactionEntity source){
        OperationActDetailResponse dto = new OperationActDetailResponse();
        dto.setOperationId(source.getOperationId());
        dto.setIun(source.getIun());
        dto.setRecipientTaxId(source.getRecipientId());
        dto.setRecipientType(source.getRecipientType());
        dto.setDelegateTaxId(source.getDelegateId());
        dto.setUid(source.getUid());
        dto.setFileKey(source.getFileKey());
        dto.setOperationEndDate(DateUtils.parseDateString(source.getOperationEndDate()));
        dto.setOperationStartDate(DateUtils.parseDateString(source.getOperationStartDate()));
        dto.setOperationStatus(source.getStatus());
        dto.setErrorReason(source.getErrorReason());
        dto.setQrCode(source.getQrCode());
        dto.setOperationType(source.getOperationType());
        return dto;
    }
}
