package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.SentNotificationDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.CF_OR_QRCODE_NOT_VALID;
import static org.junit.jupiter.api.Assertions.*;

class PnDeliveryClientTest extends BaseTest {

    @Autowired
    private PnDeliveryClient pnDeliveryClient;

    @Test
    void testGetCheckAar() {
        String recipientType = "PF", recipientInternalId = "PF-4fc75df3-0913-407e-bdaa-e50329708b7d", qrCode = "UFVNUS1ETVdHLUhSTFAtMjAyMjA5LVEtMV9GUk1UVFI3Nk0wNkI3MTVFXzVhZGIxMGE2LTM1MDEtNDcyYS04ZTkyLTU3ZGUyYzgxNTZhYw";
        Mono<ResponseCheckAarDtoDto> response = pnDeliveryClient.getCheckAar(recipientType, recipientInternalId, qrCode);
        assertEquals("LJLH-GNTJ-DVXR-202209-J-1", response.block().getIun());
    }

    @Test
    void testGetCheckAarCode400() {
        String recipientType = "PF", recipientInternalId = "PG-4fc75df3-0913-407e-bdaa-e50329708b7d", qrCode = "UFVNUS1ETVdHLUhSTFAtMjAyMjA5LVEtMV9GUk1UVFI3Nk0wNkI3MTVFXzVhZGIxMGE2LTM1MDEtNDcyYS04ZTkyLTU3ZGUyYzgxNTZhYw";
        Mono<ResponseCheckAarDtoDto> response = pnDeliveryClient.getCheckAar(recipientType, recipientInternalId, qrCode);
        response.onErrorResume(RaddGenericException.class, exception -> {
            assertEquals(CF_OR_QRCODE_NOT_VALID, exception.getExceptionType());
            return Mono.empty();
        }).block();
    }

    @Test
    void testGetNotifications() {
        String iun = "LJLH-GNTJ-DVXR-202209-J-1";
        Mono<SentNotificationDto> monoResponse = pnDeliveryClient.getNotifications(iun);
        monoResponse.doOnNext(response -> {
            assertEquals(iun, response.getIun());
            assertEquals("CFComuneMilano", response.getSenderTaxId());
            assertEquals("comune di milano", response.getSenderDenomination());
        }).block();
    }

    @Test
    void testGetNotificationsCode400() {
        String iun = "LJLH-GNTJ";
        Mono<SentNotificationDto> response = pnDeliveryClient.getNotifications(iun);
        response.onErrorResume(PnRaddException.class, exception -> {
            assertEquals(400, exception.getWebClientEx().getStatusCode().value());
            return Mono.empty();
        }).block();
    }

    @Test
    void testGetPresignedUrlDocument() {
        String iun = "LJLH-GNTJ-DVXR-202209-J-1", docXid = "12984594", recipientTaxId = "65df2qm7y";
        Mono<NotificationAttachmentDownloadMetadataResponseDto> monoResponse = pnDeliveryClient.getPresignedUrlDocument(iun, docXid, recipientTaxId);
        monoResponse.doOnNext(response -> {
            assertEquals("D73JG1340FJ3GBFI04NT0B73JV9W7331V", response.getSha256());
            assertEquals("application/pdf", response.getContentType());
            assertEquals(54092, response.getContentLength());
        }).block();
    }

    @Test
    void testGetPresignedUrlDocumentCode400() {
        String iun = "LJLH-GNTJ-DVXR-202209-J-1", docXid = "12984594", recipientTaxId = "65df2qm7y";
        Mono<NotificationAttachmentDownloadMetadataResponseDto> response = pnDeliveryClient.getPresignedUrlDocument(iun, docXid, recipientTaxId);

        response.onErrorResume(exception -> {
            if (exception instanceof PnRaddException){
                assertEquals(HttpStatus.valueOf(400), ((PnRaddException) exception).getWebClientEx().getStatusCode());
                return Mono.empty();
            }
            fail("Type exception bad");
            return null;
        }).block();
    }

    @Test
    void testGetPresignedUrlPaymentDocument() {
        String iun = "LJLH-GNTJ-DVXR-202209-J-1", attachmentName = "paymentDoc", recipientTaxId = "12df2qm7y";
        Mono<NotificationAttachmentDownloadMetadataResponseDto> monoResponse = pnDeliveryClient.getPresignedUrlPaymentDocument(iun, attachmentName, recipientTaxId);
        monoResponse.doOnNext(response -> {
            assertEquals("D73JG1340FJ3GBFI04NT0B73JV9W7331V", response.getSha256());
            assertEquals("application/pdf", response.getContentType());
            assertEquals(54092, response.getContentLength());
        }).block();
    }

    @Test
    void testGetPresignedUrlPaymentDocumentCode400() {
        String iun = "LJLH-GNTJ-DVXR-202209-J-1", attachmentName = "paymentDoc", recipientTaxId = "12df2qm7y";
        Mono<NotificationAttachmentDownloadMetadataResponseDto> response = pnDeliveryClient.getPresignedUrlPaymentDocument(iun, attachmentName, recipientTaxId);
        response.onErrorResume(exception -> {
            if (exception instanceof PnRaddException){
                assertEquals(HttpStatus.valueOf(400), ((PnRaddException) exception).getWebClientEx().getStatusCode());
                return Mono.empty();
            }
            fail("Type exception bad");
            return null;
        }).block();
    }
}
