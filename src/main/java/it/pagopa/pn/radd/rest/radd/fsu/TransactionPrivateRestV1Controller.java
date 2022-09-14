package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.rest.radd.v1.api.TransactionManagementApi;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActStartTransactionRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.StartTransactionResponse;
import it.pagopa.pn.radd.services.radd.fsu.v1.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

 @Slf4j
@RestController
public class TransactionPrivateRestV1Controller implements TransactionManagementApi {

    private final TransactionService transactionService;

    public TransactionPrivateRestV1Controller(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Mono<ResponseEntity<StartTransactionResponse>> startTransaction(String uid, Mono<ActStartTransactionRequest> actStartTransactionRequest, ServerWebExchange exchange) {
        log.info("Rest Controller");
        return transactionService.startTransaction(uid, actStartTransactionRequest).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }
}
