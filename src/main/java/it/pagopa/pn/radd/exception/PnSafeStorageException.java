package it.pagopa.pn.radd.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class PnSafeStorageException extends PnRaddException{

    public PnSafeStorageException(WebClientResponseException ex) {
        super(ex);
    }


}
