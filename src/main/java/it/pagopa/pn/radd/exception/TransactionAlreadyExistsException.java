package it.pagopa.pn.radd.exception;

public class TransactionAlreadyExistsException extends RaddGenericException {

    public TransactionAlreadyExistsException() {
        super(ExceptionTypeEnum.TRANSACTION_ALREADY_EXIST);
    }

}
