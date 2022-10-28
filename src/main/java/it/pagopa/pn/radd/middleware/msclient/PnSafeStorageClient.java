package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.PnSafeStorageException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.api.FileDownloadApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.api.FileMetadataUpdateApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.api.FileUploadApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.*;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import it.pagopa.pn.radd.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DOCUMENT_UPLOAD_ERROR;
import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.RETRY_AFTER;

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

    public Mono<FileCreationResponseDto> createFile(String contentType, String bundleId, String checksum){
        log.debug(String.format("Req params: %s %s", contentType, bundleId));
        log.debug(String.format("URL %s ", this.pnRaddFsuConfig.getClientSafeStorageBasepath()));
        log.debug(String.format("storage id %s ", this.pnRaddFsuConfig.getSafeStorageCxId()));
        FileCreationRequestDto request = new FileCreationRequestDto();
        request.setStatus(Const.PRELOADED);
        request.setContentType(contentType);
        request.setDocumentType(this.pnRaddFsuConfig.getSafeStorageDocType());
        return this.fileUploadApi.createFile(this.pnRaddFsuConfig.getSafeStorageCxId(), Const.X_CHECKSUM, checksum, request)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).onErrorResume(WebClientResponseException.class, ex -> Mono.error(new RaddGenericException(DOCUMENT_UPLOAD_ERROR)));
    }

    public Mono<FileDownloadResponseDto> getFile(String fileKey){
        log.info("Req params : {}", fileKey);
        log.info("GET FILE TICK {}", new Date().getTime());
        return fileDownloadApi.getFile(fileKey, this.pnRaddFsuConfig.getSafeStorageCxId(), true)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).map(item -> {
                        log.info("GET FILE TOCK {}", new Date().getTime());
                        return item;
                }).onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND){
                        return Mono.error(new RaddGenericException(RETRY_AFTER, new BigDecimal(670)));
                    }
                    return Mono.error(new PnSafeStorageException(ex));
                });
    }

    public Mono<OperationResultCodeResponseDto> updateFileMetadata(String fileKey){
        log.info("Req params : {}", fileKey);
        log.info("UPDATE FILE METADATA TICK {}", new Date().getTime());

        UpdateFileMetadataRequestDto request = new UpdateFileMetadataRequestDto();
        request.setStatus(Const.ATTACHED);
        return fileMetadataUpdateApi.updateFileMetadata(fileKey, this.pnRaddFsuConfig.getSafeStorageCxId(), request)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).map(item -> {
                    log.info("UPDATE FILE METADATA TOCK {}", new Date().getTime());
                    return item;
                }).onErrorResume(WebClientResponseException.class, ex -> Mono.error(new PnSafeStorageException(ex)));
    }


}