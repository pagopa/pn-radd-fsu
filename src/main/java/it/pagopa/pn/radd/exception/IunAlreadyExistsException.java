package it.pagopa.pn.radd.exception;

public class IunAlreadyExistsException extends RaddGenericException {

    public IunAlreadyExistsException(Integer maxNumberOfPrints) {
        super(ExceptionTypeEnum.ALREADY_COMPLETE_PRINT, formatMessage(maxNumberOfPrints));
    }

    private static String formatMessage(Integer maxNumberOfPrints) {
        String prints = maxNumberOfPrints == 1 ? "stampa" : "stampe";
        return String.format(ExceptionTypeEnum.ALREADY_COMPLETE_PRINT.getMessage(), maxNumberOfPrints, prints);
    }
}


