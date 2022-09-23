package it.pagopa.pn.radd.exception;


public class RaddTransactionAlreadyExist extends PnException {

    public RaddTransactionAlreadyExist() {
        super("Transaction db", "Transazione già esistente o con stato completed o aborted");
    }

}
