package it.pagopa.pn.radd.middleware.msclient.config;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.ApiClient;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.api.EventComunicationApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.api.LegalFactsPrivateApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.api.PaperNotificationFailedApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.api.TimelineAndStatusApi;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeliveryPushClientConfigurator extends CommonBaseClient {
    @Bean
    public EventComunicationApi eventComunicationApi(PnRaddFsuConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getClientDeliveryPushBasepath());
        return new EventComunicationApi(apiClient);
    }

    @Bean
    public TimelineAndStatusApi timelineAndStatusApi(PnRaddFsuConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getClientDeliveryPushBasepath());
        return new TimelineAndStatusApi(apiClient);
    }

    @Bean
    public PaperNotificationFailedApi paperNotificationFailedApi(PnRaddFsuConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getClientDeliveryPushBasepath());
        return new PaperNotificationFailedApi(apiClient);
    }

    @Bean
    public LegalFactsPrivateApi legalFactsPrivateApi(PnRaddFsuConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getClientDeliveryPushBasepath());
        return new LegalFactsPrivateApi(apiClient);
    }
}
