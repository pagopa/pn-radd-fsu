package it.pagopa.pn.radd.utils;

public class Const {

    public static final String OK = "OK";
    public static final String KO = "KO";

    public static final String PF = "PF";
    public static final String PG = "PG";

    //Status document
    public static final String DOCUMENT_TYPE = "PN_RADD_ALT_ATTACHMENT";
    public static final String PRELOADED = "PRELOADED";

    public static final String SAVED = "SAVED";
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
    public static final String START_ACT_INQUIRY = "Start ACT Inquiry";
    public static final String START_AOR_INQUIRY = "Start AOR Inquiry";
    public static final String END_ACT_INQUIRY = "End ACT Inquiry";
    public static final String END_AOR_INQUIRY = "End AOR Inquiry";
    public static final String END_ACT_INQUIRY_WITH_ERROR = "End ACT Inquiry with error {}";
    public static final String END_AOR_INQUIRY_WITH_ERROR = "End AOR Inquiry with error {}";

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

    public static final String CONTENT_TYPE_TEXT_CSV = "text/csv";
    public static final String DOWNLOAD_COVER_FILE_PATH = "/radd-net/api/v1/download/{operationType}/{operationId}";
    public static final String ZIP_ATTACHMENT_URL_NOT_FOUND = "ZIP ATTACHMENT URL NOT FOUND";

    /* ActService */
    public static final String START_ACT_START_TRANSACTION = "Start ACT startTransaction";
    public static final String START_ACT_COMPLETE_TRANSACTION = "Start ACT completeTransaction";
    public static final String START_ACT_ABORT_TRANSACTION = "Start ACT abortTransaction";
    public static final String END_ACT_START_TRANSACTION = "End ACT starTransaction";
    public static final String END_ACT_START_TRANSACTION_WITH_ERROR = "End ACT startTransaction with error {}";
    public static final String END_ACT_ABORT_TRANSACTION = "End ACT abortTransaction";
    public static final String END_ACT_ABORT_TRANSACTION_WITH_ERROR = "End ACT abortTransaction with error {}";
    public static final String END_ACT_COMPLETE_TRANSACTION = "End ACT completeTransaction";
    public static final String END_ACT_COMPLETE_TRANSACTION_WITH_ERROR = "End ACT completeTransaction with error {}";
    public static final String CONTENT_TYPE_PDF = "application/pdf";
    public static final String SAFESTORAGE_PREFIX = "safestorage://";


    public static final String REMOVED_FROM_LATEST_IMPORT = "Removed from latest import";

    /* AorService */
    public static final String START_AOR_START_TRANSACTION = "Start AOR startTransaction";
    public static final String END_AOR_START_TRANSACTION = "End AOR starTransaction";
    public static final String END_AOR_START_TRANSACTION_WITH_ERROR = "End AOR startTransaction with error {}";
    public static final String START_AOR_ABORT_TRANSACTION = "Start AOR abortTransaction";
    public static final String END_AOR_ABORT_TRANSACTION = "End AOR abortTransaction";
    public static final String END_AOR_ABORT_TRANSACTION_WITH_ERROR = "End AOR abortTransaction with error {}";
    public static final String START_AOR_COMPLETE_TRANSACTION = "Start AOR completeTransaction";
    public static final String END_AOR_COMPLETE_TRANSACTION = "End AOR completeTransaction";
    public static final String END_AOR_COMPLETE_TRANSACTION_WITH_ERROR = "End AOR completeTransaction with error {}";


    /* Document type external mapping */
    public static final String ATTO_NOTIFICATO = "ATTO_NOTIFICATO";
    public static final String ATTESTAZIONE_OPPONIBILE_A_TERZI = "ATTESTAZIONE_OPPONIBILE_A_TERZI";

    public static final String ERROR_CODE_RADD_ALT_EVENTTYPENOTSUPPORTED = "ERROR_CODE_RADD_ALT_EVENTTYPENOTSUPPORTED";

    /* Registry costant */
    public static final String ERROR_DUPLICATE = "Rifiutato in quanto duplicato";

    public static final String REQUEST_ID_PREFIX = "SELF";

    /* event type */

    public static final String CAP_CHECKER_EVENT = "CAP_CHECKER_EVENT";
    public static final String RADD_NORMALIZE_REQUEST = "RADD_NORMALIZE_REQUEST";
    public static final String IMPORT_COMPLETED = "IMPORT_COMPLETED";


    public static final String MISSING_ADDRESS_REQUIRED_FIELD = "I dati relativi a indirizzo, cap, città e provincia sono obbligatori";
    public static final String START_VALIDITY_PARSING_ERROR = "C'è stato un errore nel parsing della data di startValidity";
    public static final String END_VALIDITY_PARSING_ERROR = "C'è stato un errore nel parsing della data di endValidity";
    public static final String MISSING_DESCRIPTION_REQUIRED_FIELD = "Il campo descrizione è obbligatorio";
    public static final String MISSING_PHONE_NUMBER_REQUIRED_FIELD = "Il campo telefono è obbligatorio";
    public static final String WRONG_PHONE_NUMBER_FORMAT = "Il campo telefono non rispetta il formato definito";

    /* BaseService */
    public static final String MISSING_FILE_KEY_REQUIRED = "Campo fileKey obbligatorio mancante";
    public static final String UNEXPECTED_FILE_KEY = "Campo fileKey inaspettato";
    public static final String MISSING_CHECKSUM_REQUIRED = "Campo checksum obbligatorio mancante";
    public static final String UNEXPECTED_CHECKSUM = "Campo checksum inaspettato";



    /* Regex */
    public static final String REGEX_PHONENUMBER = "^[0-9\\W]{0,20}$";
    public static final String REGEX_GEOLOCATION = "^[0-9]{1,3}\\.?[0-9]{1,10}$";
    public static final String REGEX_OPENINGTIME = "^((mon|tue|wed|thu|fri|sat|sun)=([0-9]{1,2}:[0-9]{2}-[0-9]{1,2}:[0-9]{2}(_[0-9]{1,2}:[0-9]{2}-[0-9]{1,2}:[0-9]{2})?#)(?!.*\\2))+$";
    public static final String REGEX_CAPACITY = "^[0-9]{0,10}$";
}
