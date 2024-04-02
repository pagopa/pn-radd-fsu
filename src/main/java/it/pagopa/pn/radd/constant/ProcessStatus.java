package it.pagopa.pn.radd.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessStatus {
    public static final String PROCESS_VERIFY_ADDRESS = "[VERIFY] verify address";
    public static final String PROCESS_SERVICE_DEDUPLICA = "[DEDUPLICA] verify address slave and master";
    public static final String PROCESS_SERVICE_POSTEL_ATTIVAZIONE = "[ACTIVATE_SINI_COMPONENT] activate SINI component";
    public static final String PROCESS_CHECKING_APIKEY = "[CHECKING_APIKEY] verifying if apiKey is present on DynamoDB table";
    public static final String PROCESS_START_WRITING_CSV = "[WRITING_CSV] start writing csv";
    public static final String PROCESS_END_WRITING_CSV = "[WRITING_CSV] end writing csv";
    public static final String PROCESS_SERVICE_SAFE_STORAGE = "[SAFE_STORAGE] safe storage service";
    public static final String PROCESS_SERVICE_UPLOAD_DOWNLOAD_FILE = "[UPLOAD_DOWNLOAD_FILE] upload and download file";
    public static final String PROCESS_SERVICE_POSTEL_CALLBACK = "[POSTEL_CALLBACK] postel callback service";
    public static final String PROCESS_SERVICE_NORMALIZE_ADDRESS = "[NORMALIZE_REQUEST] normalize address request service";

}
