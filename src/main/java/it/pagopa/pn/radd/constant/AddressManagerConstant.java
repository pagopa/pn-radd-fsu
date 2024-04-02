package it.pagopa.pn.radd.constant;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.NONE)
public class AddressManagerConstant {
	public static final String ADDRESS_NORMALIZER_SYNC = "ADDRESS NORMALIZER SYNC - ";
    public static final String ADDRESS_NORMALIZER_ASYNC = "ADDRESS NORMALIZER ASYNC - ";
	public static final String PN_ADDRESSES_NORMALIZED = "PN_ADDRESSES_NORMALIZED";
	public static final String SAVED = "SAVED";
	public static final String SAFE_STORAGE_URL_PREFIX = "safestorage://";
	public static final String SHA256 = "SHA-256";
	public static final String POSTEL = "POSTEL";
	public static final String AM_NORMALIZE_INPUT_EVENTTYPE = "AM_NORMALIZE_INPUT";
	public static final String AM_POSTEL_CALLBACK_EVENTTYPE = "AM_POSTEL_CALLBACK";
	public static final String CONTENT_TYPE = "text/csv";
	public static final String SAFE_STORAGE_STATUS = "SAVED";
	public static final String DOCUMENT_TYPE = "PN_ADDRESSES_RAW";
	public static final String SYNTAX_ERROR = "Syntax error";
	public static final String SEMANTIC_ERROR = "Semantic error";
	public static final String SYNTAX_ERROR_CODE = "400.01";
	public static final String SEMANTIC_ERROR_CODE = "400.02";
	public static final String PNADDR001_MESSAGE = "Address declared non-mailable by normalizer";
	public static final String PNADDR002_MESSAGE = "Address with not Enabled postalCode / foreignState as destination";
	public static final String RETRY_SUFFIX = ".RETRY_";
	public static final String CONTEXT_BATCH_ID = "batch_id:";
}
