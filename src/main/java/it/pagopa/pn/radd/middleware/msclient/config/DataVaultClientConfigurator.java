package it.pagopa.pn.radd.middleware.msclient.config;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndatavault.v1.ApiClient;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndatavault.v1.api.RecipientsApi;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataVaultClientConfigurator extends CommonBaseClient {
    @Bean
    public RecipientsApi recipientsApi(PnRaddFsuConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getClientDatavaultBasepath());
        return new RecipientsApi(apiClient);
    }
}
