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

import java.security.SecureRandom;
import java.time.Duration;


@RestController
public class AorPrivateRestV1Controller implements AorDocumentInquiryApi, AorTransactionManagementApi {

    private final SecureRandom rnd = new SecureRandom();
    private final AorService aorService;

    public AorPrivateRestV1Controller(AorService aorService) {
        this.aorService = aorService;
    }


    @Override
    public Mono<ResponseEntity<AORInquiryResponse>> aorInquiry(String uid, String recipientTaxId,
                                                               String recipientType, ServerWebExchange exchange) {
        return aorService.aorInquiry(uid, recipientTaxId, recipientType).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<AbortTransactionResponse>> abortAorTransaction(String uid, Mono<AbortTransactionRequest> abortTransactionRequest, ServerWebExchange exchange) {
        AbortTransactionResponse response = new AbortTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage("OK");
        response.setStatus(status);
        return Mono.delay(Duration.ofMillis(rnd.nextInt(500))).just(ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @Override
    public Mono<ResponseEntity<CompleteTransactionResponse>> completeAorTransaction(String uid, Mono<CompleteTransactionRequest> completeTransactionRequest, ServerWebExchange exchange) {
        CompleteTransactionResponse response = new CompleteTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage("OK");
        response.setStatus(status);
        return Mono.delay(Duration.ofMillis(rnd.nextInt(500))).just(ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @Override
    public Mono<ResponseEntity<StartTransactionResponse>> startAorTransaction(String uid, Mono<AorStartTransactionRequest> aorStartTransactionRequest, ServerWebExchange exchange) {
        return aorService.startTransaction(uid, aorStartTransactionRequest).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }
}
