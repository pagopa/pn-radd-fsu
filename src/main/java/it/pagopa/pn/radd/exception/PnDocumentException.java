package it.pagopa.pn.radd.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class PnDocumentException extends PnRaddException {

    public PnDocumentException(WebClientResponseException webClientEx) {
        super(webClientEx);
    }
}