package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.AORInquiryResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


class AorServiceTest extends BaseTest {
    @InjectMocks
    private AorService aorService;
    @Mock
    private PnDeliveryPushClient pnDeliveryPushClient;


    @Test
    void testWhenSearchReturnEmptyThrowException(){
        Mockito.when(pnDeliveryPushClient.getPaperNotificationFailed(Mockito.any())).thenReturn(Flux.just(new ResponsePaperNotificationFailedDtoDto()));
        aorService.aorInquiry("uid", "cf", "type")
                .onErrorResume(ex -> {
                   if (ex instanceof RaddGenericException){
                       assertNotNull(((RaddGenericException) ex).getExceptionType());
                       assertEquals(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILDE_FOR_CF, ((RaddGenericException) ex).getExceptionType());
                   }
                   fail("Bad type exception");
                   return null;
                }).block();
    }

    @Test
    void testWhenSearchListEmptyReturnResponseKO(){
        ResponsePaperNotificationFailedDtoDto response1 = new ResponsePaperNotificationFailedDtoDto();
        response1.setRecipientInternalId("testCF1");

        ResponsePaperNotificationFailedDtoDto response2 = new ResponsePaperNotificationFailedDtoDto();
        response2.setRecipientInternalId("testCF2");

        Mockito.when(pnDeliveryPushClient.getPaperNotificationFailed(Mockito.any())).thenReturn(Flux.just(response1, response2));

        AORInquiryResponse inquiryResponse = aorService.aorInquiry("uid", "CF", "type").block();
        assertNotNull(inquiryResponse);
        assertFalse(inquiryResponse.getResult());
        assertEquals(new BigDecimal(99), inquiryResponse.getStatus().getCode().getValue());
        assertEquals(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILDE_FOR_CF.getMessage(), inquiryResponse.getStatus().getMessage());
    }

    @Test
    void testWhenSearchListReturnOK(){
        ResponsePaperNotificationFailedDtoDto response1 = new ResponsePaperNotificationFailedDtoDto();
        response1.setRecipientInternalId("testCF1");

        ResponsePaperNotificationFailedDtoDto response2 = new ResponsePaperNotificationFailedDtoDto();
        response2.setRecipientInternalId("testCF2");

        Mockito.when(pnDeliveryPushClient.getPaperNotificationFailed(Mockito.any())).thenReturn(Flux.just(response1, response2));

        AORInquiryResponse inquiryResponse = aorService.aorInquiry("uid", "testCF2", "type").block();
        assertNotNull(inquiryResponse);
        assertTrue(inquiryResponse.getResult());
    }

    @Test
    void testWhenRecipientIdIsNullThrowPnInvalidInput(){
        try {
            aorService.aorInquiry("uid", "", "type").block();
        } catch (PnInvalidInputException ex){
            assertNotNull(ex);
            assertEquals("Il campo codice fiscale non è valorizzato", ex.getReason());
            return;
        }
        fail("No PnInvalidInput throw");

    }

}
