package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.rest.radd.v1.dto.CompleteTransactionResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.TransactionResponseStatus;
import it.pagopa.pn.radd.utils.Const;

public class CompleteTransactionResponseMapper {

    public static CompleteTransactionResponse fromResult(){
        CompleteTransactionResponse response = new CompleteTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage(Const.OK);
        response.setStatus(status);
        return response;
    }



    public static CompleteTransactionResponse fromException(RaddGenericException ex){
        CompleteTransactionResponse r = new CompleteTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setMessage((ex.getExceptionType() == null) ? ex.getMessage() : ex.getExceptionType().getMessage());
        if (ex.getExceptionType() == ExceptionTypeEnum.TRANSACTION_NOT_EXIST) {
            status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_1);
        } else if(ex.getExceptionType() == ExceptionTypeEnum.TRANSACTION_ALREADY_COMPLETED || ex.getExceptionType() == ExceptionTypeEnum.TRANSACTION_ALREADY_ABORTED){
            status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_2);
        } else {
            status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_99);
        }
        r.setStatus(status);
        return r;
    }

}
