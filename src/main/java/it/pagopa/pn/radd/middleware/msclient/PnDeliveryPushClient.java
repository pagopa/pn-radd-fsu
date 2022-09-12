package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.api.EventComunicationApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.dto.RequestNotificationViewedDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Component
public class PnDeliveryPushClient extends BaseClient {

    private EventComunicationApi eventComunicationApi;
    private final PnRaddFsuConfig pnRaddFsuConfig;

    public PnDeliveryPushClient(PnRaddFsuConfig pnRaddFsuConfig) {
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }

    @PostConstruct
    public void init(){
        ApiClient newApiClient = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        newApiClient.setBasePath(pnRaddFsuConfig.getClientDeliveryPushBasepath());
        this.eventComunicationApi = new EventComunicationApi(newApiClient);
    }


    public Mono<ResponseNotificationViewedDtoDto> notifyNotificationViewed(String iun, String recipientType, String recipientInternalId){
        RequestNotificationViewedDtoDto request = new RequestNotificationViewedDtoDto();
        request.setIun(iun);
        request.setRecipientType(recipientType);
        request.setRecipientInternalId(recipientInternalId);
        return this.eventComunicationApi.notifyNotificationViewed(iun, request);
    }

}
