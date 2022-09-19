package it.pagopa.pn.radd.exception;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import org.springframework.http.HttpStatus;


public class PnInvalidInputException extends PnException {

    public PnInvalidInputException() {
        super("Parametri non validi", "Alcuni parametri non sono validi");
    }

}