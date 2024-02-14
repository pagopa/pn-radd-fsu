package it.pagopa.pn.radd.services.radd.fsu.v1;


import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.pojo.RaddTransactionStatusEnum;
import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import it.pagopa.pn.radd.utils.Utils;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.*;
import static it.pagopa.pn.radd.utils.Const.KO;

@CustomLog
public class BaseService {
    protected final PnDataVaultClient pnDataVaultClient;
    protected final RaddTransactionDAO raddTransactionDAO;
    protected final PnSafeStorageClient safeStorageClient;

    public BaseService(PnDataVaultClient pnDataVaultClient, RaddTransactionDAO raddTransactionDAO, PnSafeStorageClient pnSafeStorageClient) {
        this.pnDataVaultClient = pnDataVaultClient;
        this.raddTransactionDAO = raddTransactionDAO;
        safeStorageClient = pnSafeStorageClient;
    }

    protected Mono<TransactionData> getEnsureRecipientAndDelegate(TransactionData transaction){
        return getEnsureFiscalCode(transaction.getRecipientId(), transaction.getRecipientType())
                .flatMap(ensureRecipient -> {
                    if (!Strings.isBlank(transaction.getDelegateId())){
                        return getEnsureFiscalCode(transaction.getDelegateId(), Const.PF)
                                .map(delegateEnsure -> {
                                    transaction.setEnsureRecipientId(ensureRecipient);
                                    transaction.setEnsureDelegateId(delegateEnsure);
                                    return transaction;
                                });
                    }
                    transaction.setEnsureRecipientId(ensureRecipient);
                    return  Mono.just(transaction);
                });
    }

    protected Mono<TransactionData> verifyCheckSum(TransactionData transaction){
        return this.safeStorageClient.getFile(transaction.getFileKey()).map(response -> {
            //Da decommentare dopo l'aggiornamento dell'interfaccia ss
            //log.debug("Document status is : {}", response.getDocumentStatus());
            //if (!StringUtils.equals(response.getDocumentStatus(), Const.PRELOADED)){
            //    throw new RaddGenericException(DOCUMENT_STATUS_VALIDATION, KO);
            //}

            log.debug("Document checksum is : {}", response.getChecksum());
            if (Strings.isBlank(response.getChecksum()) ||
                    !response.getChecksum().equals(transaction.getChecksum())) {
                log.error("Request contains Document checksum : {}", transaction.getChecksum());
                log.error("Response contains Document version: {} checksum: {}", response.getVersionId(), response.getChecksum());
                throw new RaddGenericException(CHECKSUM_VALIDATION);
            }
            return transaction;
        });
    }

    protected Mono<TransactionData> updateFileMetadata(TransactionData transactionData){
        return this.safeStorageClient.updateFileMetadata(transactionData.getFileKey()).map(resp -> transactionData);
    }

    protected Mono<String> getEnsureFiscalCode(String fiscalCode, String type){
        if (StringUtils.isEmpty(fiscalCode) || !Utils.checkPersonType(type)) {
            log.error("Missing input parameters");
            return Mono.error(new PnInvalidInputException("recipientTaxId o recipientType non valorizzato correttamente"));
        }

        return this.pnDataVaultClient.getEnsureFiscalCode(fiscalCode, type)
                .map(response -> {
                    if (Strings.isEmpty(response)){
                        throw new RaddGenericException(ENSURE_FISCAL_CODE_EMPTY);
                    }
                    return response;
                });
    }

    protected Mono<RaddTransactionEntity> settingErrorReason(Exception ex, String operationId, OperationTypeEnum operationType, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId){
        return this.raddTransactionDAO.getTransaction(String.valueOf(xPagopaPnCxType), xPagopaPnCxId, operationId, operationType)
                .map(entity -> {
                    entity.setErrorReason((ex.getMessage() == null) ? "Generic message" : ex.getMessage());
                    if(ex instanceof RaddGenericException raddGenericException){
                        entity.setErrorReason(raddGenericException.getExceptionType().getMessage());
                        log.error("Error message {}", raddGenericException.getMessage(), raddGenericException);
                    } else if (ex instanceof PnRaddException pnRaddException){
                        entity.setErrorReason(pnRaddException.getWebClientEx().getMessage());
                    }
                    return entity;
                })
                .flatMap(entity -> raddTransactionDAO.updateStatus(entity, RaddTransactionStatusEnum.ERROR))
                .onErrorResume(exception -> {
                    log.error("Exception into settings Reason {}", exception.getMessage(), exception);
                    return Mono.just(new RaddTransactionEntity());
                });
    }

    protected void checkTransactionStatus(RaddTransactionEntity entity) {
        if (StringUtils.equals(entity.getStatus(), RaddTransactionStatusEnum.COMPLETED.name())) {
            throw new RaddGenericException(TRANSACTION_ALREADY_COMPLETED);
        } else if (StringUtils.equals(entity.getStatus(), RaddTransactionStatusEnum.ABORTED.name())){
            throw new RaddGenericException(TRANSACTION_ALREADY_ABORTED);
        } else if (StringUtils.equals(entity.getStatus(), RaddTransactionStatusEnum.ERROR.name())){
            throw new RaddGenericException(TRANSACTION_ERROR_STATUS);
        }
    }


}
