package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DocumentUploadResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.ResponseStatus;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.utils.Const;

public class DocumentUploadResponseMapper {

    private DocumentUploadResponseMapper () {
        // do nothing
    }

    public static DocumentUploadResponse fromResult(FileCreationResponseDto file){
        DocumentUploadResponse resp = new DocumentUploadResponse();
        resp.setUrl(file.getUploadUrl());
        resp.setFileKey(file.getKey());
        resp.setSecret(file.getSecret());
        ResponseStatus status = new ResponseStatus();
        status.code(ResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage(Const.OK);
        resp.setStatus(status);
        return resp;
    }


    public static DocumentUploadResponse fromException(RaddGenericException ex){
        DocumentUploadResponse resp = new DocumentUploadResponse();
        ResponseStatus status = new ResponseStatus();
        status.setMessage(ex.getExceptionType().getMessage());
        status.code(ResponseStatus.CodeEnum.NUMBER_99);
        resp.setStatus(status);
        return resp;
    }
}
