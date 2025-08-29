package it.pagopa.pn.radd.utils;


import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DownloadUrl;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity.ITEMS_SEPARATOR;
import static it.pagopa.pn.radd.utils.Const.DOWNLOAD_COVER_FILE_PATH;
import static it.pagopa.pn.radd.utils.OperationTypeEnum.ACT;
import static it.pagopa.pn.radd.utils.OperationTypeEnum.AOR;

public class Utils {

    private Utils() {
    }

    public static String generateUUIDFromString(String input) {
        try {
            // Hash SHA-1 dei dati della richiesta
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            bytes[6] &= 0x0f;  // clear version
            bytes[6] |= 0x50;  // set to version 5
            bytes[8] &= 0x3f;  // clear variant
            bytes[8] |= (byte) 0x80;  // set to IETF variant

            long msb = 0;
            long lsb = 0;
            for (int i = 0; i < 8; i++) {
                msb = (msb << 8) | (bytes[i] & 0xff);
            }
            for (int i = 8; i < 16; i++) {
                lsb = (lsb << 8) | (bytes[i] & 0xff);
            }

            return new UUID(msb, lsb).toString();
        } catch (Exception e) {
            throw new RuntimeException("Errore nella generazione del UUID", e);
        }
    }

    public static boolean checkPersonType(String personType) {
        return StringUtils.equals(personType, Const.PF) || StringUtils.equals(personType, Const.PG);
    }

    public static boolean checkOperationType(String operationType) {
        return StringUtils.equals(operationType, ACT.name()) || StringUtils.equals(operationType, AOR.name());
    }

    public static String transactionIdBuilder(CxTypeAuthFleet cxTypeAuthFleet, String xPagopaPnCxId, String operationId) {
        return cxTypeAuthFleet.getValue() + ITEMS_SEPARATOR + xPagopaPnCxId + ITEMS_SEPARATOR + operationId;
    }

    public static DownloadUrl getDocumentDownloadUrl(String pnRaddAltBasepath, String operationType, String operationId, String attachmentId, String documentType) {
        DownloadUrl downloadUrl = new DownloadUrl();
        String url = pnRaddAltBasepath + DOWNLOAD_COVER_FILE_PATH.replace("{operationType}", operationType).replace("{operationId}", operationId);
        if (attachmentId != null) {
            url = url + "?attachmentId=" + attachmentId;
        }
        downloadUrl.setUrl(url);
        downloadUrl.setNeedAuthentication(true);
        downloadUrl.setDocumentType(documentType);

        return downloadUrl;
    }

    public static String getFileKeyFromPresignedUrl(String presignedUrl) {
        Pattern FILEKEY_IN_PRESIGNED_URL = Pattern.compile("(.*safestorage.*/)(.*)(\\?.*)");

        Matcher matcher = FILEKEY_IN_PRESIGNED_URL.matcher(presignedUrl);
        if (matcher.find()) {
            return matcher.group(2);
        }

        Pattern ZIP_LEGAL_FACT = Pattern.compile("download/(ACT|AOR)/.*(\\?attachmentId=)");
        matcher = ZIP_LEGAL_FACT.matcher(presignedUrl);
        if (matcher.find()) {
            return "zipUrl";
        }

        Pattern COVERFILE = Pattern.compile("download/(ACT|AOR)/.*");
        matcher = COVERFILE.matcher(presignedUrl);
        if (matcher.find()) {
            return "coverFileUrl";
        }

        return "";
    }

    public static void matchRegex(String regex, String value, ExceptionTypeEnum exceptionTypeEnum) {
        if (!StringUtils.isEmpty(value) && !Pattern.compile(regex).matcher(value).matches()) {
            throw new RaddGenericException(exceptionTypeEnum, HttpStatus.BAD_REQUEST);
        }
    }
}
