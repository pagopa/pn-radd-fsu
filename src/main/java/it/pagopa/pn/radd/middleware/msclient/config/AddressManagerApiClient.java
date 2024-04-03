package it.pagopa.pn.radd.middleware.msclient.config;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.ApiClient;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.api.NormalizeAddressServiceApi;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AddressManagerApiClient extends CommonBaseClient {

    @Bean
    public NormalizeAddressServiceApi normalizeAddressReactiveServiceApi(PnRaddFsuConfig cfg){
        ApiClient newApiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder()) );
        newApiClient.setBasePath( cfg.getAddressManagerBaseUrl() );
        return new NormalizeAddressServiceApi(newApiClient);
    }
}
