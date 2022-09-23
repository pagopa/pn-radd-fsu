package it.pagopa.pn.radd.services.radd.fsu.v1;

import io.netty.handler.codec.http.HttpResponseStatus;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BaseService {

    public BaseService() {
        // do nothing
    }

    protected Mono<String> getEnsureFiscalCode(String fiscalCode, String type, PnDataVaultClient pnDataVaultClient){
        if (StringUtils.isEmpty(fiscalCode) || !Utils.checkPersonType(type)) {
            log.error("Missing input parameters");
            return Mono.error(new PnInvalidInputException("recipientTaxId o recipientType non valorizzato correttamente"));
        }

        return pnDataVaultClient.getEnsureFiscalCode(fiscalCode, type)
                .map(response -> {
                    if (Strings.isEmpty(response)){
                        throw new RaddFiscalCodeEnsureException();
                    }
                    return response;
                }).onErrorResume(Mono::error);
    }

    protected CompleteTransactionResponse completeErrorResponse(Throwable ex) {
        CompleteTransactionResponse r = new CompleteTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        if (ex instanceof RaddTransactionNoExistedException) {
            status.setMessage(Const.NOT_EXISTS_OPERAION);
            status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_1);

        } else if (ex instanceof RaddTransactionStatusException) {
            if (((RaddTransactionStatusException)ex).getStatus() == HttpResponseStatus.FORBIDDEN.code()) {
                status.setMessage(Const.ABORT_OPERATION);
            } else {
                status.setMessage(Const.ALREADY_COMPLETE_OPERATION);
            }
            status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_2);

        } else {
            status.setMessage(Const.KO);
            status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_99);
        }
        r.setStatus(status);
        return r;
    }

    protected AbortTransactionResponse abortErrorResponse(Throwable ex) {
        AbortTransactionResponse r = new AbortTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        if (ex instanceof RaddTransactionNoExistedException) {
            status.setMessage(Const.NOT_EXISTS_OPERAION);
            status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_1);

        } else if (ex instanceof RaddTransactionStatusException) {
            status.setMessage(Const.ALREADY_COMPLETE_OPERATION);
            status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_2);

        } else {
            status.setMessage(Const.KO);
            status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_99);
        }
        r.setStatus(status);
        return r;
    }

}
