package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.NotificationRecipientV24Dto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV25Dto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DocumentUploadRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DocumentUploadResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.ResponseStatus;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.PnRaddForbiddenException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.exception.TransactionAlreadyExistsException;
import it.pagopa.pn.radd.middleware.db.OperationsIunsDAO;
import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.db.impl.RaddTransactionDAOImpl;
import it.pagopa.pn.radd.middleware.msclient.DocumentDownloadClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.PdfGenerator;
import it.pagopa.pn.radd.utils.RaddRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DOCUMENT_UPLOAD_ERROR;
import static it.pagopa.pn.radd.utils.ZipUtils.extractPdfFromZip;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class DocumentOperationsServiceTest {
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

    @Mock
    DocumentDownloadClient documentDownloadClient;

    @Mock
    OperationsIunsDAO operationsIunsDAO;

    @Mock
    PnRaddFsuConfig pnRaddFsuConfig;

    @Test
    void documentDownloadACTTest() throws IOException {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.STARTED);
        raddTransactionEntity.setIun("123");

        SentNotificationV25Dto sentNotificationV21Dto = new SentNotificationV25Dto();
        NotificationRecipientV24Dto notificationRecipientV21Dto = new NotificationRecipientV24Dto();
        notificationRecipientV21Dto.setInternalId("123");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        byte[] response = new byte[0];
        byte[] responseHex = HexFormat.of().parseHex(Hex.encodeHexString(response));

        when(raddTransactionDAOImpl.getTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        when(pnDeliveryClient.getNotifications(any())).thenReturn(Mono.just(sentNotificationV21Dto));
        when(pdfGenerator.generateCoverFile(any())).thenReturn(response);

        StepVerifier.create(documentOperationsService.documentDownload("ACT", "ACT", CxTypeAuthFleet.PF, "cxId", null))
                .expectNext(responseHex)
                .verifyComplete();

    }

    @Test
    void documentDownloadWithAttachmentIdTest() {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.STARTED);
        raddTransactionEntity.setZipAttachments(Map.of("123", "123"));

        SentNotificationV25Dto sentNotificationV21Dto = new SentNotificationV25Dto();
        NotificationRecipientV24Dto notificationRecipientV21Dto = new NotificationRecipientV24Dto();
        notificationRecipientV21Dto.setInternalId("123");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        byte[] zipFile = getFile();
        byte[] pdfFile = extractPdfFromZip(zipFile);
        byte[] responseHex = HexFormat.of().parseHex(Hex.encodeHexString(pdfFile));

        when(raddTransactionDAOImpl.getTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        when(documentDownloadClient.downloadContent(any())).thenReturn(Mono.just(zipFile));

        StepVerifier.create(documentOperationsService.documentDownload("ACT", "ACT", CxTypeAuthFleet.PF, "cxId", "123"))
                .expectNextMatches(res -> Arrays.equals(res, responseHex))
                .verifyComplete();
    }

    @Test
    void documentDownloadAORTest() throws IOException {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.STARTED);
        raddTransactionEntity.setIun("123");

        SentNotificationV25Dto sentNotificationV21Dto = new SentNotificationV25Dto();
        NotificationRecipientV24Dto notificationRecipientV21Dto = new NotificationRecipientV24Dto();
        notificationRecipientV21Dto.setInternalId("123");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        OperationsIunsEntity operationsIunsEntity = new OperationsIunsEntity();
        operationsIunsEntity.setIun("123");
        operationsIunsEntity.setTransactionId("123");

        byte[] response = new byte[0];
        byte[] responseHex = HexFormat.of().parseHex(Hex.encodeHexString(response));

        when(raddTransactionDAOImpl.getTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        when(pnDeliveryClient.getNotifications(any())).thenReturn(Mono.just(sentNotificationV21Dto));
        when(pdfGenerator.generateCoverFile(any())).thenReturn(response);
        when(operationsIunsDAO.getAllIunsFromTransactionId(any())).thenReturn(Flux.just(operationsIunsEntity));

        StepVerifier.create(documentOperationsService.documentDownload("AOR", "ACT", CxTypeAuthFleet.PF, "cxId", null))
                .expectNext(responseHex)
                .verifyComplete();

    }

    @Test
    void documentDownloadAORNoRecipientTest() {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.STARTED);
        raddTransactionEntity.setIun("123");

        SentNotificationV25Dto sentNotificationV21Dto = new SentNotificationV25Dto();
        NotificationRecipientV24Dto notificationRecipientV21Dto = new NotificationRecipientV24Dto();
        notificationRecipientV21Dto.setInternalId("");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        OperationsIunsEntity operationsIunsEntity = new OperationsIunsEntity();
        operationsIunsEntity.setIun("123");
        operationsIunsEntity.setTransactionId("123");

        when(raddTransactionDAOImpl.getTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        when(pnDeliveryClient.getNotifications(any())).thenReturn(Mono.just(sentNotificationV21Dto));
        when(operationsIunsDAO.getAllIunsFromTransactionId(any())).thenReturn(Flux.just(operationsIunsEntity));

        StepVerifier.create(documentOperationsService.documentDownload("AOR", "ACT", CxTypeAuthFleet.PF, "cxId", null))
                .expectError(RaddGenericException.class)
                .verify();

    }


    private byte[] getFile() {
        try {
            return new ClassPathResource("zip/zip-with-pdf.zip").getInputStream().readAllBytes();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void documentDownloadValidateErrorTest() {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.STARTED);

        SentNotificationV25Dto sentNotificationV21Dto = new SentNotificationV25Dto();
        NotificationRecipientV24Dto notificationRecipientV21Dto = new NotificationRecipientV24Dto();
        notificationRecipientV21Dto.setInternalId("123");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        StepVerifier.create(documentOperationsService.documentDownload("", "ACT", CxTypeAuthFleet.PF, "cxId", "123"))
                .expectError(PnInvalidInputException.class)
                .verify();

    }

    @Test
    void documentDownloadValidateErrorTest2() {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.STARTED);

        SentNotificationV25Dto sentNotificationV21Dto = new SentNotificationV25Dto();
        NotificationRecipientV24Dto notificationRecipientV21Dto = new NotificationRecipientV24Dto();
        notificationRecipientV21Dto.setInternalId("123");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        StepVerifier.create(documentOperationsService.documentDownload("ACT", "", CxTypeAuthFleet.PF, "cxId", "123"))
                .expectError(PnInvalidInputException.class)
                .verify();

    }

    @Test
    void documentDownloadAbortedStatusErrorTest() {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.ABORTED);

        SentNotificationV25Dto sentNotificationV21Dto = new SentNotificationV25Dto();
        NotificationRecipientV24Dto notificationRecipientV21Dto = new NotificationRecipientV24Dto();
        notificationRecipientV21Dto.setInternalId("123");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        when(raddTransactionDAOImpl.getTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        StepVerifier.create(documentOperationsService.documentDownload("ACT", "ACT", CxTypeAuthFleet.PF, "cxId", "123"))
                .expectError(TransactionAlreadyExistsException.class)
                .verify();

    }

    @Test
    void documentDownloadPdfGeneratorErrorTest() {
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setRecipientId("123");
        raddTransactionEntity.setStatus(Const.STARTED);

        SentNotificationV25Dto sentNotificationV21Dto = new SentNotificationV25Dto();
        NotificationRecipientV24Dto notificationRecipientV21Dto = new NotificationRecipientV24Dto();
        notificationRecipientV21Dto.setInternalId("123");
        notificationRecipientV21Dto.setDenomination("denomination");
        sentNotificationV21Dto.setRecipients(List.of(notificationRecipientV21Dto));

        when(raddTransactionDAOImpl.getTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        StepVerifier.create(documentOperationsService.documentDownload("ACT", "ACT", CxTypeAuthFleet.PF, "cxId", "123"))
                .expectError(RaddGenericException.class)
                .verify();

    }


    @Test
    void testWhenIdAndBoundleKO(){
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        Mockito.when(pnSafeStorageClient.createFile( any(), any())
        ).thenReturn(Mono.error( new RaddGenericException(DOCUMENT_UPLOAD_ERROR)));
        when(pnRaddFsuConfig.getSafeStorageDocType()).thenReturn("test");
        Mono<DocumentUploadResponse> response = documentOperationsService.createFile(Mono.just(bundleId),String.valueOf(RaddRole.RADD_UPLOADER));
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
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        FileCreationResponseDto fileCreationResponseDto = mock(FileCreationResponseDto.class);
        fileCreationResponseDto.setUploadUrl("testUrl");
        when(pnRaddFsuConfig.getSafeStorageDocType()).thenReturn("test");
        Mockito.when(pnSafeStorageClient.createFile(Mockito.any(), Mockito.any())
        ).thenReturn( Mono.just(fileCreationResponseDto) );
        DocumentUploadResponse response = documentOperationsService.createFile(Mono.just(bundleId),String.valueOf(RaddRole.RADD_UPLOADER)).block();
        assertNotNull(response);
        assertEquals(ResponseStatus.CodeEnum.NUMBER_0, response.getStatus().getCode());
    }

    @Test
    void testWhenRequestIsNull(){

        Mono <DocumentUploadResponse> response = documentOperationsService.createFile(null, String.valueOf(RaddRole.RADD_UPLOADER));
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Body non valido", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    void testWhenRoleIsInvalidThenThrowsException(){
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        assertThrows(PnRaddForbiddenException.class, () -> documentOperationsService.createFile(Mono.just(bundleId), String.valueOf(RaddRole.RADD_STANDARD)));
    }
    @Test
    void testWhenRoleIsNullThenThrowsException(){
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        assertThrows(PnRaddForbiddenException.class, () -> documentOperationsService.createFile(Mono.just(bundleId), null));
    }

}

