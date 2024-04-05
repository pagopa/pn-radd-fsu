package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.api.ImportApi;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.VerifyRequestResponse;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
public class RegistryImportController implements ImportApi {

    private final RegistryImportService registryImportService;

    /**
     * POST /radd-alt/api/v1/registry/import/upload
     * API utilizzata per la richiesta della presigned URL utilizzata per il caricamento del file CSV contenente la lista di sportelli di un soggetto RADD.
     *
     * @param xPagopaPnCxType       Customer/Receiver Type (required)
     * @param xPagopaPnCxId         Customer/Receiver Identifier (required)
     * @param uid                   Identificativo pseudo-anonimizzato dell&#39;operatore RADD (required)
     * @param registryUploadRequest (required)
     * @return OK (status code 200)
     * or Bad Request (status code 400)
     * or Unauthorized (status code 401)
     * or Forbidden (status code 403)
     * or Method not allowed (status code 405)
     * or Internal Server Error (status code 500)
     */
/*    @Override
    public Mono<ResponseEntity<RegistryUploadResponse>> uploadRegistryRequests(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, Mono<RegistryUploadRequest> registryUploadRequest, final ServerWebExchange exchange) {
        return registryImportService.uploadRegistryRequests(xPagopaPnCxId, registryUploadRequest)
                .map(registryUploadResponse -> ResponseEntity.status(HttpStatus.OK).body(registryUploadResponse));
    }*/
    /**
     * GET /radd-alt/api/v1/registry/import/{requestId}/verify
     * L’API di verifica stato richiesta import restituisce lo stato di tale richiesta di import
     *
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param uid Identificativo pseudo-anonimizzato dell&#39;operatore RADD (required)
     * @param requestId Identificativo univoco della richiesta di censimento (CSV o CRUD) (required)
     * @return OK (status code 200)
     *         or Bad Request (status code 400)
     *         or Unauthorized (status code 401)
     *         or Forbidden (status code 403)
     *         or Method not allowed (status code 405)
     *         or Internal Server Error (status code 500)
     */
/*    @Override
    public Mono<ResponseEntity<VerifyRequestResponse>> verifyRequest(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, String requestId, final ServerWebExchange exchange) {
        return registryImportService.verifyRegistriesImportRequest(xPagopaPnCxId, requestId)
                .map(verifyRequestResponse -> ResponseEntity.status(HttpStatus.OK).body(verifyRequestResponse));
    }*/
}
