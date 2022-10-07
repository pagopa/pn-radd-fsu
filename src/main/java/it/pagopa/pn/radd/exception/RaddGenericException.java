package it.pagopa.pn.radd.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RaddGenericException extends RuntimeException {


    private final ExceptionTypeEnum exceptionType;
    private final ExceptionCodeEnum code;
    private final HttpStatus status;
    private final String message;
    private final Object extra;


    public RaddGenericException(ExceptionTypeEnum exceptionType, ExceptionCodeEnum code) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
        this.code = code;
        this.status = null;
        this.message = null;
        this.extra = null;
    }

    public RaddGenericException(ExceptionTypeEnum exceptionType, ExceptionCodeEnum code, Object extra){
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
        this.code = code;
        this.status = null;
        this.message = null;
        this.extra = extra;
    }

    public RaddGenericException(ExceptionTypeEnum exceptionType, HttpStatus status) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
        this.code = null;
        this.message = null;
        this.status = status;
        this.extra = null;
    }

    public RaddGenericException(String message, ExceptionCodeEnum code){
        super(message);
        this.exceptionType = null;
        this.message = message;
        this.code = code;
        this.status = null;
        this.extra = null;
    }

}
