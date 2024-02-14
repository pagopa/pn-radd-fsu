package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.api.DocumentOperationsApi;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DocumentUploadRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DocumentUploadResponse;
import it.pagopa.pn.radd.services.radd.fsu.v1.DocumentOperationsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class DocumentOperationsRestV1Controller implements DocumentOperationsApi {

    private final DocumentOperationsService documentOperationsService;

    public DocumentOperationsRestV1Controller(DocumentOperationsService documentOperationsService) {
        this.documentOperationsService = documentOperationsService;
    }


    /**
     * GET /radd-net/api/v1/download/{operationType}/{operationId}
     * API utilizzata per il download del frontespizio
     *
     * @param operationType   Tipo di operazione aor o act (required)
     * @param operationId     Id della pratica (required)
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId   Customer/Receiver Identifier (required)
     * @return Ritorna il frontespizio (status code 200)
     * or Bad Request (status code 400)
     * or Unauthorized (status code 401)
     * or Forbidden (status code 403)
     * or Method not allowed (status code 405)
     * or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<byte[]>> documentDownload(String operationType, String operationId, CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String attachmentId,  final ServerWebExchange exchange) {
        return documentOperationsService.documentDownload(operationType, operationId, xPagopaPnCxType, xPagopaPnCxId)
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<DocumentUploadResponse>> documentUpload(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, Mono<DocumentUploadRequest> documentUploadRequest, ServerWebExchange exchange) {
        return documentOperationsService.createFile(uid, documentUploadRequest).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }
}
