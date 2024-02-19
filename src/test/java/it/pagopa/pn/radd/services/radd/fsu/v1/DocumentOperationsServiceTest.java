package it.pagopa.pn.radd.services.radd.fsu.v1;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DOCUMENT_UPLOAD_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.NotificationRecipientV23Dto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV23Dto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DocumentUploadRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DocumentUploadResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.ResponseStatus;
import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.PnException;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.db.impl.RaddTransactionDAOImpl;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.PdfGenerator;

import java.io.IOException;
import java.util.HexFormat;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
class DocumentOperationsServiceTest extends BaseTest {
    @InjectMocks
    DocumentOperationsService documentOperationsService;

    @Mock
    PnDeliveryClient pnDeliveryClient;

    @Mock
    PnSafeStorageClient pnSafeStorageClient;

    @Mock
    RaddTransactionDAOImpl raddTransactionDAOImpl;

    @Mock
    PdfGenerator pdfGenerator;

    @Test
    void documentDownloadTest() throws IOException {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.STARTED);

        SentNotificationV23Dto sentNotificationV21Dto = new SentNotificationV23Dto();
        NotificationRecipientV23Dto notificationRecipientV21Dto = new NotificationRecipientV23Dto();
        notificationRecipientV21Dto.setInternalId("123");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        byte[] response = new byte[0];
        byte[] responseHex = HexFormat.of().parseHex(Hex.encodeHexString(response));

        when(raddTransactionDAOImpl.getTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        when(pnDeliveryClient.getNotifications(any())).thenReturn(Mono.just(sentNotificationV21Dto));
        when(pdfGenerator.generateCoverFile(any())).thenReturn(response);

        StepVerifier.create(documentOperationsService.documentDownload("ACT", "ACT", CxTypeAuthFleet.PF, "cxId", "123"))
                .expectNext(responseHex)
                .verifyComplete();

    }

    @Test
    void documentDownloadValidateErrorTest() throws IOException {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.STARTED);

        SentNotificationV23Dto sentNotificationV21Dto = new SentNotificationV23Dto();
        NotificationRecipientV23Dto notificationRecipientV21Dto = new NotificationRecipientV23Dto();
        notificationRecipientV21Dto.setInternalId("123");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        byte[] response = new byte[0];

        when(raddTransactionDAOImpl.getTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        when(pnDeliveryClient.getNotifications(any())).thenReturn(Mono.just(sentNotificationV21Dto));
        when(pdfGenerator.generateCoverFile(any())).thenReturn(response);

        StepVerifier.create(documentOperationsService.documentDownload("", "ACT", CxTypeAuthFleet.PF, "cxId", "123"))
                .expectError()
                .verify();

    }

    @Test
    void documentDownloadValidateErrorTest2() throws IOException {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.STARTED);

        SentNotificationV23Dto sentNotificationV21Dto = new SentNotificationV23Dto();
        NotificationRecipientV23Dto notificationRecipientV21Dto = new NotificationRecipientV23Dto();
        notificationRecipientV21Dto.setInternalId("123");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        byte[] response = new byte[0];

        when(raddTransactionDAOImpl.getTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        when(pnDeliveryClient.getNotifications(any())).thenReturn(Mono.just(sentNotificationV21Dto));
        when(pdfGenerator.generateCoverFile(any())).thenReturn(response);

        StepVerifier.create(documentOperationsService.documentDownload("ACT", "", CxTypeAuthFleet.PF, "cxId", "123"))
                .expectError()
                .verify();

    }

    @Test
    void documentDownloadAbortedStatusErrorTest() throws IOException {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.ABORTED);

        SentNotificationV23Dto sentNotificationV21Dto = new SentNotificationV23Dto();
        NotificationRecipientV23Dto notificationRecipientV21Dto = new NotificationRecipientV23Dto();
        notificationRecipientV21Dto.setInternalId("123");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        byte[] response = new byte[0];

        when(raddTransactionDAOImpl.getTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        when(pnDeliveryClient.getNotifications(any())).thenReturn(Mono.just(sentNotificationV21Dto));
        when(pdfGenerator.generateCoverFile(any())).thenReturn(response);

        StepVerifier.create(documentOperationsService.documentDownload("ACT", "ACT", CxTypeAuthFleet.PF, "cxId", "123"))
                .expectError()
                .verify();

    }

    @Test
    void documentDownloadPdfGeneratorErrorTest() throws IOException {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.STARTED);

        SentNotificationV23Dto sentNotificationV21Dto = new SentNotificationV23Dto();
        NotificationRecipientV23Dto notificationRecipientV21Dto = new NotificationRecipientV23Dto();
        notificationRecipientV21Dto.setInternalId("123");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        byte[] response = new byte[0];

        when(raddTransactionDAOImpl.getTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        when(pnDeliveryClient.getNotifications(any())).thenReturn(Mono.just(sentNotificationV21Dto));
        when(pdfGenerator.generateCoverFile(any())).thenThrow(IOException.class);

        StepVerifier.create(documentOperationsService.documentDownload("ACT", "ACT", CxTypeAuthFleet.PF, "cxId", "123"))
                .expectError()
                .verify();

    }

    @Test
    void documentDownloadNoRecipientErrorTest() throws IOException {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.STARTED);

        SentNotificationV23Dto sentNotificationV21Dto = new SentNotificationV23Dto();
        NotificationRecipientV23Dto notificationRecipientV21Dto = new NotificationRecipientV23Dto();
        notificationRecipientV21Dto.setInternalId("234");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        byte[] response = new byte[0];

        when(raddTransactionDAOImpl.getTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        when(pnDeliveryClient.getNotifications(any())).thenReturn(Mono.just(sentNotificationV21Dto));
        when(pdfGenerator.generateCoverFile(any())).thenThrow(IOException.class);

        StepVerifier.create(documentOperationsService.documentDownload("ACT", "ACT", CxTypeAuthFleet.PF, "cxId", "123"))
                .expectError()
                .verify();

    }

    @Test
    @Disabled
    void testWhenIdAndBoundleIdERROR99(){
        String id="idTest";
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;

        FileCreationRequestDto request = new FileCreationRequestDto();
        request.setContentType("zip");
        Mockito.when(pnSafeStorageClient.createFile( any(), anyString())
        ).thenReturn(Mono.error(new PnException("Errore", "99")));
        Mono<DocumentUploadResponse> response = documentOperationsService.createFile(id, Mono.just(bundleId) );
        response.onErrorResume( ex ->{
                    log.info(ex.getMessage());
                    assertNotNull(ex);
                    assertNotNull(ex.getMessage());
                    assertEquals( ResponseStatus.CodeEnum.NUMBER_99.toString()  , ((PnException) ex).getDescription());
                    return Mono.empty();
                }
        ).block();
    }

    @Test
    void testWhenIdAndBoundleKO(){
        String id="idTest";
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        Mockito.when(pnSafeStorageClient.createFile( any(), any())
        ).thenReturn(Mono.error( new RaddGenericException(DOCUMENT_UPLOAD_ERROR)));
        Mono<DocumentUploadResponse> response = documentOperationsService.createFile(id, Mono.just(bundleId) );
        response.onErrorResume(ex ->{
            if (ex instanceof RaddGenericException){
                log.info(((RaddGenericException) ex).getExceptionType().getMessage());
                assertNotNull(ex);
                assertNotNull(((RaddGenericException) ex).getExceptionType());
                assertEquals( DOCUMENT_UPLOAD_ERROR  , ((RaddGenericException) ex).getExceptionType());
                return Mono.empty();
            }
            fail("Other exception");
            return null;
        }).block();
    }


    @Test
    void testWhenIdAndBoundleId(){
        String id="idTest";
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        FileCreationResponseDto fileCreationResponseDto = mock(FileCreationResponseDto.class);
        fileCreationResponseDto.setUploadUrl("testUrl");
        Mockito.when(pnSafeStorageClient.createFile(Mockito.any(), Mockito.any())
        ).thenReturn( Mono.just(fileCreationResponseDto) );
        DocumentUploadResponse response = documentOperationsService.createFile(id, Mono.just(bundleId) ).block();
        assertNotNull(response);
        assertEquals(ResponseStatus.CodeEnum.NUMBER_0, response.getStatus().getCode());
    }


    @Test
    @Disabled
    void testWhenIdAndBoundleIdIsEmpty(){
        String id="";
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        Mono <DocumentUploadResponse> response = documentOperationsService.createFile(id, Mono.just(bundleId) );
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni valori non sono valorizzati", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    @Disabled
    void testWhenContentTypeIsNull(){
        DocumentUploadRequest documentUploadRequest=new DocumentUploadRequest();
        Mono <DocumentUploadResponse> response = documentOperationsService.createFile("test", Mono.just(documentUploadRequest));
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni valori non sono valorizzati", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    @Disabled
    void testWhenBundleIdIsNull(){
        DocumentUploadRequest documentUploadRequest=new DocumentUploadRequest();
        Mono <DocumentUploadResponse> response = documentOperationsService.createFile("test", Mono.just(documentUploadRequest));
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni valori non sono valorizzati", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    @Disabled
    void testWhenContentTypeIsEmpty(){
        DocumentUploadRequest documentUploadRequest=new DocumentUploadRequest();
        Mono <DocumentUploadResponse> response = documentOperationsService.createFile("test", Mono.just(documentUploadRequest));
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni valori non sono valorizzati", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    @Disabled
    void testWhenBundleIdIsEmpty(){
        DocumentUploadRequest documentUploadRequest=new DocumentUploadRequest();
        Mono <DocumentUploadResponse> response = documentOperationsService.createFile("test", Mono.just(documentUploadRequest));
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni valori non sono valorizzati", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    void testWhenRequestIsNull(){

        Mono <DocumentUploadResponse> response = documentOperationsService.createFile("test", null);
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Body non valido", exception.getMessage());
            return Mono.empty();
        }).block();

    }

}

