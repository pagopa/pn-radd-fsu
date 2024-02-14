package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.OperationResponseStatus;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.OperationsResponse;
import it.pagopa.pn.radd.utils.Const;

import java.util.List;

public class OperationsResponseMapper {


    public static OperationsResponse fromResult(List<String> operations){
        OperationsResponse notificationPracticesResponse = new OperationsResponse();
        OperationResponseStatus status = new OperationResponseStatus();
        if (operations.isEmpty()){
            status.setCode(OperationResponseStatus.CodeEnum.NUMBER_1);
            status.setMessage("Non ci sono operation id");
            notificationPracticesResponse.setResult(false);
        } else {
            status.setCode(OperationResponseStatus.CodeEnum.NUMBER_0);
            notificationPracticesResponse.setOperationIds(operations);
            status.setMessage(Const.OK);
            notificationPracticesResponse.setResult(true);
        }
        notificationPracticesResponse.setStatus(status);
        return notificationPracticesResponse;
    }

}
