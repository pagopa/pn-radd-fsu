package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.rest.radd.v1.api.ActDocumentInquiryApi;
import it.pagopa.pn.radd.rest.radd.v1.api.ActTransactionManagementApi;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.services.radd.fsu.v1.ActService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@RestController
public class ActPrivateRestV1Controller implements ActDocumentInquiryApi, ActTransactionManagementApi {


    ActService actService;

    public ActPrivateRestV1Controller(ActService actService) {
        this.actService = actService;
    }

    @Override
    public Mono<ResponseEntity<ActInquiryResponse>> actInquiry(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, String recipientTaxId, String recipientType, String qrCode, String iun, final ServerWebExchange exchange) {
        return actService.actInquiry(uid, recipientTaxId, recipientType, qrCode).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<StartTransactionResponse>> startActTransaction(String uid, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId,  Mono<ActStartTransactionRequest> actStartTransactionRequest, ServerWebExchange exchange) {
        return actStartTransactionRequest
                .zipWhen(request -> actService.startTransaction(uid, request), (req, resp) -> resp)
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<CompleteTransactionResponse>> completeActTransaction(String uid, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, Mono<CompleteTransactionRequest> completeTransactionRequest, ServerWebExchange exchange) {
        return completeTransactionRequest
                .zipWhen(req -> actService.completeTransaction(uid, req), (req, resp) -> resp)
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<AbortTransactionResponse>> abortActTransaction(String uid, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, Mono<AbortTransactionRequest> abortTransactionRequest, ServerWebExchange exchange) {
        return abortTransactionRequest
                .zipWhen(req -> actService.abortTransaction(uid, req), (req, resp) -> resp)
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }
}
