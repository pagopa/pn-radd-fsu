package it.pagopa.pn.radd.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class PnCheckQrCodeException extends PnRaddException {

    public PnCheckQrCodeException(WebClientResponseException webClientEx) {
        super(webClientEx);
    }
}