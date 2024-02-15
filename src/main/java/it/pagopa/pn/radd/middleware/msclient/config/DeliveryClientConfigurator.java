package it.pagopa.pn.radd.middleware.msclient.config;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.ApiClient;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.api.InternalOnlyApi;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeliveryClientConfigurator extends CommonBaseClient {
    @Bean
    public InternalOnlyApi deliveryApi(PnRaddFsuConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getClientDeliveryBasepath());
        return new InternalOnlyApi(apiClient);
    }
}
