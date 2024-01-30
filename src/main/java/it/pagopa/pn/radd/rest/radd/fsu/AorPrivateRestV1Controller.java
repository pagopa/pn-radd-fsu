package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.rest.radd.v1.api.AorDocumentInquiryApi;
import it.pagopa.pn.radd.rest.radd.v1.api.AorTransactionManagementApi;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.services.radd.fsu.v1.AorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class AorPrivateRestV1Controller implements AorDocumentInquiryApi, AorTransactionManagementApi {
    private final AorService aorService;

    public AorPrivateRestV1Controller(AorService aorService) {
        this.aorService = aorService;
    }


    @Override
    public Mono<ResponseEntity<AORInquiryResponse>> aorInquiry(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, String recipientTaxId,
                                                               String recipientType, ServerWebExchange exchange) {
        return aorService.aorInquiry(uid, recipientTaxId, recipientType, xPagopaPnCxType, xPagopaPnCxId).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<AbortTransactionResponse>> abortAorTransaction(String uid, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, Mono<AbortTransactionRequest> abortTransactionRequest, ServerWebExchange exchange) {
        return aorService.abortTransaction(uid, abortTransactionRequest).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<CompleteTransactionResponse>> completeAorTransaction(String uid, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, Mono<CompleteTransactionRequest> completeTransactionRequest, ServerWebExchange exchange) {
        return aorService.completeTransaction(uid, completeTransactionRequest).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));

    }

    @Override
    public Mono<ResponseEntity<StartTransactionResponse>> startAorTransaction(String uid, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, Mono<AorStartTransactionRequest> aorStartTransactionRequest, ServerWebExchange exchange) {
        return aorStartTransactionRequest
                .zipWhen(req -> aorService.startTransaction(uid, req), (req, resp) -> resp)
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }
}
