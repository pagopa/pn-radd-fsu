package it.pagopa.pn.radd.exception;

public class RaddTransactionNoExistedException  extends PnException {

    public RaddTransactionNoExistedException() {
        super("Transazione non trovata", "Non è stata trovata la transazione con questo operation id");
    }

    public RaddTransactionNoExistedException(String message) {
        super(message, "Transazione non trovata");
    }
}
