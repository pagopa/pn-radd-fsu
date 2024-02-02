package it.pagopa.pn.radd.exception;

public class IunAlreadyExistsException extends RaddGenericException {

    public IunAlreadyExistsException() {
        super(ExceptionTypeEnum.ALREADY_COMPLETE_PRINT);
    }

}


