package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.rest.radd.v1.api.NotificationInquiryApi;

import it.pagopa.pn.radd.rest.radd.v1.dto.OperationActResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationAorResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationsResponse;
import it.pagopa.pn.radd.services.radd.fsu.v1.OperationService;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
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
    public Mono<ResponseEntity<OperationActResponse>> getActTransactionByOperationId(String idPractice, ServerWebExchange exchange) {
        return operationService.getTransactionActByOperationIdAndType(idPractice).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<OperationsResponse>> getActPracticesByIun(String iun, ServerWebExchange exchange) {
        return operationService.getTransactionActByIun(iun)
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<OperationsResponse>> getAorPracticesByIun(String iun, ServerWebExchange exchange) {
        //TODO cambiare con quello di AOR
        return operationService.getTransactAorByIun(iun)
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<OperationAorResponse>> getAorTransactionByOperationId(String operationId, ServerWebExchange exchange) {
        return operationService.getTransactionAorByOperationIdAndType(operationId)
                .map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }
}
