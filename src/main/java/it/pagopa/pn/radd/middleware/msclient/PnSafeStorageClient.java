package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.PnDocumentException;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.api.FileDownloadApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.api.FileMetadataUpdateApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.api.FileUploadApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.*;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import it.pagopa.pn.radd.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class PnSafeStorageClient extends BaseClient {
    private FileUploadApi fileUploadApi;
    private FileDownloadApi fileDownloadApi;
    private FileMetadataUpdateApi fileMetadataUpdateApi;
    private final PnRaddFsuConfig pnRaddFsuConfig;

    public PnSafeStorageClient(PnRaddFsuConfig pnRaddFsuConfig) {
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }

    @PostConstruct
    public void init(){
        ApiClient newApiClient = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        newApiClient.setBasePath(pnRaddFsuConfig.getClientSafeStorageBasepath());
        this.fileUploadApi = new FileUploadApi(newApiClient);
        this.fileDownloadApi = new FileDownloadApi(newApiClient);
        this.fileMetadataUpdateApi = new FileMetadataUpdateApi(newApiClient);
    }

    public Mono<FileCreationResponseDto> createFile(String contentType, String bundleId){
        log.info(String.format("Req params: %s %s", contentType, bundleId));
        log.info(String.format("URL %s ", this.pnRaddFsuConfig.getClientSafeStorageBasepath()));
        log.info(String.format("storage id %s ", this.pnRaddFsuConfig.getSafeStorageCxId()));
        FileCreationRequestDto request = new FileCreationRequestDto();
        request.setStatus(Const.PRELOADED);
        request.setContentType(contentType);
        request.setDocumentType(Const.DOCUMENT_TYPE);
        return this.fileUploadApi.createFile(this.pnRaddFsuConfig.getSafeStorageCxId(), request)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).onErrorResume(WebClientResponseException.class, ex -> Mono.error(new PnDocumentException(ex)));
    }

    public Mono<FileDownloadResponseDto> getFile(String fileKey){
        log.info("Req params : {}", fileKey);
        return fileDownloadApi.getFile(fileKey, this.pnRaddFsuConfig.getSafeStorageCxId(), true)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                );
    }

    public Mono<OperationResultCodeResponseDto> updateFileMetadata(String fileKey){
        log.info("Req params : {}", fileKey);
        UpdateFileMetadataRequestDto request = new UpdateFileMetadataRequestDto();
        request.setStatus(Const.ATTACHED);
        return fileMetadataUpdateApi.updateFileMetadata(fileKey, this.pnRaddFsuConfig.getSafeStorageCxId(), request)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                );
    }


}