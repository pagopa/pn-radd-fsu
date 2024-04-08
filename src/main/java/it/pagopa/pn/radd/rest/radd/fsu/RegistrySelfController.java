package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.api.RegistryApi;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.UpdateRegistryRequest;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistrySelfService;
import lombok.RequiredArgsConstructor;
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
     * PATCH /radd-alt/api/v1/registry/{registryId}/dismiss
     * L'API di eliminazione sportello permette a un soggetto RADD di rimuovere un'anagrafica fornendo l'identificativo univoco dello sportello e la data in cui tale sportello sarà disattivato.
     *
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param uid Identificativo pseudo-anonimizzato dell'operatore RADD (required)
     * @param registryId Identificativo univoco dello sportello (required)
     * @param endDate Data in cui tale sportello sarà disattivato (required)
     * @return OK (status code 204)
     *         or Bad Request (status code 400)
     *         or Unauthorized (status code 401)
     *         or Forbidden (status code 403)
     *         or Method not allowed (status code 405)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> deleteRegistry(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, String registryId,String endDate ,final ServerWebExchange exchange) {
        return registrySelfService.deleteRegistry(xPagopaPnCxId, registryId, endDate)
                .map(registryUploadResponse -> ResponseEntity.noContent().build());
    }
}
