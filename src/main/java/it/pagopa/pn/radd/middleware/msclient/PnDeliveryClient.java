package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.api.InternalOnlyApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.RequestCheckAarDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.SentNotificationDto;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class PnDeliveryClient extends BaseClient {
    private InternalOnlyApi deliveryApi;
    private final PnRaddFsuConfig pnRaddFsuConfig;


    public PnDeliveryClient(PnRaddFsuConfig pnRaddFsuConfig) {
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }

    @PostConstruct
    public void init(){
        ApiClient newApiClient = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        newApiClient.setBasePath(pnRaddFsuConfig.getClientDeliveryBasepath());
        this.deliveryApi = new InternalOnlyApi(newApiClient);
    }

    public Mono<ResponseCheckAarDtoDto> getCheckAar(String recipientType, String recipientInternalId, String qrCode) {
        RequestCheckAarDtoDto request = new RequestCheckAarDtoDto();
        request.setAarQrCodeValue(qrCode);
        request.setRecipientType(recipientType);
        request.setRecipientInternalId(recipientInternalId);
        return this.deliveryApi.checkAarQrCode(request)
                .retryWhen(
                Retry.backoff(2, Duration.ofMillis(25))
                        .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
        );
    }

    public Mono<SentNotificationDto> getNotifications(String iun){
        return this.deliveryApi.getSentNotificationPrivate(iun)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                );
    }


}