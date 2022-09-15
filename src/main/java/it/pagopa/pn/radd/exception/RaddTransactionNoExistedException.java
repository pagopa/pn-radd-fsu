package it.pagopa.pn.radd.exception;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import org.springframework.http.HttpStatus;

public class RaddTransactionNoExistedException  extends PnHttpResponseException {

    public RaddTransactionNoExistedException() {
        super("Non è stata trovata la transazione", HttpStatus.CONFLICT.value());
    }
}
