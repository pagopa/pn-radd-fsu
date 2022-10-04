package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileDownloadResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.OperationResultCodeResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DOCUMENT_UPLOAD_ERROR;
import static org.junit.jupiter.api.Assertions.*;

class PnSafeStorageClientTest extends BaseTest {

    @Autowired
    private PnSafeStorageClient pnSafeStorageClient;

    @Test
    void testCreateFile() {
        String contentType = "application/pdf", operationId = "operationId";
        Mono<FileCreationResponseDto> monoResponse =  pnSafeStorageClient.createFile(contentType, operationId);
        monoResponse.doOnNext(response -> {
            assertEquals("http://localhost:1080/safe-storage/storage/unFile", response.getUploadUrl());
            assertEquals("AZ23RF12", response.getSecret());
            assertEquals("8F7E/9A3B/1234/AB87", response.getKey());
        }).block();
    }

    @Test
    void testCreateFileCode404() {
        String contentType = "application/json", operationId = "operationId";
        Mono<FileCreationResponseDto> monoResponse =  pnSafeStorageClient.createFile(contentType, operationId);
        monoResponse.onErrorResume(exception -> {
            if (exception instanceof RaddGenericException){
                assertNotNull(((RaddGenericException) exception).getExceptionType());
                assertEquals(DOCUMENT_UPLOAD_ERROR, ((RaddGenericException) exception).getExceptionType());
                return Mono.empty();
            }
            fail("Badly type exception");
            return null;
        }).block();
    }

    @Test
    void testGetFileForDownload() {
        String fileKey = "8F7E/9A3B/1234/AB87";
        Mono<FileDownloadResponseDto> monoResponse = pnSafeStorageClient.getFile(fileKey);
        monoResponse.doOnNext(response -> {
            assertEquals("random/path/of/the/file", response.getKey());
            assertEquals("3Z9SdhZ50PBeIj617KEMrztNKDMJj8FZ", response.getVersionId());
            assertEquals("3028", response.getContentLength());
            assertEquals("PN_LEGALFACT", response.getDocumentType());
            assertEquals("PRELOADED", response.getDocumentStatus());
        });
    }

    @Test
    void testGetFileForDownloadCode404() {
        String fileKey = "8F7E/9A3B/1234/AB87";
        Mono<FileDownloadResponseDto> monoResponse = pnSafeStorageClient.getFile(fileKey);
        monoResponse.onErrorResume(exception -> {
            if (exception instanceof PnSafeStorageException){
                assertEquals(404, ((PnSafeStorageException) exception).getWebClientEx().getStatusCode().value());
                return Mono.empty();
            }
            fail("Badly type exception");
            return null;
        }).block();
    }

    @Test
    void testGetFileForGlacier() {
        String fileKey = "8F7E/9A3B/1234/AB87";
        Mono<FileDownloadResponseDto> monoResponse = pnSafeStorageClient.getFile(fileKey);
        monoResponse.doOnNext(response -> {
            assertEquals("86400", response.getDownload().getRetryAfter());
        });
    }

    @Test
    void testGetFileForGlacierCode404() {
        String fileKey = "8F7E/9A3B/1234/AB87";
        Mono<FileDownloadResponseDto> monoResponse = pnSafeStorageClient.getFile(fileKey);
        monoResponse.onErrorResume(PnSafeStorageException.class, exception -> {
            assertEquals(404, exception.getWebClientEx().getStatusCode().value());
            return Mono.empty();
        }).block();
    }

    @Test
    void testUpdateFileMetadata() {
        String fileKey = "8F7E/9A3B/1234/AB87";
        Mono<OperationResultCodeResponseDto> monoResponse = pnSafeStorageClient.updateFileMetadata(fileKey);
        monoResponse.doOnNext(response -> {
            assertEquals("200", response.getResultCode());
        });
    }

    @Test
    void testUpdateFileMetadataCode404() {
        String fileKey = "8F7E/9A3B/1234/AB87";
        Mono<OperationResultCodeResponseDto> monoResponse = pnSafeStorageClient.updateFileMetadata(fileKey);
        monoResponse.onErrorResume(PnSafeStorageException.class, exception -> {
            assertEquals(404, exception.getWebClientEx().getStatusCode().value());
            return Mono.empty();
        }).block();
    }
}
