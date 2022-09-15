package it.pagopa.pn.radd.rest.radd.fsu;


import it.pagopa.pn.radd.rest.radd.v1.api.NotificationInquiryApi;
import it.pagopa.pn.radd.rest.radd.v1.dto.NotificationPracticesResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.NotificationResponse;
import it.pagopa.pn.radd.services.radd.fsu.v1.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class NotificationRestV1Controller implements NotificationInquiryApi {
    private final NotificationService notificationService;

    public NotificationRestV1Controller(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public Mono<ResponseEntity<NotificationResponse>> getTransaction(String idPractice, ServerWebExchange exchange) {
        return notificationService.getTransaction(idPractice).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

    @Override
    public Mono<ResponseEntity<NotificationPracticesResponse>> getPractices(String iun, ServerWebExchange exchange) {
        return notificationService.getPracticesId(iun).map(m -> ResponseEntity.status(HttpStatus.OK).body(m));
    }

}
