package it.pagopa.pn.radd.utils;


import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DownloadUrl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity.ITEMS_SEPARATOR;
import static it.pagopa.pn.radd.utils.Const.DOWNLOAD_COVER_FILE_PATH;
import static it.pagopa.pn.radd.utils.OperationTypeEnum.ACT;
import static it.pagopa.pn.radd.utils.OperationTypeEnum.AOR;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    public static boolean checkPersonType(String personType) {
        logger.debug("Checking person type: {}", personType);
        boolean result = StringUtils.equals(personType, Const.PF) || StringUtils.equals(personType, Const.PG);
        logger.debug("Person type check result: {}", result);
        return result;
    }

    public static boolean checkOperationType(String operationType) {
        logger.debug("Checking operation type: {}", operationType);
        boolean result = StringUtils.equals(operationType, ACT.name()) || StringUtils.equals(operationType, AOR.name());
        logger.debug("Operation type check result: {}", result);
        return result;
    }

    public static String transactionIdBuilder(CxTypeAuthFleet cxTypeAuthFleet, String xPagopaPnCxId, String operationId) {
        String transactionId = cxTypeAuthFleet.getValue() + ITEMS_SEPARATOR + xPagopaPnCxId + ITEMS_SEPARATOR + operationId;
        logger.debug("Built transaction ID: {}", transactionId);
        return transactionId;
    }

    public static DownloadUrl getDocumentDownloadUrl(String pnRaddAltBasepath, String operationType, String operationId, String attachmentId, String documentType) {
        String url = pnRaddAltBasepath + DOWNLOAD_COVER_FILE_PATH.replace("{operationType}", operationType).replace("{operationId}", operationId);
        if (attachmentId != null) {
            url = url + "?attachmentId=" + attachmentId;
        }
        logger.debug("Constructed document download URL: {}", url);

        DownloadUrl downloadUrl = new DownloadUrl();
        downloadUrl.setUrl(url);
        downloadUrl.setNeedAuthentication(true);
        downloadUrl.setDocumentType(documentType);

        return downloadUrl;
    }

    public static String getFileKeyFromPresignedUrl(String presignedUrl) {
        Pattern FILEKEY_IN_PRESIGNED_URL = Pattern.compile("(.*safestorage.*/)(.*)(\\?.*)");

        Matcher matcher = FILEKEY_IN_PRESIGNED_URL.matcher(presignedUrl);
        if (matcher.find()) {
            String fileKey = matcher.group(2);
            logger.debug("Extracted file key {} from presigned URL: {}", presignedUrl, fileKey);
            return fileKey;
        }

        Pattern ZIP_LEGAL_FACT = Pattern.compile("download/(ACT|AOR)/.*(\\?attachmentId=)");
        matcher = ZIP_LEGAL_FACT.matcher(presignedUrl);
        if (matcher.find()) {
            logger.debug("Presigned URL is for a ZIP legal fact download");
            return "zipUrl";
        }

        Pattern COVERFILE = Pattern.compile("download/(ACT|AOR)/.*");
        matcher = COVERFILE.matcher(presignedUrl);
        if (matcher.find()) {
            logger.debug("Presigned URL is for a cover file download");
            return "coverFileUrl";
        }

        logger.debug("Presigned URL does not match any known patterns");
        return "";
    }
}
