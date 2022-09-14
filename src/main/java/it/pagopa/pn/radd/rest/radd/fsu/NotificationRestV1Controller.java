package it.pagopa.pn.radd.rest.radd.fsu;


import it.pagopa.pn.radd.rest.radd.v1.api.NotificationInquiryApi;
import it.pagopa.pn.radd.rest.radd.v1.dto.NotificationPracticesResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.NotificationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class NotificationRestV1Controller implements NotificationInquiryApi {


    @Override
    public Mono<ResponseEntity<NotificationResponse>> getTransaction(String idPractice, ServerWebExchange exchange) {
        return NotificationInquiryApi.super.getTransaction(idPractice, exchange);
    }

    @Override
    public Mono<ResponseEntity<NotificationPracticesResponse>> getPractices(String iun, ServerWebExchange exchange) {
        return NotificationInquiryApi.super.getPractices(iun, exchange);
    }

}
