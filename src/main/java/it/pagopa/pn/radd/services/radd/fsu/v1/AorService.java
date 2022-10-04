package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.AORInquiryResponseMapper;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.AORInquiryResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
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
        if (StringUtils.isBlank(recipientTaxId)){
            throw new PnInvalidInputException("Il campo codice fiscale non è valorizzato");
        }
        return this.pnDeliveryPushClient.getPaperNotificationFailed(recipientTaxId).collectList()
                .map(listNotification -> {

                    if (listNotification == null){
                        throw new RaddGenericException(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED, ExceptionCodeEnum.KO);
                    }

                    List< ResponsePaperNotificationFailedDtoDto> filter = listNotification
                            .stream()
                            .filter(item -> StringUtils.equals(item.getRecipientInternalId(), recipientTaxId))
                            .collect(Collectors.toList());
                    if (filter.isEmpty()) {
                        throw new RaddGenericException(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILDE_FOR_CF, ExceptionCodeEnum.KO);
                    }
                    return AORInquiryResponseMapper.fromResult();
                }).onErrorResume(RaddGenericException.class, ex -> Mono.just(AORInquiryResponseMapper.fromException(ex)));
    }






}
