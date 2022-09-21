package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.rest.radd.v1.api.DocumentInquiryApi;
import it.pagopa.pn.radd.rest.radd.v1.api.TransactionManagementApi;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.services.radd.fsu.v1.ActService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class ActPrivateRestV1Controller implements DocumentInquiryApi, TransactionManagementApi {

    ActService actService;

    public ActPrivateRestV1Controller(ActService actService) {
        this.actService = actService;
    }

    @Override
    public Mono<ResponseEntity<ActInquiryResponse>> actInquiry(String uid, String recipientTaxId, String recipientType, String qrCode, final ServerWebExchange exchange) {
        return actService.actInquiry(uid, recipientTaxId, recipientType, qrCode).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<StartTransactionResponse>> startTransaction(String uid, Mono<ActStartTransactionRequest> actStartTransactionRequest, ServerWebExchange exchange) {
        return actService.startTransaction(uid, actStartTransactionRequest).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<CompleteTransactionResponse>> completeActTransaction(String uid, Mono<CompleteTransactionRequest> completeTransactionRequest, ServerWebExchange exchange) {
        return actService.completeTransaction(uid, completeTransactionRequest).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }
}
