package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActStartTransactionRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.AorStartTransactionRequest;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.DateUtils;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

@Component
public class TransactionDataMapper {

    private TransactionDataMapper() {
        // do nothing
    }

    public RaddTransactionEntity toEntity(String uid, TransactionData transaction){
        RaddTransactionEntity entity = new RaddTransactionEntity();
        if (transaction.getIun() == null || StringUtils.isBlank(transaction.getIun())){
            entity.setIun(Arrays.toString(transaction.getIuns().toArray()));
        } else {
            entity.setIun(transaction.getIun());
        }
        entity.setOperationId(transaction.getOperationId());
        entity.setDelegateId(transaction.getEnsureDelegateId());
        entity.setRecipientId(transaction.getEnsureRecipientId());
        entity.setRecipientType(transaction.getRecipientType());
        entity.setFileKey(transaction.getFileKey());
        entity.setUid(uid);
        entity.setOperationType(transaction.getOperationType().name());
        entity.setQrCode(transaction.getQrCode());
        entity.setStatus(Const.STARTED);
        entity.setErrorReason("");
        entity.setOperationStartDate(DateUtils.formatDate(transaction.getOperationDate()));
        return entity;
    }

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
