package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.PnCheckQrCodeException;
import it.pagopa.pn.radd.exception.PnDocumentException;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.api.FileDownloadApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.api.FileUploadApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileCreationRequestDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileDownloadResponseDto;
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
    }

    public Mono<FileCreationResponseDto> createFile(String contentType, String operationId){
        log.info(String.format("Req params: %s %s", contentType, operationId));
        FileCreationRequestDto request = new FileCreationRequestDto();
        request.setStatus(Const.PRELOADED);
        request.setContentType(contentType);
        return this.fileUploadApi.createFile(operationId, request)
                .retryWhen(
                Retry.backoff(2, Duration.ofMillis(25))
                        .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
        ).onErrorResume(WebClientResponseException.class, ex -> Mono.error(new PnDocumentException(ex)));
    }

    public Mono<FileDownloadResponseDto> getFile(String fileKey){
        log.info("Req params : {}", fileKey);
        return fileDownloadApi.getFile(fileKey, "pn-radd-fsu", true)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                );
    }


}