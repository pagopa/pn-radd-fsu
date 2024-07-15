package it.pagopa.pn.radd.exception;

import lombok.Getter;

@Getter
public enum ExceptionTypeEnum {
    REGISTRY_NOT_FOUND("REGISTRY_NOT_FOUND", "Sede radd non trovata", 99),
    DATE_NOTICE_ERROR("DATE_NOTICE_ERROR","La data di fine validità non rispetta il preavviso minimo richiesto", 99),
    DATE_INTERVAL_ERROR("DATE_INTERVAL_ERROR","La data di fine validità non può essere precedente alla data di inizio validità", 99),
    DATE_INVALID_ERROR("DATE_INVALID_ERROR","formato della data non valido", 99),
    IUN_NOT_FOUND("IUN_NOT_FOUND", "Iun not found with params", 99),
    TRANSACTION_NOT_SAVED("TRANSACTION_NOT_SAVED", "La transazione non è stata salvata", 99),
    TRANSACTION_NOT_EXIST("TRANSACTION_NOT_EXIST", "Transazione inesistente", 99),
    TRANSACTION_ALREADY_EXIST("TRANSACTION_ALREADY_EXIST", "Transazione già esistente o con stato completed o aborted", 5),
    TRANSACTION_ALREADY_COMPLETED("TRANSACTION_ALREADY_COMPLETED", "La transazione risulta già completa", 99),
    TRANSACTION_ALREADY_ABORTED("TRANSACTION_ALREADY_ABORTED", "La transazione risulta annullata", 99),
    TRANSACTION_ERROR_STATUS("TRANSACTION_ERROR_STATUS", "La transazione risulta in errore", 99),
    TRANSACTION_NOT_UPDATE_STATUS("TRANSACTION_NOT_UPDATE_STATUS", "Lo stato della transazione non è stato aggiornato", 99),
    CHECKSUM_VALIDATION("CHECKSUM_VALIDATION", "Il valore del checksum non corrisponde", 99),
    DOCUMENT_STATUS_VALIDATION("DOCUMENT_STATUS_VALIDATION", "Stato documento non corretto", 99),
    DOCUMENT_UPLOAD_ERROR("DOCUMENT_UPLOAD_ERROR", "si è verificato un errore durante il caricamento", 99),
    DOCUMENT_UNAVAILABLE("DOCUMENT_UNAVAILABLE", "Documenti non disponibili", 4),
    VERSION_ID_VALIDATION("VERSION_ID_VALIDATION", "Version id non corrispondono", 99),
    ZIP_ATTACHMENT_URL_NOT_FOUND("ZIP_ATTACHMENT_URL_NOT_FOUND", "Allegato non valido", 99),
    DOCUMENT_NOT_FOUND("DOCUMENT_NOT_FOUND", "Documenti non più disponibili", 2),
    ALREADY_COMPLETE_PRINT("ALREADY_COMPLETE_PRINT", "Limite di %s %s superato", 3),
    INVALID_INPUT("CF_OR_QRCODE_NOT_VALID", "Input non valido", 10),
    ENSURE_FISCAL_CODE_EMPTY("ENSURE_FISCAL_CODE_EMPTY", "Il codice fiscale non è stato anonimizzato", 99),
    OPERATION_TYPE_UNKNOWN("OPERATION TYPE UNKNOWN", "Il tipo di operazione è sconosciuto", 99),
    GENERIC_ERROR("GENERIC ERROR", "Si è verificato un errore", 99),
    RETRY_AFTER("RETRY_AFTER", "Documento non disponibile per il download", 2),
    NOTIFICATION_CANCELLED("NOTIFICATION_CANCELLED", "Questa notifica è stata annullata dall’ente mittente", 80),
    NO_NOTIFICATIONS_FAILED("NO_NOTIFICATIONS_FAILED", "Non ci sono notifiche non consegnate", 99),

    NO_NOTIFICATIONS_FAILED_FOR_CF("NO_NOTIFICATIONS_FAILED", "Non ci sono notifiche non consegnate per questo codice fiscale", 10),
    TRANSACTIONS_NOT_FOUND_FOR_CF("TRANSACTIONS_NOT_FOUND_FOR_CF", "Non ci sono transazioni per questo codice fiscale", 99),
    DATE_VALIDATION_ERROR("DATE_VALIDATION_ERROR", "Le date non sono compatibili", 99),
    DUPLICATE_REQUEST("DUPLICATE_REQUEST", "Richiesta Duplicata. il file inviato è già in fase di elaborazione", 99),
    PENDING_REQUEST("PENDING_REQUEST", "Una precedente richiesta di import è ancora in corso", 99),
    MISSING_REQUIRED_PARAMETER("MISSING_REQUIRED_PARAMETER", "RequestId e CxId sono obbligatori", 99),
    IMPORT_REQUEST_NOT_FOUND("IMPORT_REQUEST_NOT_FOUND", "Richiesta di import non trovata", 99),
    RADD_REGISTRY_NOT_FOUND("RADD_REGISTRY_NOT_FOUND","Punto di ritiro SEND non trovato",99 ),
    RADD_REGISTRY_UNSUPPORTED_LAST_EVALUATED_KEY("RADD_REGISTRY_UNSUPPORTED_LAST_EVALUATED_KEY", "Ultima chiave di paginazione non supportata.", 99),
    PHONE_NUMBER_ERROR("PHONE_NUMBER_ERROR","Il formato del numero di telefono non è corretto", 99),
    GEOLOCATION_ERROR("GEOLOCATION_ERROR","Il formato della geolocalizzazione non è corretto", 99),
    OPENING_TIME_ERROR("OPENING_TIME_ERROR","Il formato dell'orario di apertura non è corretto", 99),
    CAPACITY_ERROR("CAPACITY_ERROR","Il formato della capacità non è corretto", 99),
    ERROR_CODE_PN_RADD_ALT_UNSUPPORTED_LAST_EVALUATED_KEY("PN_RADD_ALT_UNSUPPORTED_LAST_EVALUATED_KEY", "Impossibile deserializzare la lastEvaluatedKey", 99);


    private final String title;
    private final String message;
    private final Integer code;

    ExceptionTypeEnum(String title, String message, Integer code) {
        this.title = title;
        this.message = message;
        this.code = code;
    }
}
