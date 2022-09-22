package it.pagopa.pn.radd.exception;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import org.springframework.http.HttpStatus;

public class RaddFiscalCodeEnsureException  extends PnHttpResponseException {

    public RaddFiscalCodeEnsureException() {
        super("Non è stato possibile recuperare il valore", HttpStatus.CONFLICT.value());
    }
}
