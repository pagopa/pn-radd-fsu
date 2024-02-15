package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndatavault.v1.api.RecipientsApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndatavault.v1.dto.RecipientTypeDto;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@Slf4j
@AllArgsConstructor
@Component
public class PnDataVaultClient extends BaseClient {

    private final RecipientsApi recipientsApi;

    private final PnRaddFsuConfig pnRaddFsuConfig;


    public Mono<String> getEnsureFiscalCode(String fiscalCode, String type) {
        log.trace("ENSURE FISCAL CODE TICK {}", new Date().getTime());
        return this.recipientsApi.ensureRecipientByExternalId(
                (StringUtils.equalsIgnoreCase(type, RecipientTypeDto.PF.getValue()) ? RecipientTypeDto.PF: RecipientTypeDto.PG), fiscalCode)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable ->throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).map(item -> {
                    log.trace("ENSURE FISCAL CODE TOCK {}", new Date().getTime());
                    return item;
                })
                .doOnError(ex -> log.error("Error in getEnsureFiscalCode", ex))
                .onErrorResume(WebClientResponseException.class, ex -> Mono.error(new PnRaddException(ex)));
    }

}
