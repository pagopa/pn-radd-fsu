package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.api.LegalFactsPrivateApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactCategoryDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactDownloadMetadataResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.LegalFactListElementDto;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;


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


    public Flux<LegalFactListElementDto> getNotificationLegalFacts(String uid, String iun, String recipientType) {
        return this.legalFactsApi.getNotificationLegalFactsPrivate(uid, iun, "_fsu_");
    }

    public Mono<LegalFactDownloadMetadataResponseDto> getLegalFact(String uid, String iun, String recipientType, LegalFactCategoryDto categoryDto, String factId) {
        return this.legalFactsApi.getLegalFactPrivate(recipientType,iun,categoryDto, factId, null);
    }
}
