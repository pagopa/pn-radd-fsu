package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.PnSafeStorageException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileDownloadResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.OperationResultCodeResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DOCUMENT_UPLOAD_ERROR;
import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.RETRY_AFTER;
import static org.junit.jupiter.api.Assertions.*;

class PnSafeStorageClientTest extends BaseTest.WithMockServer {

    @Autowired
    private PnSafeStorageClient pnSafeStorageClient;

    @Test
    void testCreateFile() {
        String contentType = "application/pdf", operationId = "operationId", checksumValue = "708F4C8216F30FA6007F8E2F316ECC935D94057202FC5D8008BCCC118EA12560";
        Mono<FileCreationResponseDto> monoResponse =  pnSafeStorageClient.createFile(contentType, operationId, checksumValue);
        monoResponse.map(response -> {
            assertEquals("http://localhost:1080/safe-storage/storage/unFile", response.getUploadUrl());
            assertEquals("AZ23RF12", response.getSecret());
            assertEquals("8F7E/9A3B/1234/AB87", response.getKey());
            assertEquals("PUT", response.getUploadMethod().getValue());
            return Mono.empty();
        }).block();
    }

    @Test
    void testCreateFileCode404() {
        String contentType = "application/json", operationId = "operationId", checksumValue = "708F4C8216F30FA6007F8E2F316ECC935D94057202FC5D8008BCCC118EA12560";
        Mono<FileCreationResponseDto> monoResponse =  pnSafeStorageClient.createFile(contentType, operationId, checksumValue);
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
        String fileKey = "AB87";
        Mono<FileDownloadResponseDto> monoResponse = pnSafeStorageClient.getFile(fileKey);
        monoResponse.map(response -> {
            assertEquals("random/path/of/the/file", response.getKey());
            assertEquals("3Z9SdhZ50PBeIj617KEMrztNKDMJj8FZ", response.getVersionId());
            assertEquals(new BigDecimal(3028), response.getContentLength());
            assertEquals("PN_LEGALFACT", response.getDocumentType());
            //Da decommentare dopo l'aggiornamento ss
            //assertEquals("PRELOADED", response.getDocumentStatus());
            return Mono.empty();
        }).block();
    }

    @Test
    void testGetFileForDownloadCode404() {
        String fileKey = "ABC";
        Mono<FileDownloadResponseDto> monoResponse = pnSafeStorageClient.getFile(fileKey);
        monoResponse.onErrorResume(exception -> {
            if (exception instanceof RaddGenericException){
                assertNull(((RaddGenericException) exception).getMessage());
                return Mono.empty();
            }
            fail("Badly type exception");
            return null;
        }).block();
    }

    @Test
    void testGetFileForGlacier() {
        String fileKey = "AB49";
        Mono<FileDownloadResponseDto> monoResponse = pnSafeStorageClient.getFile(fileKey);
        monoResponse.map(response -> {
            if(response.getDownload() != null) {
                assertEquals("https://presignedurldemo.s3.eu-west-2.amazonaws.com/", response.getDownload().getUrl());
                assertEquals("2032-04-12T12:32:04Z", response.getRetentionUntil().toString());
                assertEquals("jezIVxlG1M1woCSUngM6KipUN3/p8cG5RMIPnuEanlE=", response.getChecksum());
                assertEquals("PN_LEGALFACT", response.getDocumentType());
                //Da decommentare dopo l'aggiornamento ss
                //assertEquals("PRELOADED", response.getDocumentStatus());

            }
            return Mono.empty();
        }).block();
    }

    @Test
    void testGetFileForGlacierCode404() {
        String fileKey = "XYZ";
        Mono<FileDownloadResponseDto> monoResponse = pnSafeStorageClient.getFile(fileKey);
        monoResponse.onErrorResume(exception -> {
            if (exception instanceof RaddGenericException){
                assertNull(((RaddGenericException) exception).getMessage());
                return Mono.empty();
            }
            fail("Badly type exception");
            return null;
        }).block();
    }

    @Test
    void testUpdateFileMetadata() {
        String fileKey = "8F7E/9A3B";
        Mono<OperationResultCodeResponseDto> monoResponse = pnSafeStorageClient.updateFileMetadata(fileKey);
        monoResponse.map(response -> {
            assertEquals("200", response.getResultCode());
            assertEquals("Il file non è stato trovato.", response.getResultDescription());
            if(response.getErrorList() != null) {
                assertEquals("retentionDate cannot be anticipated", response.getErrorList().get(0));
            }
            return Mono.empty();
        });
    }

    @Test
    void testUpdateFileMetadataCode404() {
        String fileKey = "8F7E";
        Mono<OperationResultCodeResponseDto> monoResponse = pnSafeStorageClient.updateFileMetadata(fileKey);
        monoResponse.onErrorResume(PnSafeStorageException.class, exception -> {
            assertEquals(404, exception.getWebClientEx().getStatusCode().value());
            return Mono.empty();
        }).block();
    }
}
