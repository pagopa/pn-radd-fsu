package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class AorServiceTest extends BaseTest {
    private static final String ENSURE_FC = "PF-4fc75df3-0913-407e-bdaa-e50329708b7d";
    @InjectMocks
    private AorService aorService;
    @Mock
    private PnDeliveryPushClient pnDeliveryPushClient;
    @Mock
    private PnDataVaultClient pnDataVaultClient;
    @Mock
    private RaddTransactionDAO raddTransactionDAO;
    private AbortTransactionRequest abortTransactionRequest;
    private CompleteTransactionRequest completeTransactionRequest;
    private RaddTransactionEntity entityComplete;

    @BeforeEach
    public void setUp(){
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any())).thenReturn(Mono.just(ENSURE_FC));
        abortTransactionRequest = new AbortTransactionRequest();
        abortTransactionRequest.setOperationId("1234AOR");
        abortTransactionRequest.setReason("cancelled by user");
        abortTransactionRequest.setOperationDate(new Date());

        completeTransactionRequest = new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("1234AOR");
        completeTransactionRequest.setOperationDate(new Date());

        entityComplete = new RaddTransactionEntity();
        entityComplete.setOperationType("1234AOR");
        entityComplete.setStatus(Const.COMPLETED);
    }

    // AOR START //
    @Test
    void testStartWhenValidateRequestThenThrowInvalidInputException(){
        AorStartTransactionRequest request = new AorStartTransactionRequest();
        StepVerifier.create(aorService.startTransaction("uid", request))
                .expectError(PnInvalidInputException.class).verify();

        request.setOperationId("1234AOR");
        StepVerifier.create(aorService.startTransaction("uid", request))
                .expectError(PnInvalidInputException.class).verify();
    }


    // ---------------- //


    // AOR COMPLETE //
    @Test
    void testCompleteWhenValidateRequestThenThrowInvalidInputException(){
        CompleteTransactionRequest request = new CompleteTransactionRequest();
        StepVerifier.create(aorService.completeTransaction("uid", Mono.just(request)))
                .expectError(PnInvalidInputException.class).verify();
    }

    @Test
    void testCompleteWhenDaoNotFindThenReturnResponseKO(){
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST));
        CompleteTransactionResponse response = aorService.completeTransaction("uid", Mono.just(completeTransactionRequest)).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_1, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_NOT_EXIST.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void testCompleteWhenTransactionAlreadyCompletedThenReturnResponseKO(){
        entityComplete.setStatus(Const.COMPLETED);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        CompleteTransactionResponse response = aorService.completeTransaction("uid", Mono.just(completeTransactionRequest)).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_2, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_COMPLETED.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void testCompleteWhenTransactionAlreadyAbortedThenReturnResponseKO(){
        entityComplete.setStatus(Const.ABORTED);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        CompleteTransactionResponse response = aorService.completeTransaction("uid", Mono.just(completeTransactionRequest)).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_2, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_ABORTED.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void testCompleteWhenTransactionInErrorThenReturnResponseKO(){
        entityComplete.setStatus(Const.ERROR);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        CompleteTransactionResponse response = aorService.completeTransaction("uid", Mono.just(completeTransactionRequest)).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ERROR_STATUS.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void testCompleteWhenUpdateStatusThenReturnKO(){
        entityComplete.setStatus(Const.STARTED);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        Mockito.when(raddTransactionDAO.updateStatus(Mockito.any())).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS));

        CompleteTransactionResponse response = aorService.completeTransaction("uid", Mono.just(completeTransactionRequest)).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void testCompleteAllOKThenReturnOK(){
        entityComplete.setStatus(Const.STARTED);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        Mockito.when(raddTransactionDAO.updateStatus(Mockito.any())).thenReturn(Mono.just(entityComplete));

        CompleteTransactionResponse response = aorService.completeTransaction("uid", Mono.just(completeTransactionRequest)).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_0, response.getStatus().getCode());
    }

    // ---------------- //

    // AOR ABORT //

    @Test
    void testAbortWhenValidateRequestThenThrowInvalidInputException(){
        AbortTransactionRequest request = new AbortTransactionRequest();
        StepVerifier.create(aorService.abortTransaction("uid", Mono.just(request)))
                .expectError(PnInvalidInputException.class).verify();

        request.setOperationId("1234AOR");
        StepVerifier.create(aorService.abortTransaction("uid", Mono.just(request)))
                .expectError(PnInvalidInputException.class).verify();

        request.setReason("cancelled by user");
        StepVerifier.create(aorService.abortTransaction("uid", Mono.just(request)))
                .expectError(PnInvalidInputException.class).verify();
    }

    @Test
    void testAbortWhenDaoNotFindThenReturnResponseKO(){
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST));
        AbortTransactionResponse response = aorService.abortTransaction("uid", Mono.just(abortTransactionRequest)).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_1, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_NOT_EXIST.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void testAbortWhenTransactionAlreadyCompletedThenReturnResponseKO(){
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        AbortTransactionResponse response = aorService.abortTransaction("uid", Mono.just(abortTransactionRequest)).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_2, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_COMPLETED.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void testAbortWhenTransactionAlreadyAbortedThenReturnResponseKO(){
        entityComplete.setStatus(Const.ABORTED);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        AbortTransactionResponse response = aorService.abortTransaction("uid", Mono.just(abortTransactionRequest)).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_ABORTED.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void testAbortWhenTransactionInErrorThenReturnResponseKO(){
        entityComplete.setStatus(Const.ERROR);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        AbortTransactionResponse response = aorService.abortTransaction("uid", Mono.just(abortTransactionRequest)).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ERROR_STATUS.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void testAbortWhenUpdateStatusThenReturnKO(){
        entityComplete.setStatus(Const.STARTED);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        Mockito.when(raddTransactionDAO.updateStatus(Mockito.any())).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS));

        AbortTransactionResponse response = aorService.abortTransaction("uid", Mono.just(abortTransactionRequest)).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS.getMessage(), response.getStatus().getMessage());
    }

    @Test
    void testAbortAllOKThenReturnOK(){
        entityComplete.setStatus(Const.STARTED);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        Mockito.when(raddTransactionDAO.updateStatus(Mockito.any())).thenReturn(Mono.just(entityComplete));

        AbortTransactionResponse response = aorService.abortTransaction("uid", Mono.just(abortTransactionRequest)).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_0, response.getStatus().getCode());
    }

    // -------------------- //

    //  AOR INQUIRY //
    @Test
    void testWhenSearchReturnEmptyThrowException(){
        Mockito.when(pnDeliveryPushClient.getPaperNotificationFailed(Mockito.any())).thenReturn(Flux.just(new ResponsePaperNotificationFailedDtoDto()));
        aorService.aorInquiry("uid", "FRMTTR76M06B715E", "PF")
                .onErrorResume(ex -> {
                    if (ex instanceof RaddGenericException){
                        assertNotNull(((RaddGenericException) ex).getExceptionType());
                        assertEquals(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF, ((RaddGenericException) ex).getExceptionType());
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

        AORInquiryResponse inquiryResponse = aorService.aorInquiry("uid", "FRMTTR76M06B715E", "PF").block();
        assertNotNull(inquiryResponse);
        assertFalse(inquiryResponse.getResult());
        assertEquals(new BigDecimal(99), inquiryResponse.getStatus().getCode().getValue());
        assertEquals(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF.getMessage(), inquiryResponse.getStatus().getMessage());
    }

    @Test
    void testWhenSearchListReturnOK(){
        ResponsePaperNotificationFailedDtoDto response1 = new ResponsePaperNotificationFailedDtoDto();
        response1.setRecipientInternalId(ENSURE_FC);

        ResponsePaperNotificationFailedDtoDto response2 = new ResponsePaperNotificationFailedDtoDto();
        response2.setRecipientInternalId("testCF2");
        Mockito.when(pnDeliveryPushClient.getPaperNotificationFailed(Mockito.any())).thenReturn(Flux.just(response1, response2));

        AORInquiryResponse inquiryResponse = aorService.aorInquiry("uid", "FRMTTR76M06B715E", "PF").block();
        log.info("Response {}", inquiryResponse);
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
    // ------------------- //
}
