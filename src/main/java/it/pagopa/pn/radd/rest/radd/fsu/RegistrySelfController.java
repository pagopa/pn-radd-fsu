package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.api.RegistryApi;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.GetRegistryResponseV2;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistrySelfService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static it.pagopa.pn.radd.utils.RaddRegistryUtils.validatePartnerId;

@RestController
@RequiredArgsConstructor
@CustomLog
public class RegistrySelfController implements RegistryApi {

    private final RegistrySelfService registrySelfService;

    @Override
    public Mono<ResponseEntity<RegistryV2>> addRegistry(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, Mono<CreateRegistryRequestV2> createRegistryRequestV2, ServerWebExchange exchange) {
        validatePartnerId(xPagopaPnCxId);
        return createRegistryRequestV2.flatMap(request -> registrySelfService.addRegistry(xPagopaPnCxId,
                        UUID.randomUUID().toString(),
                        uid,
                        request))
                .map(createRegistryResponse -> ResponseEntity.status(HttpStatus.OK).body(createRegistryResponse));
    }

    @Override
    public Mono<ResponseEntity<RegistryV2>> updateRegistry(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, String locationId, Mono<UpdateRegistryRequestV2> updateRegistryRequestV2, ServerWebExchange exchange) {
        validatePartnerId(xPagopaPnCxId);
        return updateRegistryRequestV2.flatMap(request -> registrySelfService.updateRegistry(xPagopaPnCxId, locationId, uid, request))
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteRegistry(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, String locationId, ServerWebExchange exchange) {
        validatePartnerId(xPagopaPnCxId);
        return registrySelfService.deleteRegistry(xPagopaPnCxId, locationId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<GetRegistryResponseV2>> retrieveRegistries(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, Integer limit, String lastKey, ServerWebExchange exchange) {
        validatePartnerId(xPagopaPnCxId);
        return registrySelfService.retrieveRegistries(xPagopaPnCxId, limit, lastKey)
                .map(getRegistryResponseV2 -> ResponseEntity.status(HttpStatus.OK).body(getRegistryResponseV2));
    }

}
