package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.api.EventComunicationApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.api.PaperNotificationFailedApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.RecipientTypeDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.RequestNotificationViewedDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponsePaperNotificationFailedDtoDto;
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
import java.util.concurrent.TimeoutException;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF;

@Slf4j
@Component
public class PnDeliveryPushClient extends BaseClient {
    private static final String RADD_TYPE = "__FSU__";
    private EventComunicationApi eventComunicationApi;
    private PaperNotificationFailedApi paperNotificationFailedApi;
    private final PnRaddFsuConfig pnRaddFsuConfig;

    public PnDeliveryPushClient(PnRaddFsuConfig pnRaddFsuConfig) {
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }

    @PostConstruct
    public void init(){
        ApiClient newApiClient = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        newApiClient.setBasePath(pnRaddFsuConfig.getClientDeliveryPushBasepath());
        this.eventComunicationApi = new EventComunicationApi(newApiClient);
        this.paperNotificationFailedApi = new PaperNotificationFailedApi(newApiClient);
    }


    public Mono<ResponseNotificationViewedDtoDto> notifyNotificationViewed(RaddTransactionEntity entity){
        RequestNotificationViewedDtoDto request = new RequestNotificationViewedDtoDto();
        request.setRecipientType(RecipientTypeDto.fromValue(entity.getRecipientType()));
        request.setRecipientInternalId(entity.getRecipientId());
        request.setRaddBusinessTransactionDate(DateUtils.getOffsetDateTime(entity.getOperationStartDate()));
        request.setRaddBusinessTransactionId(entity.getOperationId());
        request.setRaddType(RADD_TYPE);
        return this.eventComunicationApi.notifyNotificationViewed(entity.getIuns().get(0), request)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
                .onErrorResume(WebClientResponseException.class, ex -> Mono.error(new PnRaddException(ex)));
    }


    public Flux<ResponsePaperNotificationFailedDtoDto> getPaperNotificationFailed(String recipientInternalId){
        return this.paperNotificationFailedApi.paperNotificationFailed(recipientInternalId, true)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                ).onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND){
                        return Mono.error(new RaddGenericException(NO_NOTIFICATIONS_FAILED_FOR_CF));
                    }
                    return Mono.error(new PnRaddException(ex));
                });
    }

}
