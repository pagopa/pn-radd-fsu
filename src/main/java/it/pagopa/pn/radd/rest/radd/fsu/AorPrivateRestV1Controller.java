package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.rest.radd.v1.api.AorDocumentInquiryApi;
import it.pagopa.pn.radd.rest.radd.v1.api.AorTransactionManagementApi;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.services.radd.fsu.v1.AorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Duration;


@RestController
public class AorPrivateRestV1Controller implements AorDocumentInquiryApi, AorTransactionManagementApi {

    private final SecureRandom rnd = new SecureRandom();

    private final PnDeliveryClient pnDeliveryClient;
    private final AorService aorService;

    public AorPrivateRestV1Controller(PnDeliveryClient pnDeliveryClient, AorService aorService) {
        this.pnDeliveryClient = pnDeliveryClient;
        this.aorService = aorService;
    }


    @Override
    public Mono<ResponseEntity<AORInquiryResponse>> aorInquiry(String uid, String recipientTaxId,
                                                               String recipientType, ServerWebExchange exchange) {
        return aorService.aorInquiry(uid, recipientTaxId, recipientType).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<AbortTransactionResponse>> abortAorTransaction(String uid, Mono<AbortTransactionRequest> abortTransactionRequest, ServerWebExchange exchange) {
        return aorService.abortTransaction(uid, abortTransactionRequest).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<CompleteTransactionResponse>> completeAorTransaction(String uid, Mono<CompleteTransactionRequest> completeTransactionRequest, ServerWebExchange exchange) {
        /*CompleteTransactionResponse response = new CompleteTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage("OK");
        response.setStatus(status);
        return Mono.delay(Duration.ofMillis(rnd.nextInt(500))).just(ResponseEntity.status(HttpStatus.OK).body(response));
        */
        return aorService.completeTransaction(uid, completeTransactionRequest).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));

    }

    @Override
    public Mono<ResponseEntity<StartTransactionResponse>> startAorTransaction(String uid, Mono<AorStartTransactionRequest> aorStartTransactionRequest, ServerWebExchange exchange) {
        StartTransactionResponse response = new StartTransactionResponse();
        StartTransactionResponseStatus status = new StartTransactionResponseStatus();
        status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage("OK");
        status.setRetryAfter(BigDecimal.valueOf(600));
        response.setStatus(status);

        final String iun = "LJLH-GNTJ-DVXR-202209-J-1";
        final String docIdx = "0";

        return aorStartTransactionRequest
                .zipWhen(r -> pnDeliveryClient.getPresignedUrlDocument(iun, docIdx, r.getRecipientTaxId()), (request, urls) -> urls)
                .zipWith(Mono.just(response))
                .map(urlAndResponse -> {
                    urlAndResponse.getT2().setUrlList(urlAndResponse.getT2().getUrlList());
                    return ResponseEntity.status(HttpStatus.OK).body(urlAndResponse.getT2());
                });
    }
}
