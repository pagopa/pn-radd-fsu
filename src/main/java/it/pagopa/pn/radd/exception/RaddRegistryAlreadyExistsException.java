package it.pagopa.pn.radd.exception;

public class RaddRegistryAlreadyExistsException extends RaddGenericException {

    public RaddRegistryAlreadyExistsException() {
        super(ExceptionTypeEnum.RADD_REGISTRY_ALREADY_EXISTS);
    }
}
