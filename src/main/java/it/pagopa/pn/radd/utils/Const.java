package it.pagopa.pn.radd.utils;

public class Const {

    public static final String OK = "OK";
    public static final String KO = "KO";

    public static final String PF = "PF";
    public static final String PG = "PG";

    //Status document
    public static final String DOCUMENT_TYPE = "PN_RADD_ALT_ATTACHMENT";
    public static final String PRELOADED = "PRELOADED";
    public static final String ATTACHED = "ATTACHED";

    //Status transaction entity
    public static final String STARTED = "STARTED";
    public static final String DRAFT = "DRAFT";
    public static final String ERROR = "ERROR";
    public static final String COMPLETED = "COMPLETED";
    public static final String ABORTED = "ABORTED";




    /* Inquiry message */
    public static final String NOT_VALID_QR_CODE = "QrCode non valido";
    public static final String NOT_VALID_FISCAL_CODE = "CF non valido";
    public static final String NOT_FOUND_DOCUMENT = "Documenti non più disponibili";
    public static final String ALREADY_COMPLETE_PRINT = "Stampa già eseguita";

    /* Transaction message */
    public static final String NOT_EXISTS_OPERATION = "Transazione inesistente";
    public static final String ALREADY_COMPLETE_OPERATION = "Transazione già completata";
    public static final String ABORT_OPERATION = "Transazione in stato annullata";

    /* Checksum value */

    public static final String X_CHECKSUM = "SHA-256";

    /* DocumentOperations */
    public static final String ERROR_NO_RECIPIENT = "ERROR_NO_RECIPIENT";
    public static final String ERROR_CODE_RADD_DOCUMENTCOMPOSITIONFAILED = "ERROR_CODE_RADD_DOCUMENTCOMPOSITIONFAILED";
    public static final String MISSING_INPUT_PARAMETERS = "Missing input parameters";
    public static final String CONTENT_TYPE_ZIP = "application/zip";




}
