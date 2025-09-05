package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v2.api.RegistryV2Api;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.*;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.GetRegistryResponseV2;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistrySelfServiceV2;
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
public class RegistrySelfControllerV2 implements RegistryV2Api {

    private final RegistrySelfServiceV2 registrySelfServiceV2;

    @Override
    public Mono<ResponseEntity<RegistryV2>> addRegistry(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, Mono<CreateRegistryRequestV2> createRegistryRequestV2, ServerWebExchange exchange) {
        validatePartnerId(xPagopaPnCxId);
        return createRegistryRequestV2.flatMap(request -> registrySelfServiceV2.addRegistry(xPagopaPnCxId,
                        UUID.randomUUID().toString(),
                        uid,
                        request))
                .map(createRegistryResponse -> ResponseEntity.status(HttpStatus.OK).body(createRegistryResponse));
    }

    @Override
    public Mono<ResponseEntity<RegistryV2>> updateRegistry(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, String locationId, Mono<UpdateRegistryRequestV2> updateRegistryRequestV2, ServerWebExchange exchange) {
        validatePartnerId(xPagopaPnCxId);
        return updateRegistryRequestV2.flatMap(request -> registrySelfServiceV2.updateRegistry(xPagopaPnCxId, locationId, uid, request))
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteRegistry(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, String locationId, ServerWebExchange exchange) {
        validatePartnerId(xPagopaPnCxId);
        return registrySelfServiceV2.deleteRegistry(xPagopaPnCxId, locationId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<GetRegistryResponseV2>> retrieveRegistries(CxTypeAuthFleet xPagopaPnCxType, String xPagopaPnCxId, String uid, Integer limit, String lastKey, ServerWebExchange exchange) {
        validatePartnerId(xPagopaPnCxId);
        return registrySelfServiceV2.retrieveRegistries(xPagopaPnCxId, limit, lastKey)
                .map(getRegistryResponseV2 -> ResponseEntity.status(HttpStatus.OK).body(getRegistryResponseV2));
    }

}
