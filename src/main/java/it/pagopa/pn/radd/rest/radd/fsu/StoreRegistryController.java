package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v2.api.RegistryStorePrivateApi;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.StoreRegistriesResponse;
import it.pagopa.pn.radd.services.radd.fsu.v1.StoreRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class StoreRegistryController implements RegistryStorePrivateApi {

    private final StoreRegistryService storeRegistryService;

    @Override
    public Mono<ResponseEntity<StoreRegistriesResponse>> retrieveStoreRegistries(Integer limit, String lastKey, final ServerWebExchange exchange) {
        return storeRegistryService.retrieveStoreRegistries(limit, lastKey)
                .map(ResponseEntity::ok);
    }
}
