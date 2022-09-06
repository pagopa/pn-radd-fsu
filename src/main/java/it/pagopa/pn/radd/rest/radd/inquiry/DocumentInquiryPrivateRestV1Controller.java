package it.pagopa.pn.radd.rest.radd.inquiry;

import it.pagopa.pn.radd.rest.radd.v1.api.DocumentInquiryApi;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponse;
import it.pagopa.pn.radd.services.radd.inquiry.v1.DocumentInquiryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class DocumentInquiryPrivateRestV1Controller implements DocumentInquiryApi {

    DocumentInquiryService documentInquiryService;

    public DocumentInquiryPrivateRestV1Controller(DocumentInquiryService documentInquiryService) {
        this.documentInquiryService = documentInquiryService;
    }

    @Override
    public Mono<ResponseEntity<ActInquiryResponse>> actInquiry(String uid, String recipientTaxId, String recipientType, String qrCode, final ServerWebExchange exchange) {
        return documentInquiryService.actInquiry().map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }
}
