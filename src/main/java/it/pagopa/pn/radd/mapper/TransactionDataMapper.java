package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActStartTransactionRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.AorStartTransactionRequest;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class TransactionDataMapper {

    public TransactionData toTransaction(String uid, ActStartTransactionRequest request){
        TransactionData transactionData = new TransactionData();
        transactionData.setUid(uid);
        transactionData.setRecipientType(request.getRecipientType().getValue());
        transactionData.setRecipientId(request.getRecipientTaxId());
        transactionData.setDelegateId(request.getDelegateTaxId());
        transactionData.setQrCode(request.getQrCode());
        transactionData.setFileKey(request.getFileKey());
        transactionData.setOperationDate(request.getOperationDate());
        transactionData.setOperationId(request.getOperationId());
        transactionData.setChecksum(request.getChecksum());
        transactionData.setOperationType(OperationTypeEnum.ACT);
        transactionData.setVersionId(request.getVersionToken());
        transactionData.setIuns(new ArrayList<>());
        return transactionData;
    }

    public TransactionData toTransaction(String uid, AorStartTransactionRequest request){
        TransactionData transactionData = new TransactionData();
        transactionData.setUid(uid);
        transactionData.setOperationType(OperationTypeEnum.AOR);
        transactionData.setRecipientType(request.getRecipientType().getValue());
        transactionData.setRecipientId(request.getRecipientTaxId());
        transactionData.setDelegateId(request.getDelegateTaxId());
        transactionData.setFileKey(request.getFileKey());
        transactionData.setOperationDate(request.getOperationDate());
        transactionData.setOperationId(request.getOperationId());
        transactionData.setChecksum(request.getChecksum());
        transactionData.setVersionId(request.getVersionToken());
        transactionData.setIuns(new ArrayList<>());
        return transactionData;
    }

}
