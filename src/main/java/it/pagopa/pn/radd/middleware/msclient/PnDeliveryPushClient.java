package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.api.EventComunicationApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.dto.RequestNotificationViewedDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Component
public class PnDeliveryPushClient extends BaseClient {

    //TODO add into application properties
    private static final String raddType = "__FSU__";
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


    public Mono<ResponseNotificationViewedDtoDto> notifyNotificationViewed(RaddTransactionEntity entity){
        RequestNotificationViewedDtoDto request = new RequestNotificationViewedDtoDto();
        request.setIun(entity.getIun());
        request.setRecipientType(entity.getRecipientType());
        request.setRecipientInternalId(entity.getRecipientId());
        //request.setRaddBusinessTransactionDate(entity.getOperationStartDate());
        request.setRaddBusinessTransactionId(entity.getOperationId());
        request.setRaddType(raddType);
        return this.eventComunicationApi.notifyNotificationViewed(entity.getIun(), request);
    }

}
