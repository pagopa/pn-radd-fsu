package it.pagopa.pn.radd.exception;

public class QrCodeAlreadyExistsException extends RaddGenericException {

    public QrCodeAlreadyExistsException() {
        super(ExceptionTypeEnum.ALREADY_COMPLETE_PRINT);
    }

}
