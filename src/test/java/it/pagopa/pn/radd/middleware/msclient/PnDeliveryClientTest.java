package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV23Dto;
import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.INVALID_INPUT;
import static org.junit.jupiter.api.Assertions.*;

class PnDeliveryClientTest extends BaseTest.WithMockServer {

    @Autowired
    private PnDeliveryClient pnDeliveryClient;

    @Test
    void testGetCheckAar() {
        String recipientType = "PF", recipientInternalId = "PF-4fc75df3-0913-407e-bdaa-e50329708b7d", qrCode = "UFVNUS1ETVdHLUhSTFAtMjAyMjA5LVEtMV9GUk1UVFI3Nk0wNkI3MTVFXzVhZGIxMGE2LTM1MDEtNDcyYS04ZTkyLTU3ZGUyYzgxNTZhYw";
        Mono<ResponseCheckAarDtoDto> monoResponse = pnDeliveryClient.getCheckAar(recipientType, recipientInternalId, qrCode);
        monoResponse.map(response -> {
            assertEquals("LJLH-GNTJ-DVXR-202209-J-1", response.getIun());
            return Mono.empty();
        }).block();
    }

    @Test
    void testGetCheckAarErrorCases() {
        String recipientType = "PF", recipientInternalId = "PG-4fc75df3-0913-407e-bdaa-e50329708b7d", qrCode = "UFVNUS1ETVdHLUhSTFAtMjAyMjA5LVEtMV9GUk1UVFI3Nk0wNkI3MTVFXzVhZGIxMGE2LTM1MDEtNDcyYS04ZTkyLTU3ZGUyYzgxNTZhYw";
        Mono<ResponseCheckAarDtoDto> monoResponse1 = pnDeliveryClient.getCheckAar(recipientType, recipientInternalId, qrCode);
        monoResponse1.onErrorResume(RaddGenericException.class, exception -> {
            assertEquals(INVALID_INPUT, exception.getExceptionType());
            return Mono.empty();
        }).block();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        recipientInternalId = "PF-4fc75df3-0913-407e-bdaa-e50329708b7d";
        qrCode = "";
        Mono<ResponseCheckAarDtoDto> monoResponse2 = pnDeliveryClient.getCheckAar(recipientType, recipientInternalId, qrCode);
        monoResponse2.onErrorResume(RaddGenericException.class, exception -> {
            assertEquals(INVALID_INPUT, exception.getExceptionType());
            return Mono.empty();
        }).block();
    }

    @Test
    void testGetNotifications() {
        String iun = "LJLH-GNTJ-DVXR-202209-J-1";
        Mono<SentNotificationV23Dto> monoResponse = pnDeliveryClient.getNotifications(iun);
        monoResponse.map(response -> {
            assertNotEquals(0, response.getRecipients().size());
            assertEquals(iun, response.getIun());
            assertEquals("CFComuneMilano", response.getSenderTaxId());
            assertEquals("comune di milano", response.getSenderDenomination());

            response.getRecipients().forEach(element -> {
                assertEquals("FRMTTR76M06B715E", element.getTaxId());
                assertEquals("Mario Cucumber denomination", element.getDenomination());
                assertNotNull(element.getDigitalDomicile());
                assertNotNull(element.getPhysicalAddress());
                assertNotNull(element.getRecipientType());
            });

            response.getDocuments().forEach(element -> {
                assertEquals("0", element.getDocIdx());
                assertNotNull(element.getDigests());
                assertNotNull(element.getRef());
            });

            return Mono.empty();
        }).block();
    }

    @Test
    void testGetNotificationsCode400() {
        String iun = "LJLH-GNTJ";
        Mono<SentNotificationV23Dto> response = pnDeliveryClient.getNotifications(iun);
        response.onErrorResume(PnRaddException.class, exception -> {
            assertEquals(400, exception.getWebClientEx().getStatusCode().value());
            return Mono.empty();
        }).block();
    }

    @Test
    void testCheckIunAndInternalId() {
        String iun = "LJLH-GNTJ-DVXR-202209-J-1";
        String recipientInternalId = "PF-4fc75df3-0913-407e-bdaa-e50329708b7d";
        Mono<Void> response = pnDeliveryClient.checkIunAndInternalId(iun, recipientInternalId);
        response.map(item -> Mono.empty()).block();
    }

    @Test
    void testCheckIunAndInternalId400() {
        String iun = "LJLH-GNTJ-DVXR-202209-J-1";
        String recipientInternalId = "4fc75df3-0913-407e-bdaa-e50329708b7d";
        Mono<Void> response = pnDeliveryClient.checkIunAndInternalId(iun, recipientInternalId);
        response.onErrorResume(PnRaddException.class, exception -> {
            assertEquals(400, exception.getWebClientEx().getStatusCode().value());
            return Mono.empty();
        }).block();
    }

    @Test
    void testGetPresignedUrlDocument() {
        String iun = "LJLH-GNTJ-DVXR-202209-J-1", docXid = "12984594", recipientTaxId = "65df2qm7y";
        Mono<NotificationAttachmentDownloadMetadataResponseDto> monoResponse = pnDeliveryClient.getPresignedUrlDocument(iun, docXid, recipientTaxId);
        monoResponse.map(response -> {
            assertEquals("D73JG1340FJ3GBFI04NT0B73JV9W7331V", response.getSha256());
            assertEquals("application/pdf", response.getContentType());
            assertEquals(54092, response.getContentLength());
            assertEquals("http://downdocuments", response.getUrl());
            assertEquals(8035, response.getRetryAfter());
            return Mono.empty();
        }).block();
    }

    @Test
    void testGetPresignedUrlDocumentCode400() {
        String iun = "LJLH-GNTJ-DVXR-202209-J-1", docXid = "12984594", recipientTaxId = "";
        Mono<NotificationAttachmentDownloadMetadataResponseDto> response = pnDeliveryClient.getPresignedUrlDocument(iun, docXid, recipientTaxId);
        response.onErrorResume(exception -> {
            if (exception instanceof PnRaddException) {
                assertEquals(HttpStatus.valueOf(400), ((PnRaddException) exception).getWebClientEx().getStatusCode());
                return Mono.empty();
            }
            fail("Type exception bad");
            return null;
        }).block();
    }

    @Test
    void testGetPresignedUrlDocumentCode410() {
        String iun = "LJLH-GONE", docXid = "12984410", recipientTaxId = "1";
        Mono<NotificationAttachmentDownloadMetadataResponseDto> response = pnDeliveryClient.getPresignedUrlDocument(iun, docXid, recipientTaxId);
        response.onErrorResume(exception -> {
            if (exception instanceof RaddGenericException) {
                assertEquals(ExceptionTypeEnum.DOCUMENT_UNAVAILABLE, ((RaddGenericException) exception).getExceptionType());
                return Mono.empty();
            }
            return Mono.empty();
        }).block();
    }

    @Test
    void testGetPresignedUrlPaymentDocument() {
        String iun = "LJLH-GNTJ-DVXR-202209-J-1", attachmentName = "paymentDoc", recipientTaxId = "12df2qm7y";
        Mono<NotificationAttachmentDownloadMetadataResponseDto> monoResponse = pnDeliveryClient.getPresignedUrlPaymentDocument(iun, attachmentName, recipientTaxId, 1);
        monoResponse.map(response -> {
            assertEquals("D73JG1340FJ3GBFI04NT0B73JV9W7331V", response.getSha256());
            assertEquals("application/pdf", response.getContentType());
            assertEquals(54092, response.getContentLength());
            return Mono.empty();
        }).block();
    }

    @Test
    void testGetPresignedUrlPaymentDocumentCode400() {
        String iun = "LJLH-GNTJ-DVXR-202209-J-1", attachmentName = "paymentDoc", recipientTaxId = "";
        Mono<NotificationAttachmentDownloadMetadataResponseDto> response = pnDeliveryClient.getPresignedUrlPaymentDocument(iun, attachmentName, recipientTaxId, null);
        response.onErrorResume(exception -> {
            if (exception instanceof PnRaddException) {
                assertEquals(HttpStatus.valueOf(400), ((PnRaddException) exception).getWebClientEx().getStatusCode());
                return Mono.empty();
            }
            fail("Type exception bad");
            return null;
        }).block();
    }
}
