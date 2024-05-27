package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.api.RegistryStoreApi;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistriesStoreResponse;
import it.pagopa.pn.radd.services.radd.fsu.v1.StoreLocatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class StoreLocatorController implements RegistryStoreApi {

    private final StoreLocatorService storeLocatorService;

    @Override
    public Mono<ResponseEntity<RegistriesStoreResponse>> retrieveStoreRegistries(Integer limit, String lastKey, final ServerWebExchange exchange) {
        return storeLocatorService.retrieveStoreRegistries(limit, lastKey)
                .map(registriesStoreResponse -> ResponseEntity.status(HttpStatus.OK).body(registriesStoreResponse));
    }
}
