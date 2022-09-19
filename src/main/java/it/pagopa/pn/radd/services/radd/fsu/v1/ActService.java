package it.pagopa.pn.radd.services.radd.fsu.v1;

import io.netty.handler.codec.http.HttpResponseStatus;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponseStatus;
import it.pagopa.pn.radd.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ActService extends BaseService {

    private final RaddTransactionDAO raddTransactionDAO;
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;
    private final PnDataVaultClient pnDataVaultClient;

    public ActService(RaddTransactionDAO raddTransactionDAO, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient, PnDataVaultClient pnDataVaultClient) {
        this.raddTransactionDAO = raddTransactionDAO;
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.pnDataVaultClient = pnDataVaultClient;
    }

    public Mono<ActInquiryResponse> actInquiry(String uid, String recipientTaxId, String recipientType, String qrCode) {
        // retrieve iun

        return Mono.just(new ActInquiryResponse())
                .zipWhen(tmp -> getEnsureRecipientAndDelegate(recipientTaxId))
                .zipWhen(r -> pnDeliveryClient.getCheckAar(recipientType, r.getT2(), qrCode))
                .map(item -> {
                    ResponseCheckAarDtoDto response = item.getT2();
                    log.info("Response iun : {}", response.getIun());
                    ActInquiryResponse actInquiryResponse = item.getT1().getT1();
                    actInquiryResponse.setResult(true);
                    ActInquiryResponseStatus status = new ActInquiryResponseStatus();
                    status.setMessage(Const.OK);
                    status.code(ActInquiryResponseStatus.CodeEnum.NUMBER_0);
                    actInquiryResponse.setStatus(status);
                    return item.getT1().getT1();
                }).onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(addErrorStatus(ex));
                });
    }


    private Mono<String> getEnsureRecipientAndDelegate(String recipientTaxId){
        return getEnsureFiscalCode(recipientTaxId, this.pnDataVaultClient);
    }

    private ActInquiryResponse addErrorStatus(WebClientResponseException ex){
        ActInquiryResponse r = new ActInquiryResponse();
        r.setResult(false);
        ActInquiryResponseStatus status = new ActInquiryResponseStatus();
        status.setMessage(Const.KO);
        if (ex.getRawStatusCode() == HttpResponseStatus.NOT_FOUND.code()) {
            status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_1);
        } else if (ex.getRawStatusCode() == HttpResponseStatus.FORBIDDEN.code()) {
            status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_2);
        } else if (ex.getRawStatusCode() == HttpResponseStatus.CONFLICT.code()) {
            status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_3);
        } else {
            status.setCode(ActInquiryResponseStatus.CodeEnum.NUMBER_99);
        }
        r.setStatus(status);
        return r;
    }

}
