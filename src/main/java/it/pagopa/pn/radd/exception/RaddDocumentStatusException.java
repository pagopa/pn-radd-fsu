package it.pagopa.pn.radd.exception;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import org.springframework.http.HttpStatus;

public class RaddDocumentStatusException extends PnHttpResponseException {


    public RaddDocumentStatusException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }
}

