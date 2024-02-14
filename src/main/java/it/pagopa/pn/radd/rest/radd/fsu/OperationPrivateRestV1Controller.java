package it.pagopa.pn.radd.rest.radd.fsu;


import it.pagopa.pn.radd.alt.generated.openapi.server.v1.api.NotificationInquiryApi;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.radd.services.radd.fsu.v1.OperationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class OperationPrivateRestV1Controller implements NotificationInquiryApi {
    private final OperationService operationService;

    public OperationPrivateRestV1Controller(OperationService operationService) {
        this.operationService = operationService;
    }

    @Override
    public Mono<ResponseEntity<OperationActResponse>> getActTransactionByTransactionId(String idPractice, ServerWebExchange exchange) {
        return operationService.getTransactionActByTransactionIdAndType(idPractice).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<OperationsResponse>> getActPracticesByIun(String iun, ServerWebExchange exchange) {
        return operationService.getOperationsActByIun(iun)
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<OperationsResponse>> getAorPracticesByIun(String iun, ServerWebExchange exchange) {
        return operationService.getOperationsAorByIun(iun)
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<OperationAorResponse>> getAorTransactionByTransactionId(String transactionId, ServerWebExchange exchange) {
        return operationService.getTransactionAorByTransactionIdAndType(transactionId)
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }


    @Override
    public Mono<ResponseEntity<OperationsActDetailsResponse>> getActPracticesByInternalId(String internalId, Mono<FilterRequest> filterRequest, ServerWebExchange exchange) {
        return filterRequest
                .flatMap(filter -> operationService.getAllActTransactionFromFiscalCode(internalId, filter.getFrom(), filter.getTo()))
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<OperationsAorDetailsResponse>> getAorPracticesByInternalId(String internalId, Mono<FilterRequest> filterRequest, ServerWebExchange exchange) {
        return filterRequest
                .flatMap(item -> operationService.getAllAorTransactionFromFiscalCode(internalId, item.getFrom(), item.getTo()))
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }
}
