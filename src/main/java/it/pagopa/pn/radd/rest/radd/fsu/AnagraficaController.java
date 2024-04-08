package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.api.RegistryApi;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.services.radd.fsu.v1.AnagraficaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class AnagraficaController implements RegistryApi {

    private final AnagraficaService anagraficaService;

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
        return anagraficaService.deleteRegistry(xPagopaPnCxId,registryId, endDate)
                .map(registryUploadResponse -> ResponseEntity.noContent().build());
    }
}
