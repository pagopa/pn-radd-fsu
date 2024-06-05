package it.pagopa.pn.radd.exception;

import org.springframework.http.HttpStatus;

public class ZipAttachmentNotFoundException extends RaddGenericException {

    public ZipAttachmentNotFoundException() {
        super(ExceptionTypeEnum.ZIP_ATTACHMENT_URL_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

}


