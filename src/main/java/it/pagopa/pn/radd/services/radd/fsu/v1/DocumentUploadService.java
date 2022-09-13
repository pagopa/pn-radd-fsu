package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DocumentUploadService {

    private final PnSafeStorageClient pnSafeStorageClient;


    public DocumentUploadService(PnSafeStorageClient pnSafeStorageClient) {
        this.pnSafeStorageClient = pnSafeStorageClient;
    }

    public Mono<DocumentUploadResponse> createFile(String contentType, Mono<DocumentUploadRequest> documentUploadRequest) {
        // retrieve presigned url
        return Mono.just(new DocumentUploadResponse())
                .zipWith(pnSafeStorageClient.createFile(contentType, "documentUploadRequest."))
                .map(item -> {
                    FileCreationResponseDto response = item.getT2();
                    log.info("Response iun : {}", response.getUploadUrl());
                    return item.getT1();
                });

    }

}
