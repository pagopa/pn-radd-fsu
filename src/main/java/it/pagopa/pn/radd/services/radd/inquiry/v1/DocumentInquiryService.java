package it.pagopa.pn.radd.services.radd.inquiry.v1;

import it.pagopa.pn.radd.middleware.db.DocumentInquiryDao;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DocumentInquiryService {

    private final DocumentInquiryDao documentInquiryDao;
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;


    public DocumentInquiryService(DocumentInquiryDao documentInquiryDao, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient) {
        this.documentInquiryDao = documentInquiryDao;
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
    }

    public Mono<ActInquiryResponse> actInquiry(String uid, String recipientTaxId, String recipientType, String qrCode) {
        // retrieve iuu
        return Mono.just(new ActInquiryResponse())
                .zipWith(pnDeliveryClient.getCheckAar(recipientType, recipientTaxId, qrCode))
                        .map(item -> {
                            item.getT1().setResult(Boolean.FALSE);
                            return item.getT1();
                        });
        /*
        RaddTransactionEntity entitySave = new RaddTransactionEntity();
        //TODO add settings
        //TODO save entity into Dynamo DB transaction;
        documentInquiryDao.createRaddTransaction(entitySave);

        // send notification viewed
        pnDeliveryPushClient.notifyNotificationViewed("iun", recipientType, recipientTaxId);
        */

    }

}
