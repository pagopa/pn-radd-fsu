package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ActService {

    private final RaddTransactionDAO raddTransactionDAO;
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;


    public ActService( RaddTransactionDAO raddTransactionDAO, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient) {
        this.raddTransactionDAO = raddTransactionDAO;
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

    private boolean validate(String recipientTaxId, String recipientType) {
        // valida delegato

        /*if (mandateDto.getDelegate().getPerson()
                && !mandateDto.getDelegate().getFiscalCode().matches("[A-Za-z]{6}[0-9]{2}[A-Za-z]{1}[0-9]{2}[A-Za-z]{1}[0-9]{3}[A-Za-z]{1}"))
            throw new PnInvalidInputException();
        if (!mandateDto.getDelegate().getPerson()
                && !mandateDto.getDelegate().getFiscalCode().matches("[0-9]{11}"))
            throw new InvalidInputException();

        // la delega richiede la data di fine
        if (!StringUtils.hasText(mandateDto.getDateto()))
            throw new InvalidInputException();

        return mandateDto;*/
        return true;
    }

}
