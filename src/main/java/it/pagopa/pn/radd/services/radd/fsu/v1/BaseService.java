package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.RaddFiscalCodeEnsureException;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BaseService {

    public BaseService() {
        // do nothing
    }

    protected Mono<String> getEnsureFiscalCode(String fiscalCode, PnDataVaultClient pnDataVaultClient){
        return pnDataVaultClient.getEnsureFiscalCode(fiscalCode)
                .map(response -> {
                    if (response == null || Strings.isBlank(response)){
                        throw new RaddFiscalCodeEnsureException();
                    }
                    return response;
                }).onErrorResume(Mono::error);
    }

}
