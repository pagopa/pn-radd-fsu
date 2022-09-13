package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.rest.radd.v1.api.TransactionManagementApi;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActStartTransactionRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.StartTransactionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class TransactionPrivateRestV1Controller implements TransactionManagementApi {

    @Override
    public Mono<ResponseEntity<StartTransactionResponse>> startTransaction(String uid, Mono<ActStartTransactionRequest> actStartTransactionRequest, ServerWebExchange exchange) {
        return TransactionManagementApi.super.startTransaction(uid, actStartTransactionRequest, exchange);
    }
}
