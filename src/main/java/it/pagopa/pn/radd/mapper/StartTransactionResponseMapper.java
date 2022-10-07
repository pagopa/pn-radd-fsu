package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.rest.radd.v1.dto.StartTransactionResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.StartTransactionResponseStatus;
import it.pagopa.pn.radd.utils.Const;

import java.math.BigDecimal;
import java.util.List;

public class StartTransactionResponseMapper {




    public static StartTransactionResponse fromResult(List<String> result){
        StartTransactionResponse response = new StartTransactionResponse();
        response.setUrlList(result);
        StartTransactionResponseStatus status = new StartTransactionResponseStatus();
        status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_0);
        response.setStatus(status);
        status.setMessage(Const.OK);
        return response;
    }


    public static StartTransactionResponse fromException(RaddGenericException ex){
        StartTransactionResponse response = new StartTransactionResponse();
        StartTransactionResponseStatus status = new StartTransactionResponseStatus();
        status.setMessage(ex.getExceptionType().getMessage());
        status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_99);
        if (ex.getExceptionType() == ExceptionTypeEnum.RETRY_AFTER){
            status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_2);
            status.setRetryAfter((BigDecimal) ex.getExtra());
        }
        response.setStatus(status);
        return response;
    }

}
