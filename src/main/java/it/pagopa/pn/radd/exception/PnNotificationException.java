package it.pagopa.pn.radd.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class PnNotificationException extends PnRaddException {

    public PnNotificationException(WebClientResponseException webClientEx) {
        super(webClientEx);
    }
}