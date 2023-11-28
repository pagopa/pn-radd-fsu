package it.pagopa.pn.radd.exception;

public class PaperNotificationFailedEmptyException extends RaddGenericException {


    public PaperNotificationFailedEmptyException() {
        super(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF);
    }
}
