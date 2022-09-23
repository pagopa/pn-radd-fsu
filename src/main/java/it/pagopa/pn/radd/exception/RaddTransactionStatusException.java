package it.pagopa.pn.radd.exception;

public class RaddTransactionStatusException  extends PnException{

    public RaddTransactionStatusException() {
        super("Stato transazione non corretto", "Valore non corretto");
    }

    public RaddTransactionStatusException(String message, String description, int code) {
        super(message, description, code);
    }

}
