package it.pagopa.pn.radd.exception;

import org.springframework.http.HttpStatus;

public class RaddChecksumException extends PnException {

    public RaddChecksumException() {
        super("CheckSum", "Il valore del checksum non corrisponde", HttpStatus.BAD_REQUEST.value());
    }
}
