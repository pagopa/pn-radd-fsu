package it.pagopa.pn.radd.utils;


import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DownloadUrl;
import org.apache.commons.lang3.StringUtils;

import static it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity.ITEMS_SEPARATOR;
import static it.pagopa.pn.radd.utils.Const.DOWNLOAD_COVER_FILE_PATH;
import static it.pagopa.pn.radd.utils.OperationTypeEnum.ACT;
import static it.pagopa.pn.radd.utils.OperationTypeEnum.AOR;

public class Utils {

    private Utils() {
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
        if(attachmentId != null) {
            url = url + "?attachmentId=" + attachmentId;
        }
        downloadUrl.setUrl(url);
        downloadUrl.setNeedAuthentication(true);
        downloadUrl.setDocumentType(documentType);

        return downloadUrl;
    }
}
