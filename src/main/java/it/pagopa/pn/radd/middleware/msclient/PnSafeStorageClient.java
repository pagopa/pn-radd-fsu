package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.api.FileDownloadApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.api.FileMetadataUpdateApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.api.FileUploadApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.*;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.PnSafeStorageException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import it.pagopa.pn.radd.utils.Const;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DOCUMENT_UPLOAD_ERROR;

@CustomLog
@Component
@RequiredArgsConstructor
public class PnSafeStorageClient extends BaseClient {

    private final FileUploadApi fileUploadApi;
    private final FileDownloadApi fileDownloadApi;
    private final FileMetadataUpdateApi fileMetadataUpdateApi;
    private final PnRaddFsuConfig pnRaddFsuConfig;


    public Mono<FileCreationResponseDto> createFile(FileCreationRequestDto fileCreationRequestDto, String checksum) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_SAFE_STORAGE, "createFile");

        log.debug(String.format("Req params: %s", fileCreationRequestDto.getContentType()));
        log.debug(String.format("URL %s ", this.pnRaddFsuConfig.getClientSafeStorageBasepath()));
        log.debug(String.format("storage id %s ", this.pnRaddFsuConfig.getSafeStorageCxId()));
        log.trace("CREATE FILE TICK {}", new Date().getTime());
        return this.fileUploadApi.createFile(this.pnRaddFsuConfig.getSafeStorageCxId(), Const.X_CHECKSUM, checksum, fileCreationRequestDto)
                .map(item -> {
                    log.trace("CREATE FILE TOCK {}", new Date().getTime());
                    return item;
                })
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).onErrorResume(WebClientResponseException.class, ex -> {
                    log.trace("CREATE FILE TOCK {}", new Date().getTime());
                    log.error(ex.getResponseBodyAsString());
                    return Mono.error(new RaddGenericException(DOCUMENT_UPLOAD_ERROR));
                });
    }

    public Mono<FileDownloadResponseDto> getFile(String fileKey) {
        log.debug("Req params : {}", fileKey);
        log.trace("GET FILE TICK {}", new Date().getTime());
        return fileDownloadApi.getFile(fileKey, this.pnRaddFsuConfig.getSafeStorageCxId(), false)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).map(item -> {
                    log.trace("GET FILE TOCK {}", new Date().getTime());
                    return item;
                }).onErrorResume(WebClientResponseException.class, ex -> {
                    log.trace("GET FILE TOCK {}", new Date().getTime());
                    log.error(ex.getResponseBodyAsString());
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new RaddGenericException(ExceptionTypeEnum.DOCUMENT_UNAVAILABLE));
                    }
                    return Mono.error(new PnSafeStorageException(ex));
                });
    }

    public Mono<OperationResultCodeResponseDto> updateFileMetadata(String fileKey) {
        log.debug("Req params : {}", fileKey);
        log.trace("UPDATE FILE METADATA TICK {}", new Date().getTime());

        it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.UpdateFileMetadataRequestDto request = new UpdateFileMetadataRequestDto();
        request.setStatus(Const.ATTACHED);
        return fileMetadataUpdateApi.updateFileMetadata(fileKey, this.pnRaddFsuConfig.getSafeStorageCxId(), request)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).map(item -> {
                    log.trace("UPDATE FILE METADATA TOCK {}", new Date().getTime());
                    return item;
                }).onErrorResume(WebClientResponseException.class, ex -> {
                    log.trace("UPDATE FILE METADATA TOCK {}", new Date().getTime());
                    log.error(ex.getResponseBodyAsString());
                    return Mono.error(new PnSafeStorageException(ex));
                });
    }


}