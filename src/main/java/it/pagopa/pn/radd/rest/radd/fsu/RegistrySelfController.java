package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.api.RegistryApi;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistriesResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.UpdateRegistryRequest;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistrySelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
public class RegistrySelfController implements RegistryApi {

    private final RegistrySelfService registrySelfService;

    /**
     * PATCH /radd-alt/api/v1/registry/{registryId}
     * API utilizzata per la modifica di un&#39;anagrafica RADD
     *
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param uid Identificativo pseudo-anonimizzato dell&#39;operatore RADD (required)
     * @param registryId Identificativo dello sportello RADD (required)
     * @param updateRegistryRequest  (required)
     * @return OK (status code 204)
     *         or Bad Request (status code 400)
     *         or Unauthorized (status code 401)
     *         or Forbidden (status code 403)
     *         or Method not allowed (status code 405)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> updateRegistry(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, String registryId, Mono<UpdateRegistryRequest> updateRegistryRequest,  final ServerWebExchange exchange) {
        return updateRegistryRequest.flatMap(request -> registrySelfService.updateRegistry(registryId, xPagopaPnCxId, request))
                .map(raddRegistryEntityMono -> ResponseEntity.noContent().build());
    }

    /**
     * GET /radd-alt/api/v1/registry
     * API utilizzata per recuperare la lista paginata di anagrafiche RADD dato il cxId
     *
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param uid Identificativo pseudo-anonimizzato dell&#39;operatore RADD (required)
     * @param limit  (optional, default to 10)
     * @param lastKey  (optional)
     * @param cap CAP (optional)
     * @param city Città (optional)
     * @param pr Provincia (optional)
     * @param externalCode Identificativo punto ritiro SEND indicato dal client (optional)
     * @return OK (status code 200)
     *         or Bad Request (status code 400)
     *         or Unauthorized (status code 401)
     *         or Forbidden (status code 403)
     *         or Method not allowed (status code 405)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public  Mono<ResponseEntity<RegistriesResponse>> retrieveRegistries(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, Integer limit, String lastKey, String cap, String city, String pr, String externalCode, final ServerWebExchange exchange) {
        return registrySelfService.registryListing(xPagopaPnCxId, limit, lastKey, cap, city, pr, externalCode)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }
}