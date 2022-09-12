package it.pagopa.pn.radd.exception;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import org.springframework.http.HttpStatus;

public class RaddTransactionAlreadyExist extends PnHttpResponseException {

    public RaddTransactionAlreadyExist() {
        super("Non è possibile creare due Transaction per lo stesso utente", HttpStatus.CONFLICT.value());
    }

}
