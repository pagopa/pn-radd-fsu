package it.pagopa.pn.radd.exception;

import org.springframework.http.HttpStatus;

public class CoordinatesNotFoundException extends RaddGenericException {
    public CoordinatesNotFoundException(String message) {
        super(ExceptionTypeEnum.COORDINATES_NOT_FOUND, message, HttpStatus.BAD_REQUEST);
    }
}
