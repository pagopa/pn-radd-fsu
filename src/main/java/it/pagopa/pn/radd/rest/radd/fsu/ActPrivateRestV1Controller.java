package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.rest.radd.v1.api.DocumentInquiryApi;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponse;
import it.pagopa.pn.radd.services.radd.fsu.v1.ActService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class ActPrivateRestV1Controller implements DocumentInquiryApi {

    ActService actService;

    public ActPrivateRestV1Controller(ActService actService) {
        this.actService = actService;
    }

    @Override
    public Mono<ResponseEntity<ActInquiryResponse>> actInquiry(String uid, String recipientTaxId, String recipientType, String qrCode, final ServerWebExchange exchange) {
        return actService.actInquiry(uid, recipientTaxId, recipientType, qrCode).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }
}
