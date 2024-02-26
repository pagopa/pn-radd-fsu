package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DownloadUrl;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.StartTransactionResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.StartTransactionResponseStatus;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.pojo.DocumentTypeEnum;
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

    public static StartTransactionResponse fromResult(List<DownloadUrl> result, String operationType, String operationId, String pnRaddAltBasepath, List<DocumentTypeEnum> documentTypeEnumFilter) {
        StartTransactionResponse response = new StartTransactionResponse();
        DownloadUrl firstDownloadUrl = getDocumentDownloadUrl(pnRaddAltBasepath, operationType, operationId, null, DocumentTypeEnum.COVER_FILE.name());
        result.add(0, firstDownloadUrl);
        StartTransactionResponseStatus status = new StartTransactionResponseStatus();
        status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_0);
        response.setStatus(status);
        status.setMessage(Const.OK);

        result = filterByAndMapDocumentType(result, documentTypeEnumFilter);
        response.setDownloadUrlList(result);

        return response;
    }

    @NotNull
    private static List<DownloadUrl> filterByAndMapDocumentType(List<DownloadUrl> result, List<DocumentTypeEnum> documentTypeEnumFilter) {
        result = result.stream()
                .filter(downloadUrl -> !documentTypeEnumFilter.contains(DocumentTypeEnum.valueOf(downloadUrl.getDocumentType())))
                .map(downloadUrl -> {
                            downloadUrl.setDocumentType(DocumentTypeEnum.valueOf(downloadUrl.getDocumentType()).getValue());
                            return downloadUrl;
                        }
                )
                .toList();
        return result;
    }

    @NotNull
    public static List<DownloadUrl> getDownloadUrls(List<String> result) {
        List<DownloadUrl> downloadUrlList = new ArrayList<>();
        downloadUrlList.addAll(result.stream().map(url -> {
            DownloadUrl downloadUrlItem = new DownloadUrl();
            downloadUrlItem.setUrl(url);
            downloadUrlItem.setNeedAuthentication(false);
            downloadUrlItem.setDocumentType(DocumentTypeEnum.AAR.name());
            return downloadUrlItem;
        }).toList());

        return downloadUrlList;
    }


    public static StartTransactionResponse fromException(RaddGenericException ex) {
        StartTransactionResponse response = new StartTransactionResponse();
        StartTransactionResponseStatus status = new StartTransactionResponseStatus();
        status.setMessage(ex.getExceptionType().getMessage());
        if (ex.getExceptionType() == ExceptionTypeEnum.RETRY_AFTER) {
            status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_2);
            status.setRetryAfter((BigDecimal) ex.getExtra());
        } else if (ex.getExceptionType() == ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF
                || ex.getExceptionType() == ExceptionTypeEnum.INVALID_INPUT) {
            status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_10);
        } else if (ex.getExceptionType() == ExceptionTypeEnum.ALREADY_COMPLETE_PRINT) {
            status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_3);
        } else if (ex.getExceptionType() == ExceptionTypeEnum.TRANSACTION_ALREADY_EXIST) {
            status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_5);
        } else if (ex.getExceptionType() == ExceptionTypeEnum.NOTIFICATION_CANCELLED) {
            status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_80);
        } else if (ex.getExceptionType() == ExceptionTypeEnum.DOCUMENT_UNAVAILABLE) {
            status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_4);
        } else {
            status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_99);
        }

        response.setStatus(status);
        return response;
    }
}