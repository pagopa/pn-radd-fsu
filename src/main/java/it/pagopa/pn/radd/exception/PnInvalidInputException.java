package it.pagopa.pn.radd.exception;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import org.springframework.http.HttpStatus;


public class PnInvalidInputException extends PnException {

    public PnInvalidInputException() {
        this("Alcuni parametri non sono validi");
    }

    public PnInvalidInputException(String message) {
        super("Parametri non validi", message);
    }

}