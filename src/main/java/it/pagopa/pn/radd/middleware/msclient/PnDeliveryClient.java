package it.pagopa.pn.radd.middleware.msclient;

import io.netty.handler.codec.http.HttpResponseStatus;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.api.InternalOnlyApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.RequestCheckAarDtoDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV23Dto;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
@AllArgsConstructor
public class PnDeliveryClient extends BaseClient {
    private final InternalOnlyApi deliveryApi;
    private final PnRaddFsuConfig pnRaddFsuConfig;


    public Mono<ResponseCheckAarDtoDto> getCheckAar(String recipientType, String recipientInternalId, String qrCode) {
        RequestCheckAarDtoDto request = new RequestCheckAarDtoDto();
        request.setAarQrCodeValue(qrCode);
        if (qrCode.contains("aar=")) {
            request.setAarQrCodeValue(qrCode.substring(qrCode.lastIndexOf("aar=") + 4));
        }
        request.setRecipientType(recipientType);
        request.setRecipientInternalId(recipientInternalId);
        log.trace("CHECK AAR QRCODE TICK {}", new Date().getTime());
        return this.deliveryApi.checkAarQrCode(request)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(25))
                        .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).map(item -> {
                    log.trace("AAR TOCK : {}", new Date().getTime());
                    return item;
                }).onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Error : {}", ex.getResponseBodyAsString());
                    ExceptionTypeEnum message;
                    if (ex.getRawStatusCode() == HttpResponseStatus.NOT_FOUND.code() ||
                            ex.getRawStatusCode() == HttpResponseStatus.BAD_REQUEST.code()) {
                        message = ExceptionTypeEnum.INVALID_INPUT;
                    } else if (ex.getRawStatusCode() == HttpResponseStatus.FORBIDDEN.code()) {
                        message = ExceptionTypeEnum.DOCUMENT_NOT_FOUND;
                    } else if (ex.getRawStatusCode() == HttpResponseStatus.CONFLICT.code()) {
                        message = ExceptionTypeEnum.ALREADY_COMPLETE_PRINT;
                    } else {
                        return Mono.error(new PnRaddException(ex));
                    }
                    return Mono.error(new RaddGenericException(message));
                });
    }

    public Mono<SentNotificationV23Dto> getNotifications(String iun) {
        log.trace("GET NOTIFICATIONS TICK {}", new Date().getTime());
        return this.deliveryApi.getSentNotificationPrivate(iun)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
                .map(item -> {
                    log.trace("GET NOTIFICATIONS TOCK : {}", new Date().getTime());
                    return item;
                })
                .onErrorResume(WebClientResponseException.class, ex -> Mono.error(new PnRaddException(ex)));
    }


    public Mono<NotificationAttachmentDownloadMetadataResponseDto> getPresignedUrlDocument(String iun, String docXid, String recipientTaxId) {
        log.trace("SINGLE PRESIGNED DOCUMENT TICK {}", new Date().getTime());
        return this.deliveryApi.getReceivedNotificationDocumentPrivate(iun, Integer.valueOf(docXid), recipientTaxId, null)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).map(item -> {
                    log.trace("SINGLE PRESIGNED DOCUMENT URL TOCK : {}", new Date().getTime());
                    return item;
                }).onErrorResume(WebClientResponseException.class, ex -> {
                    ExceptionTypeEnum message = ExceptionTypeEnum.DOCUMENT_UNAVAILABLE;
                    if (ex.getRawStatusCode() == HttpResponseStatus.GONE.code()) {
                        return Mono.error(new RaddGenericException(message));
                    }
                    return Mono.error(new PnRaddException(ex));
                });
    }

    public Mono<NotificationAttachmentDownloadMetadataResponseDto> getPresignedUrlPaymentDocument(String iun, String attachmentName, String recipientTaxId, Integer attachmentIdx) {
        log.trace("SINGLE PRESIGNED ATTACHEMENT TICK {}", new Date().getTime());
        return this.deliveryApi.getReceivedNotificationAttachmentPrivate(iun, attachmentName, recipientTaxId, null, attachmentIdx)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).map(item -> {
                    log.trace("SINGLE PRESIGNED ATTACHEMENT TOCK {}", new Date().getTime());
                    return item;
                }).onErrorResume(WebClientResponseException.class, ex -> {
                    ExceptionTypeEnum message = ExceptionTypeEnum.DOCUMENT_UNAVAILABLE;
                    if (ex.getRawStatusCode() == HttpResponseStatus.GONE.code()) {
                        return Mono.error(new RaddGenericException(message));
                    }
                    return Mono.error(new PnRaddException(ex));
                });
    }

    public Mono<Void> checkIunAndInternalId(String iun, String recipientInternalId) {
        log.info("checkIunAndInternalId");
        return this.deliveryApi.checkIUNAndInternalId(iun, recipientInternalId, null, null, null)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).doOnSuccess(item -> log.trace("CHECK IUN AND INTERNAL ID TOCK {}", new Date().getTime())
                ).onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Error : {}", ex.getResponseBodyAsString());
                    ExceptionTypeEnum message;
                    if (ex.getRawStatusCode() == HttpResponseStatus.NOT_FOUND.code()
                            || ex.getRawStatusCode() == HttpResponseStatus.FORBIDDEN.code()) {
                        message = ExceptionTypeEnum.INVALID_INPUT;
                    } else {
                        return Mono.error(new PnRaddException(ex));
                    }
                    return Mono.error(new RaddGenericException(message));
                });
    }
}
