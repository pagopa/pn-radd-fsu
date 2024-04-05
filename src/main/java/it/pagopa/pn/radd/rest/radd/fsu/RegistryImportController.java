package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.api.RegistryApi;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class RegistryImportController implements RegistryApi {
    private final RegistryService registryService;

    @Override
    public Mono<ResponseEntity<CreateRegistryResponse>> addRegistry(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, Mono<CreateRegistryRequest> createRegistryRequest, final ServerWebExchange exchange) {
        return registryService.addRegistry(xPagopaPnCxType, xPagopaPnCxId, uid, createRegistryRequest)
                .map(createRegistryResponse -> ResponseEntity.status(HttpStatus.OK).body(createRegistryResponse));
    }
}
