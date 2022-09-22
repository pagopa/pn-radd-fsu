package it.pagopa.pn.radd.exception;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import org.springframework.http.HttpStatus;

public class RaddChecksumException extends PnHttpResponseException {

    public RaddChecksumException() {
        super("Il valore checksum non è stato trovato o non corrisponde", HttpStatus.CONFLICT.value());
    }
}
