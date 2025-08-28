package it.pagopa.pn.radd.exception;

import org.springframework.http.HttpStatus;

public class UrlSanitizeException extends RaddGenericException {

    public UrlSanitizeException(String message) {
        super(ExceptionTypeEnum.INVALID_URL, message, HttpStatus.BAD_REQUEST);
    }
}
