package it.pagopa.pn.radd.middleware.msclient.config;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.ApiClient;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.api.FileDownloadApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.api.FileMetadataUpdateApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.api.FileUploadApi;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SafeStorageClientConfigurator extends CommonBaseClient {

    @Bean
    public FileUploadApi fileUploadApi(PnRaddFsuConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getClientSafeStorageBasepath());
        return new FileUploadApi(apiClient);
    }

    @Bean
    public FileDownloadApi fileDownloadApi(PnRaddFsuConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getClientSafeStorageBasepath());
        return new FileDownloadApi(apiClient);
    }

    @Bean
    public FileMetadataUpdateApi fileMetadataUpdateApi(PnRaddFsuConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getClientSafeStorageBasepath());
        return new FileMetadataUpdateApi(apiClient);
    }

}
