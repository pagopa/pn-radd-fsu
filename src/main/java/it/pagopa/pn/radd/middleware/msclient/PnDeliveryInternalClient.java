package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.internal.v1.ApiClient;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.internal.v1.api.RecipientReadApi;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.internal.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Service
public class PnDeliveryInternalClient  extends BaseClient {
    private RecipientReadApi recipientReadApi;
    private final PnRaddFsuConfig pnRaddFsuConfig;


    public PnDeliveryInternalClient(PnRaddFsuConfig pnRaddFsuConfig) {
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }

    @PostConstruct
    public void init(){
        ApiClient client = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        client.setBasePath(pnRaddFsuConfig.getClientDeliveryBasepath());
        this.recipientReadApi = new RecipientReadApi(client);
    }



    public Mono<NotificationAttachmentDownloadMetadataResponseDto> getPresignedUrlDocument(String iun, String docXid){
        return this.recipientReadApi.getReceivedNotificationDocument(null, null, null, iun, Integer.valueOf(docXid), null, null)

                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                );
    }

    public Mono<NotificationAttachmentDownloadMetadataResponseDto> getPresignedUrlPaymentDocument(String iun, String attchamentName){
        return this.recipientReadApi.getReceivedNotificationAttachment(null, null, null, iun, attchamentName, null,null)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                );
    }



}
