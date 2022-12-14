package it.pagopa.pn.radd.services.radd.fsu.v1;


import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.mapper.DocumentUploadResponseMapper;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DocumentUploadService {

    private final PnSafeStorageClient pnSafeStorageClient;


    public DocumentUploadService(PnSafeStorageClient pnSafeStorageClient) {
        this.pnSafeStorageClient = pnSafeStorageClient;
    }

    public Mono<DocumentUploadResponse> createFile(String uid, Mono<DocumentUploadRequest> documentUploadRequest) {
       if (documentUploadRequest==null){
            log.error("Missing input parameters");
            return Mono.error( new PnInvalidInputException("Body non valido") );
        }
        // retrieve presigned url
        return documentUploadRequest
                .map(m -> {
                    if ( StringUtils.isEmpty(m.getContentType())
                            || StringUtils.isEmpty(m.getBundleId())) {
                        log.error("Missing input parameters");
                        throw new PnInvalidInputException("Alcuni valori non sono valorizzati");
                    }
                    return m;
                })
                .flatMap(value -> pnSafeStorageClient.createFile(value.getContentType(), value.getBundleId(), value.getChecksum()))
                .map(item -> {
                    log.info("Response presigned url : {}", item.getUploadUrl());
                    return DocumentUploadResponseMapper.fromResult(item);
                }).onErrorResume(RaddGenericException.class, ex -> Mono.just(DocumentUploadResponseMapper.fromException(ex)));
    }

}
