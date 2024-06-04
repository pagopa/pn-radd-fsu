package it.pagopa.pn.radd.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RaddGenericException extends RuntimeException {

    private final ExceptionTypeEnum exceptionType;
    private final HttpStatus status;
    private final String message;
    private final Object extra;


    public RaddGenericException(ExceptionTypeEnum exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
        this.message = exceptionType.getMessage();
        this.status = HttpStatus.BAD_REQUEST;
        this.extra = null;
    }

    public RaddGenericException(ExceptionTypeEnum exceptionType, Object extra){
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
        this.message = exceptionType.getMessage();
        this.status = HttpStatus.BAD_REQUEST;
        this.extra = extra;
    }

    public RaddGenericException(ExceptionTypeEnum exceptionType, HttpStatus status) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
        this.message = exceptionType.getMessage();
        this.status = status==null?HttpStatus.BAD_REQUEST:status;
        this.extra = null;
    }

    public RaddGenericException(String message){
        super(message);
        this.exceptionType = null;
        this.message = message;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.extra = null;
    }

}
