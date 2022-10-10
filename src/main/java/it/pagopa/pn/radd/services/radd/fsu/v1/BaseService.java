package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileDownloadResponseDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.DateUtils;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import it.pagopa.pn.radd.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.*;

@Slf4j
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
                                .flatMap(delegateEnsure -> {
                                    transaction.setEnsureRecipientId(ensureRecipient);
                                    transaction.setEnsureDelegateId(delegateEnsure);
                                    return Mono.just(transaction);
                                });
                    }
                    transaction.setEnsureRecipientId(ensureRecipient);
                    return  Mono.just(transaction);
                });
    }

    protected Mono<FileDownloadResponseDto> verifyCheckSum(TransactionData transaction){
        return this.safeStorageClient.getFile(transaction.getFileKey()).map(response -> {
            /*
            if (!StringUtils.equals(response.getDocumentStatus(), Const.PRELOADED)){
                throw new RaddGenericException(DOCUMENT_STATUS_VALIDATION, KO);
            }

            if (!StringUtils.equals(transaction.getVersionId(), transaction.getVersionId())){
                throw new RaddGenericException(VERSION_ID_VALIDATION, KO);
            }
            */
            if (Strings.isBlank(response.getChecksum()) ||
                    !response.getChecksum().equals(transaction.getChecksum())){
                throw new RaddGenericException(CHECKSUM_VALIDATION);
            }
            return response;
        });
    }

    protected Mono<TransactionData> updateFileMetadata(TransactionData transactionData){
        return this.safeStorageClient.updateFileMetadata(transactionData.getFileKey()).map(resp -> transactionData);
    }

    protected Mono<RaddTransactionEntity> createTransaction(TransactionData transaction, String uid){
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setIuns(transaction.getIuns());
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
        return this.raddTransactionDAO.createRaddTransaction(entity);
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

    protected Mono<RaddTransactionEntity> settingErrorReason(Exception ex, String operationId, OperationTypeEnum operationType){
        return this.raddTransactionDAO.getTransaction(operationId, operationType)
                .flatMap(entity -> {
                    entity.setStatus(Const.ERROR);
                    entity.setErrorReason((ex.getMessage() == null) ? "Generic message" : ex.getMessage());
                    if(ex instanceof RaddGenericException){
                        entity.setErrorReason(((RaddGenericException) ex).getExceptionType().getMessage());
                        log.info("Error message {}", ex.getMessage());
                    } else if (ex instanceof PnRaddException){
                        entity.setErrorReason(((PnRaddException) ex).getWebClientEx().getMessage());
                    }
                    return this.raddTransactionDAO.updateStatus(entity);
                })
                .onErrorResume(exception -> {
                    log.error("Exception into settings Reason {}", exception.getMessage());
                    return Mono.just(new RaddTransactionEntity());
                });
    }

    protected void checkTransactionStatus(RaddTransactionEntity entity) {
        if (StringUtils.equals(entity.getStatus(), Const.COMPLETED)) {
            throw new RaddGenericException(TRANSACTION_ALREADY_COMPLETED);
        } else if (StringUtils.equals(entity.getStatus(), Const.ABORTED)){
            throw new RaddGenericException(TRANSACTION_ALREADY_ABORTED);
        } else if (StringUtils.equals(entity.getStatus(), Const.ERROR)){
            throw new RaddGenericException(TRANSACTION_ERROR_STATUS);
        }
    }

}
