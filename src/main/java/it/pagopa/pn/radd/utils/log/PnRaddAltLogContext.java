package it.pagopa.pn.radd.utils.log;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.ResponseStatus;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.TransactionResponseStatus;
import it.pagopa.pn.radd.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

public class PnRaddAltLogContext {
    private String uid="";
    private String cxId="";
    private String cxType="";
    private String recipientInternalId="";
    private String delegateInternalId="";
    private String transactionId="";
    private String requestFileKey ="";
    private String downloadedFilekeys="";
    private String result="";
    private String status="";
    private String operationId="";

    public PnRaddAltLogContext addUid(String uid) {
        this.uid = "uid=%s ".formatted(uid);
        return this;
    }

    public PnRaddAltLogContext addCxId(String cxId) {
        this.cxId = "cxId=%s ".formatted(cxId);
        return this;
    }

    public PnRaddAltLogContext addCxType(String cxType) {
        this.cxType = "cxType=%s ".formatted(cxType);
        return this;
    }

    public PnRaddAltLogContext addRecipientInternalId(String recipientInternalId){
        this.recipientInternalId = "recipientInternalId=%s ".formatted(recipientInternalId);
        return this;
    }

    public PnRaddAltLogContext addDelegateInternalId(String delegateInternalId) {
        this.delegateInternalId = "delegateInternalId=%s ".formatted(delegateInternalId);
        return this;
    }

    public PnRaddAltLogContext addTransactionId(String transactionId) {
        this.transactionId = "transactionId=%s ".formatted(transactionId);
        return this;
    }

    public PnRaddAltLogContext addRequestFileKey(String requestFileKey) {
        this.requestFileKey = "requestFileKey=%s ".formatted(requestFileKey);
        return this;
    }

    public PnRaddAltLogContext addDownloadFilekeys(List<String> presignedUrls) {
        String joinedFileKeys = presignedUrls.stream().map(Utils::getFileKeyFromPresignedUrl).collect(Collectors.joining(","));
        this.downloadedFilekeys = "downloadedFilekeys=[ %s ] ".formatted(joinedFileKeys);
        return this;
    }

    public PnRaddAltLogContext addResponseResult(Boolean result) {
        this.result = "result=%s ".formatted(result);
        return this;
    }

    public PnRaddAltLogContext addResponseStatus(ResponseStatus status) {
        this.status = "status=%s ".formatted(status.toString());
        return this;
    }

    public PnRaddAltLogContext addResponseStatus(TransactionResponseStatus status) {
        this.status = "status=%s ".formatted(status.toString());
        return this;
    }

    public PnRaddAltLogContext addOperationId(String operationId) {
        this.operationId = "operationId=%s ".formatted(operationId);
        return this;
    }

    public String logContext() {
        return uid + cxId + cxType + operationId + transactionId + recipientInternalId + delegateInternalId + requestFileKey
                + downloadedFilekeys + result + status;
    }



}
