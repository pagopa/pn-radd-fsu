package it.pagopa.pn.radd.exception;

public class PnException extends RuntimeException {
    
    private final String description;
    private final int status;

    public PnException(String message, String description) {
        super(message);
        this.description = description;
        this.status = 400;
    }

    public PnException(String message, String description, int status) {
        super(message);
        this.description = description;
        this.status = status;
    }

    public String getDescription() {
        return description;
    }
    public int getStatus(){ return status; }
}
