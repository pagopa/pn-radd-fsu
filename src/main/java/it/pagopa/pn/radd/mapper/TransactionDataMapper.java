package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActStartTransactionRequest;
import org.springframework.stereotype.Component;

@Component
public class TransactionDataMapper {

    public TransactionData toTransaction(ActStartTransactionRequest request){
        TransactionData transactionData = new TransactionData();
        transactionData.setRecipientType(request.getRecipientType().getValue());
        transactionData.setRecipientId(request.getRecipientTaxId());
        transactionData.setDelegateId(request.getDelegateTaxId());
        transactionData.setQrCode(request.getQrCode());
        transactionData.setFileKey(request.getFileKey());
        transactionData.setOperationDate(request.getOperationDate());
        transactionData.setOperationId(request.getOperationId());
        transactionData.setChecksum(request.getChecksum());
        return transactionData;
    }

}
