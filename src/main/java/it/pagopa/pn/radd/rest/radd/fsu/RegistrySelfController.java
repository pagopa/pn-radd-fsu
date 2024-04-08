package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.api.RegistryApi;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistrySelfService;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class RegistrySelfController implements RegistryApi {

    private final RegistrySelfService registryService;

    /**
     * POST /radd-alt/api/v1/registry
     * API utilizzata per la richiesta di attivazione nuova anagrafica RADD
     *
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param uid Identificativo pseudo-anonimizzato dell&#39;operatore RADD (required)
     * @param createRegistryRequest  (required)
     * @return OK (status code 200)
     *         or Bad Request (status code 400)
     *         or Unauthorized (status code 401)
     *         or Forbidden (status code 403)
     *         or Method not allowed (status code 405)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<CreateRegistryResponse>> addRegistry(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, Mono<CreateRegistryRequest> createRegistryRequest, final ServerWebExchange exchange) {
        return createRegistryRequest.flatMap(request -> registryService.addRegistry(xPagopaPnCxId, request))
                .map(createRegistryResponse -> ResponseEntity.status(HttpStatus.OK).body(createRegistryResponse));
    }
}
