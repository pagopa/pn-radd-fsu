package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.api.RegistryStoreApi;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.StoreRegistriesResponse;
import it.pagopa.pn.radd.services.radd.fsu.v1.StoreLocatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class StoreRegistryController implements RegistryStoreApi {

    private final StoreLocatorService storeLocatorService;

    @Override
    public Mono<ResponseEntity<StoreRegistriesResponse>> retrieveStoreRegistries(Integer limit, String lastKey, final ServerWebExchange exchange) {
        return storeLocatorService.retrieveStoreRegistries(limit, lastKey)
                .map(ResponseEntity::ok);
    }
}
