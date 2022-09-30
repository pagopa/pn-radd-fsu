package it.pagopa.pn.radd.exception;

public class PnInvalidInputException extends PnException {

    public PnInvalidInputException() {
        this("Alcuni parametri non sono validi");
    }

    public PnInvalidInputException(String message) {
        super("Parametri non validi", message);
    }

}