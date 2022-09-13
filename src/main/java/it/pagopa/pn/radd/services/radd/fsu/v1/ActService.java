package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.middleware.db.DocumentInquiryDao;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ActService {

    private final DocumentInquiryDao documentInquiryDao;
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;


    public ActService(DocumentInquiryDao documentInquiryDao, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient) {
        this.documentInquiryDao = documentInquiryDao;
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
    }

    public Mono<ActInquiryResponse> actInquiry(String uid, String recipientTaxId, String recipientType, String qrCode) {
        // retrieve iun
        return Mono.just(new ActInquiryResponse())
                .zipWith(pnDeliveryClient.getCheckAar(recipientType, recipientTaxId, qrCode))
                        .map(item -> {
                            ResponseCheckAarDtoDto response = item.getT2();
                            log.info("Response iun : {}", response.getIun());
                            item.getT1().setResult(Boolean.TRUE);
                            ActInquiryResponseStatus status = new ActInquiryResponseStatus();
                            status.code(ActInquiryResponseStatus.CodeEnum.NUMBER_1);
                            item.getT1().setStatus(status);
                            return item.getT1();
                        });

    }

}
