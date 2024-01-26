package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.rest.radd.v1.api.DocumentUploadApi;
import it.pagopa.pn.radd.rest.radd.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadResponse;
import it.pagopa.pn.radd.services.radd.fsu.v1.DocumentUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class DocumentUploadPrivateRestV1Controller implements DocumentUploadApi {

    DocumentUploadService documentUploadService;

    public DocumentUploadPrivateRestV1Controller(DocumentUploadService documentUploadService) {
        this.documentUploadService = documentUploadService;
    }

    @Override
    public Mono<ResponseEntity<DocumentUploadResponse>> documentUpload(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, Mono<DocumentUploadRequest> documentUploadRequest, ServerWebExchange exchange) {
        return documentUploadService.createFile(uid, documentUploadRequest).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }
}
