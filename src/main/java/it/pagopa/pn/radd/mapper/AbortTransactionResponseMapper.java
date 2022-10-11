package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.rest.radd.v1.dto.AbortTransactionResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.TransactionResponseStatus;
import it.pagopa.pn.radd.utils.Const;

public class AbortTransactionResponseMapper {

    private AbortTransactionResponseMapper () {
        // do nothing
    }

    public static AbortTransactionResponse fromResult(){
        AbortTransactionResponse response = new AbortTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setMessage(Const.OK);
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_0);
        response.setStatus(status);
        return response;
    }

    public static AbortTransactionResponse fromException(RaddGenericException ex){
        AbortTransactionResponse response = new AbortTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        if (ex.getExceptionType() == ExceptionTypeEnum.TRANSACTION_NOT_EXIST) {
            status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_1);
        } else if (ex.getExceptionType() == ExceptionTypeEnum.TRANSACTION_ALREADY_COMPLETED) {
            status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_2);
        } else {
            status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_99);
        }
        status.setMessage((ex.getExceptionType() == null) ? ex.getMessage() : ex.getExceptionType().getMessage());
        response.setStatus(status);
        return response;
    }

}
