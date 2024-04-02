package it.pagopa.pn.radd.middleware.msclient.config;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.ApiClient;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.api.NormalizeAddressServiceApi;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AddressManagerConfigurator extends CommonBaseClient {

    @Bean
    public NormalizeAddressServiceApi normalizeAddressServiceApi(PnRaddFsuConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getClientAddressManagerBasepath());
        return new NormalizeAddressServiceApi(apiClient);
    }
}
