package it.pagopa.pn.radd.exception;

import lombok.Getter;

@Getter
public enum ExceptionTypeEnum {
    IUN_NOT_FOUND("IUN_NOT_FOUND", "Iun not found with params"),
    TRANSACTION_NOT_EXIST("TRANSACTION_NOT_EXIST", "Transazione inesistente"),
    TRANSACTION_ALREADY_EXIST("TRANSACTION_ALREADY_EXIST", "Transazione già esistente o con stato completed o aborted"),
    TRANSACTION_ALREADY_COMPLETED("Stato Transazione incoerente", "La trasazione risulta già completa"),
    TRANSACTION_ALREADY_ABORTED("Stato Transazione incoerente", "La trasazione risulta annullata"),
    TRANSACTION_NOT_UPDATE_STATUS("TRANSACTION_NOT_UPDATE_STATUS", "Lo stato della transazione non è stato aggiornato"),
    CHECKSUM_VALIDATION("CHECKSUM_VALIDATION", "Il valore del checksum non corrisponde"),

    DOCUMENT_STATUS_VALIDATION("DOCUMENT_STATUS_VALIDATION", "Stato documento non corretto"),
    DOCUMENT_UPLOAD_ERROR("DOCUMENT_UPLOAD_ERROR", "si è verificato un errore durante il caricamento"),

    VERSION_ID_VALIDATION("VERSION_ID_VALIDATION", "Version id non corrispondono"),
    QR_CODE_VALIDATION("QR_CODE_VALIDATION", "QrCode non valido"),
    DOCUMENT_NOT_FOUND("DOCUMENT_NOT_FOUND", "Documenti non più disponibili"),
    ALREADY_COMPLETE_PRINT("ALREADY_COMPLETE_PRINT", "Stampa già eseguita"),
    CF_OR_QRCODE_NOT_VALID("CF_OR_QRCODE_NOT_VALID", "Input non valido"),
    ENSURE_FISCAL_CODE_EMPTY("ENSURE_FISCAL_CODE_EMPTY", "Il codice fiscale non è stato anonimizzato"),
    GENERIC_ERROR("GENERIC_ ERROR", "Si è verificato un errore"),
    ;
    private final String title;
    private final String message;

    ExceptionTypeEnum(String title, String message){
        this.title = title;
        this.message = message;
    }

}
