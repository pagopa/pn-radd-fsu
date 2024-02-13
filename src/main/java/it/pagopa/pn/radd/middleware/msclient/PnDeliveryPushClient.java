package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.PaperNotificationFailedEmptyException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.api.EventComunicationApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.api.LegalFactsPrivateApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.api.PaperNotificationFailedApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.api.TimelineAndStatusApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.dto.*;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import it.pagopa.pn.radd.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class PnDeliveryPushClient extends BaseClient {
    private static final String RADD_TYPE = "ALT";
    private EventComunicationApi eventComunicationApi;
    private TimelineAndStatusApi timelineAndStatusApi;
    private PaperNotificationFailedApi paperNotificationFailedApi;
    private LegalFactsPrivateApi legalFactsApi;

    private final PnRaddFsuConfig pnRaddFsuConfig;

    public PnDeliveryPushClient(PnRaddFsuConfig pnRaddFsuConfig) {
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }

    @PostConstruct
    public void init(){
        ApiClient newApiClient = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        newApiClient.setBasePath(pnRaddFsuConfig.getClientDeliveryPushBasepath());
        this.eventComunicationApi = new EventComunicationApi(newApiClient);
        this.timelineAndStatusApi = new TimelineAndStatusApi(newApiClient);
        this.paperNotificationFailedApi = new PaperNotificationFailedApi(newApiClient);
        this.legalFactsApi = new LegalFactsPrivateApi(newApiClient);
    }

    public Flux<LegalFactListElementDto> getNotificationLegalFacts(String recipientInternalId, String iun) {
        CxTypeAuthFleetDto cxType = null;
        return this.legalFactsApi.getNotificationLegalFactsPrivate( recipientInternalId, iun, null, cxType, null)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(250))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
                .onErrorResume(WebClientResponseException.class, ex -> Mono.error(new PnRaddException(ex)));
    }

    public Mono<LegalFactDownloadMetadataWithContentTypeResponseDto> getLegalFact(String recipientInternalId, String iun, LegalFactCategoryDto categoryDto, String legalFactId) {
        log.trace("GET LEGAL FACT TICK {}", new Date().getTime());
        CxTypeAuthFleetDto cxType = null;
        return this.legalFactsApi.getLegalFactPrivate(recipientInternalId,iun, categoryDto, legalFactId, null, cxType, null)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(250))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).map(item ->{
                    log.trace("GET LEGAL FACT TOCK {}", new Date().getTime());
                    return item;
                }).onErrorResume(WebClientResponseException.class, ex -> Mono.error(new PnRaddException(ex)));
    }


    public Mono<NotificationHistoryResponseDto> getNotificationHistory(String iun){
        log.debug("IUN : {}", iun);
        log.trace("NOTIFICATION HISTORY TICK {}", new Date().getTime());
        return this.timelineAndStatusApi.getNotificationHistory(iun, 1, DateUtils.getOffsetDateTimeFromDate(new Date()))
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).map(item -> {
                    log.trace("NOTIFICATION HISTORY TOCK {}", new Date().getTime());
                    return item;
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.trace("NOTIFICATION HISTORY TOCK {}", new Date().getTime());
                    ex.getStackTrace();
                    log.error(ex.getResponseBodyAsString());
                    return Mono.error(new PnRaddException(ex));
                });
    }

    public Mono<ResponseNotificationViewedDtoDto> notifyNotificationRaddRetrieved(RaddTransactionEntity entity, Date operationDate){
        RequestNotificationViewedDtoDto request = new RequestNotificationViewedDtoDto();
        request.setRecipientType(RecipientTypeDto.fromValue(entity.getRecipientType()));
        request.setRecipientInternalId(entity.getRecipientId());
        request.setRaddBusinessTransactionDate(DateUtils.getOffsetDateTimeFromDate(operationDate));
        request.setRaddBusinessTransactionId(entity.getOperationId());
        request.setRaddType(RADD_TYPE);
        log.trace("NOTIFICATION VIEWED TICK {}", new Date().getTime());
        return this.eventComunicationApi.notifyNotificationRaddRetrieved(entity.getIun(), request)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).map(item -> {
                    log.debug("response of notification viewed : {}", item.getIun());
                    log.trace("NOTIFICATION VIEWED TOCK {}", new Date().getTime());
                    return item;
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.trace("NOTIFICATION VIEWED TOCK {}", new Date().getTime());
                    log.error("Notification viewed in error");
                    log.error(ex.getResponseBodyAsString());
                    return Mono.error(new PnRaddException(ex));
                });
    }


    public Flux<ResponsePaperNotificationFailedDtoDto> getPaperNotificationFailed(String recipientInternalId){
        return this.paperNotificationFailedApi.paperNotificationFailed(recipientInternalId, true)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND){
                        return Mono.error(new PaperNotificationFailedEmptyException());
                    }
                    return Mono.error(new PnRaddException(ex));
                });
    }

}
