package it.pagopa.pn.radd.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class PnEnsureFiscalCodeException extends PnRaddException {

    public PnEnsureFiscalCodeException(WebClientResponseException webClientEx) {
        super(webClientEx);
    }
}