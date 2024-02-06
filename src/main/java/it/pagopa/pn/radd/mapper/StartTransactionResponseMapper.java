package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.rest.radd.v1.dto.DownloadUrl;
import it.pagopa.pn.radd.rest.radd.v1.dto.StartTransactionResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.StartTransactionResponseStatus;
import it.pagopa.pn.radd.utils.Const;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StartTransactionResponseMapper {

    private static final String DOWNLOAD_COVER_FILE_PATH = "/radd-net/api/v1/download/{operationType}/{operationId}";

    private StartTransactionResponseMapper() {
        // do nothing
    }

    public static StartTransactionResponse fromResult(List<String> result, String operationType, String operationId, String pnRaddAltBasepath) {
        StartTransactionResponse response = new StartTransactionResponse();
        List<DownloadUrl> downloadUrlList = getDownloadUrls(result);
        DownloadUrl firstDownloadUrl = getFirstDownloadUrl(pnRaddAltBasepath, operationType, operationId);
        downloadUrlList.add(0, firstDownloadUrl);
        response.setDownloadUrlList(downloadUrlList);
        StartTransactionResponseStatus status = new StartTransactionResponseStatus();
        status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_0);
        response.setStatus(status);
        status.setMessage(Const.OK);
        return response;
    }

    @NotNull
    private static List<DownloadUrl> getDownloadUrls(List<String> result) {
        List<DownloadUrl> downloadUrlList = new ArrayList<>();
        downloadUrlList.addAll(result.stream().map(url -> {
            DownloadUrl downloadUrlItem = new DownloadUrl();
            downloadUrlItem.setUrl(url);
            downloadUrlItem.setNeedAuthentication(false);
            return downloadUrlItem;
        }).toList());

        return downloadUrlList;
    }

    @NotNull
    private static DownloadUrl getFirstDownloadUrl(String pnRaddAltBasepath, String operationType, String operationId) {
        DownloadUrl downloadUrl = new DownloadUrl();
        downloadUrl.setUrl(pnRaddAltBasepath + DOWNLOAD_COVER_FILE_PATH.replace("{operationType}", operationType).replace("{operationId}", operationId));
        downloadUrl.setNeedAuthentication(true);

        return downloadUrl;
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