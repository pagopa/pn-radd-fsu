package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndatavault.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pndatavault.v1.api.RecipientsApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndatavault.v1.dto.RecipientTypeDto;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;


@Component
public class PnDataVaultClient extends BaseClient {

    private RecipientsApi recipientsApi;

    private final PnRaddFsuConfig pnRaddFsuConfig;

    public PnDataVaultClient(PnRaddFsuConfig pnRaddFsuConfig) {
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }

    @PostConstruct
    public void init(){
        ApiClient apiClient = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnRaddFsuConfig.getClientDatavaultBasepath());
        this.recipientsApi = new RecipientsApi(apiClient);
    }

    public Mono<String> getEnsureFiscalCode(String fiscalCode, String type) {
        return this.recipientsApi.ensureRecipientByExternalId(
                (StringUtils.equalsIgnoreCase(type, RecipientTypeDto.PF.getValue()) ? RecipientTypeDto.PF: RecipientTypeDto.PG), fiscalCode)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable ->throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).onErrorResume(WebClientResponseException.class, ex -> Mono.error(new PnRaddException(ex)));
    }

}
