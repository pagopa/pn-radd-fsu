package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.RaddFiscalCodeEnsureException;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
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

}
