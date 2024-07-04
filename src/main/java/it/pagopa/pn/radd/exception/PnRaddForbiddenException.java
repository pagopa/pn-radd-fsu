package it.pagopa.pn.radd.exception;

public class PnRaddForbiddenException extends RuntimeException {

    private final int status;

    public PnRaddForbiddenException(String message, int status) {
        super(message);
        this.status = status;
    }
    public int getStatus(){ return status; }
}
