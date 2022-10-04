package it.pagopa.pn.radd.exception;

import lombok.Getter;

@Getter
public class PnInvalidInputException extends RuntimeException {
    private final String reason;


    public PnInvalidInputException(String reason) {
        super(reason);
        this.reason = reason;
    }
}