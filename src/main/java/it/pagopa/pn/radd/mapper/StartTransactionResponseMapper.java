package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DownloadUrl;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.StartTransactionResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.StartTransactionResponseStatus;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.utils.Const;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.radd.utils.Utils.getDocumentDownloadUrl;

public class StartTransactionResponseMapper {

    private StartTransactionResponseMapper() {
        // do nothing
    }

    public static StartTransactionResponse fromResult(List<DownloadUrl> result, String operationType, String operationId, String pnRaddAltBasepath) {
        StartTransactionResponse response = new StartTransactionResponse();
        DownloadUrl firstDownloadUrl = getDocumentDownloadUrl(pnRaddAltBasepath, operationType, operationId, null, DownloadUrl.DocumentTypeEnum.COVER_FILE);
        result.add(0, firstDownloadUrl);
        response.setDownloadUrlList(result);
        StartTransactionResponseStatus status = new StartTransactionResponseStatus();
        status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_0);
        response.setStatus(status);
        status.setMessage(Const.OK);
        return response;
    }

    @NotNull
    public static List<DownloadUrl> getDownloadUrls(List<String> result) {
        List<DownloadUrl> downloadUrlList = new ArrayList<>();
        downloadUrlList.addAll(result.stream().map(url -> {
            DownloadUrl downloadUrlItem = new DownloadUrl();
            downloadUrlItem.setUrl(url);
            downloadUrlItem.setNeedAuthentication(false);
            downloadUrlItem.setDocumentType(DownloadUrl.DocumentTypeEnum.AAR);
            return downloadUrlItem;
        }).toList());

        return downloadUrlList;
    }


    public static StartTransactionResponse fromException(RaddGenericException ex) {
        StartTransactionResponse response = new StartTransactionResponse();
        StartTransactionResponseStatus status = new StartTransactionResponseStatus();
        status.setMessage(ex.getExceptionType().getMessage());
        status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_99);
        if (ex.getExceptionType() == ExceptionTypeEnum.RETRY_AFTER) {
            status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_2);
            status.setRetryAfter((BigDecimal) ex.getExtra());
        }
        response.setStatus(status);
        return response;
    }

}