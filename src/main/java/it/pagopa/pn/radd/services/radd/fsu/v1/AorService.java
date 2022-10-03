package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.RaddAorInquiryException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.AORInquiryResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.ResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AorService extends BaseService {

    private final PnDeliveryPushClient pnDeliveryPushClient;

    public AorService(PnDeliveryPushClient pnDeliveryPushClient) {
        this.pnDeliveryPushClient = pnDeliveryPushClient;
    }

    public Mono<AORInquiryResponse> aorInquiry(String uid, String recipientTaxId,
                                               String recipientType){
        return this.pnDeliveryPushClient.getPaperNotificationFailed(recipientTaxId).collectList()
                .map(listNotification -> {

                    if (listNotification == null){
                        throw new RaddAorInquiryException("IUN", "Non ci sono notifiche non consegnate");
                    }

                    List< ResponsePaperNotificationFailedDtoDto> filter = listNotification
                            .stream()
                            .filter(item -> StringUtils.equals(item.getRecipientInternalId(), recipientTaxId))
                            .collect(Collectors.toList());
                    if (filter.isEmpty()) {
                        throw new RaddAorInquiryException("IUN", "Non ci sono notifiche non consegnate per questo codice fiscale");
                    }
                    return buildInquiryResponse(null);
                }).onErrorResume(ex -> Mono.just(buildInquiryResponse(ex)));

    }

    private AORInquiryResponse buildInquiryResponse(Throwable error){
        AORInquiryResponse response = new AORInquiryResponse();
        ResponseStatus status = new ResponseStatus();
        response.setResult(true);
        response.setStatus(status);

        if (error != null){
            log.error("Error : {}", error.getMessage());
            status.setCode(ResponseStatus.CodeEnum.NUMBER_99);
            response.setResult(false);
            if (error instanceof RaddAorInquiryException){
                status.setMessage(((RaddAorInquiryException) error).getDescription());
            } else if (error instanceof WebClientResponseException){
                //throw new exception
            } else {
                status.setMessage("Errore generico");
            }
            return response;
        }
        status.setCode(ResponseStatus.CodeEnum.NUMBER_0);
        return response;
    }





}
