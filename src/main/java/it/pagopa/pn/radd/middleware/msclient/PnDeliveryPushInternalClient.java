package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.api.LegalFactsApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.CxTypeAuthFleetDto;
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

    private LegalFactsApi legalFactsApi;
    private final PnRaddFsuConfig pnRaddFsuConfig;

    public PnDeliveryPushInternalClient(PnRaddFsuConfig pnRaddFsuConfig) {
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }

    @PostConstruct
    public void init(){
        ApiClient newApiClient = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        newApiClient.setBasePath(pnRaddFsuConfig.getClientDeliveryPushInternalBasepath());
        this.legalFactsApi = new LegalFactsApi(newApiClient);
    }


    public Flux<LegalFactListElementDto> getNotificationLegalFacts(String uid, String iun, String recipientType) {
        return this.legalFactsApi.getNotificationLegalFacts(
                uid, CxTypeAuthFleetDto.valueOf(recipientType), "_fsu_",
                iun,
                null, null);
    }

    public Mono<LegalFactDownloadMetadataResponseDto> getLegalFact(
            String uid, String iun, String recipientType, LegalFactCategoryDto categoryDto, String factId
    ) {
        return this.legalFactsApi.getLegalFact(
                uid,
                CxTypeAuthFleetDto.valueOf(recipientType),
                "_fus_",
                iun,  categoryDto, factId,
                null, null
        );
    }
}
