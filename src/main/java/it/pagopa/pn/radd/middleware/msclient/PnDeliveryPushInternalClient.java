package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.api.LegalFactsPrivateApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactCategoryDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactDownloadMetadataResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactListElementDto;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;


@Component
public class PnDeliveryPushInternalClient extends BaseClient {

    private LegalFactsPrivateApi legalFactsApi;
    private final PnRaddFsuConfig pnRaddFsuConfig;

    public PnDeliveryPushInternalClient(PnRaddFsuConfig pnRaddFsuConfig) {
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }

    @PostConstruct
    public void init(){
        ApiClient newApiClient = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        newApiClient.setBasePath(pnRaddFsuConfig.getClientDeliveryPushInternalBasepath());
        this.legalFactsApi = new LegalFactsPrivateApi(newApiClient);
    }


    public Flux<LegalFactListElementDto> getNotificationLegalFacts(String recipientInternalId, String iun) {
        return this.legalFactsApi.getNotificationLegalFactsPrivate(recipientInternalId, iun, null)
                .retryWhen(
                    Retry.backoff(2, Duration.ofMillis(250))
                        .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).onErrorResume(WebClientResponseException.class, ex -> Mono.error(new PnRaddException(ex)));
    }

    public Mono<LegalFactDownloadMetadataResponseDto> getLegalFact(String recipientInternalId, String iun, LegalFactCategoryDto categoryDto, String legalFactId) {
        return this.legalFactsApi.getLegalFactPrivate(recipientInternalId,iun, categoryDto, legalFactId, null)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(250))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).onErrorResume(WebClientResponseException.class, ex -> Mono.error(new PnRaddException(ex)));
    }
}
