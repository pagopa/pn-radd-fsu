package it.pagopa.pn.radd.exception;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;

import org.springframework.http.HttpStatus;

public class RaddIunNotFoundException  extends PnHttpResponseException {


    public RaddIunNotFoundException() {
        super("Iun not found with params", HttpStatus.NOT_FOUND.value());
    }
}
