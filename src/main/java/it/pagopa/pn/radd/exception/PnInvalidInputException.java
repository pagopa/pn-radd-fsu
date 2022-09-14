package it.pagopa.pn.radd.exception;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import org.springframework.http.HttpStatus;


public class PnInvalidInputException extends PnHttpResponseException {

    public PnInvalidInputException() {
        super("Invalid input", HttpStatus.NO_CONTENT.value());
    }

}