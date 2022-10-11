package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.mapper.TransactionDataMapper;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
class ActServiceTest extends BaseTest {

    @InjectMocks
    ActService actService;

    @Mock
    PnDataVaultClient pnDataVaultClient;

    @Mock
    RaddTransactionDAO raddTransactionDAO;

    @Mock
    TransactionDataMapper transactionDataMapper;

    @Mock
    PnDeliveryPushClient pnDeliveryPushClient;

    @Test
    void testWhenResponseIsFull(){

        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any())
        ).thenReturn( Mono.just("data"));
        Mono<String> response = actService.getEnsureFiscalCode("test", Const.PF);
        assertFalse(response.toString().isEmpty());
    }



    @Test
    void testWhenBundleIdIsEmpty(){
        Mono<String> response = actService.getEnsureFiscalCode("", Const.PF);
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("recipientTaxId o recipientType non valorizzato correttamente", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    void testWhenFiscalCodeIsNotCorrect(){
        Mono<String> response = actService.getEnsureFiscalCode("test", "fiscalcodeNotCorrect");
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("recipientTaxId o recipientType non valorizzato correttamente", exception.getMessage());
            return Mono.empty();
        }).block();
    }


    @Test
    void testWhenActInquiryHasEmptyRecipientTaxId(){

        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any())
        ).thenThrow(PnInvalidInputException.class);
        Mono<ActInquiryResponse> response = actService.actInquiry("test","","test","test");
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("recipientTaxId o recipientType non valorizzato correttamente", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    void testWhenAddInquiryHasNoQrCode(){
        Mockito.when(actService.getEnsureFiscalCode("test", "test")).thenThrow(PnInvalidInputException.class);
        StepVerifier.create(actService.actInquiry("test", "test","test",""))
                .expectError(PnInvalidInputException.class).verify();
    }

    @Test
    void testStartTransactionReturnErrorPnInvalidInputException(){
        ActStartTransactionRequest startTransactionRequest = new ActStartTransactionRequest();
        startTransactionRequest.setQrCode("qrcode");
        startTransactionRequest.setOperationId("id");
        startTransactionRequest.setOperationId("id");
        startTransactionRequest.setRecipientTaxId("taxId");
        startTransactionRequest.setRecipientType(ActStartTransactionRequest.RecipientTypeEnum.PF);
        Mockito.when (transactionDataMapper.toTransaction("id", startTransactionRequest)).thenReturn(new TransactionData());
        StepVerifier.create(actService.startTransaction("id", startTransactionRequest) )
                .expectError(PnInvalidInputException.class).verify();
    }

    @Test
    void testStartTransactionReturnError(){
        ActStartTransactionRequest startTransactionRequest = new ActStartTransactionRequest();
        Mono<StartTransactionResponse> response = actService.startTransaction("test", startTransactionRequest);
        response.onErrorResume(PnInvalidInputException.class, exception -> {
            log.info("Exception {}", exception.getReason());
            assertEquals("Id operazione non valorizzato", exception.getReason());
            return Mono.empty();
        }).block();

        startTransactionRequest.setOperationId("TestOperationId");


        Mono<StartTransactionResponse> response2 = actService.startTransaction("test", startTransactionRequest);
        response2.onErrorResume(PnInvalidInputException.class, exception -> {
            assertEquals("Codice fiscale non valorizzato", exception.getReason());
            return Mono.empty();
        }).block();

        startTransactionRequest.setRecipientTaxId("abc342psoeo22");

        Mono<StartTransactionResponse> response3 = actService.startTransaction("test", startTransactionRequest);
        response3.onErrorResume(PnInvalidInputException.class, exception -> {
            assertEquals("QRCode non valorizzato", exception.getReason());
            return Mono.empty();
        }).block();

        startTransactionRequest.setQrCode("abc342psoeo22");

        Mono<StartTransactionResponse> response4 = actService.startTransaction("test", startTransactionRequest);
        response4.onErrorResume(PnInvalidInputException.class, exception -> {
            assertEquals("Recipient Type non valorizzato correttamente", exception.getReason());
            return Mono.empty();
        }).block();
    }

    @Test
    void testCompleteTransactionReturnsCorrectly() {
        CompleteTransactionRequest completeTransactionRequest= new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIuns(List.of("iun"));
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setStatus(Const.PRELOADED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(raddTransactionEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        ResponseNotificationViewedDtoDto responseNotificationViewedDtoDto = new ResponseNotificationViewedDtoDto();
        Mono<ResponseNotificationViewedDtoDto> monoNotificationViewedDtoDto = Mono.just(responseNotificationViewedDtoDto);
        Mockito.when(pnDeliveryPushClient.notifyNotificationViewed(raddTransactionEntity)).thenReturn(monoNotificationViewedDtoDto);

        Mockito.when(raddTransactionDAO.updateStatus(raddTransactionEntity)).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest).block();
        assertNotNull(completeTransactionResponse);
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_0, completeTransactionResponse.getStatus().getCode());
        assertEquals(Const.OK, completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testWhenCompleteTransactionThrowErrorStatusCompleted() {
        CompleteTransactionRequest completeTransactionRequest= new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIuns(List.of("iun"));
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setStatus(Const.COMPLETED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(raddTransactionEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest).block();
        assertNotNull(completeTransactionResponse);
        assertNotNull(completeTransactionResponse.getStatus());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_COMPLETED.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testWhenCompleteTransactionThrowErrorStatusAborted() {
        CompleteTransactionRequest completeTransactionRequest= new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIuns(List.of("iun"));
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setStatus(Const.ABORTED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(raddTransactionEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest).block();
        assertNotNull(completeTransactionResponse);
        assertNotNull(completeTransactionResponse.getStatus());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_ABORTED.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testWhenCompleteTransactionThrowErrorStatusError() {
        CompleteTransactionRequest completeTransactionRequest= new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIuns(List.of("iun"));
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setStatus(Const.ERROR);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(raddTransactionEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest).block();
        assertNotNull(completeTransactionResponse);
        assertNotNull(completeTransactionResponse.getStatus());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ERROR_STATUS.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testWhenCompleteTransactionThrowErrorGetTransaction() {
        CompleteTransactionRequest completeTransactionRequest= new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIuns(List.of("iun"));
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setStatus(Const.PRELOADED);

        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST));

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest).block();
        assertNotNull(completeTransactionResponse);
        assertNotNull(completeTransactionResponse.getStatus());
        assertEquals(ExceptionTypeEnum.TRANSACTION_NOT_EXIST.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testWhenCompleteTransactionThrowErrorNotificationViewed() {
        CompleteTransactionRequest completeTransactionRequest= new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIuns(List.of("iun"));
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setOperationType(OperationTypeEnum.ACT.name());
        raddTransactionEntity.setStatus(Const.PRELOADED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(raddTransactionEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);
        WebClientResponseException ex = new WebClientResponseException("Internal server Error", 500, "header", null, null, null);
        //TODO sistemare il then return
        Mockito.when(pnDeliveryPushClient.notifyNotificationViewed(raddTransactionEntity)).thenReturn(Mono.error(new PnRaddException(ex)));

        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        Mockito.when(raddTransactionDAO.updateStatus(raddTransactionEntity)).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS));

        actService.completeTransaction("test", completeTransactionRequest)
        .onErrorResume(PnRaddException.class, exception ->{
            assertNotNull(exception);
            return Mono.empty();
        }).block();
    }
/*
    @Test
    void testWhenCompleteTransactionThrowErrorUpdateStatusFromExceptionTransactionNotExistNumber1() {
        CompleteTransactionRequest completeTransactionRequest = new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIuns(List.of("iun"));
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setStatus(Const.PRELOADED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(raddTransactionEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        ResponseNotificationViewedDtoDto responseNotificationViewedDtoDto = new ResponseNotificationViewedDtoDto();
        Mono<ResponseNotificationViewedDtoDto> monoNotificationViewedDtoDto = Mono.just(responseNotificationViewedDtoDto);
        Mockito.when(pnDeliveryPushClient.notifyNotificationViewed(raddTransactionEntity)).thenReturn(monoNotificationViewedDtoDto);

        Mockito.when(raddTransactionDAO.getTransaction("operationId", OperationTypeEnum.ACT)).thenThrow(RaddGenericException.class);

//        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest).block();
//        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_1, completeTransactionResponse.getStatus().getCode());
    }
*/

    @Test
    void testWhenCompleteTransactionThrowErrorUpdateStatusFromExceptionCompletedAbortedNumber2() {
        CompleteTransactionRequest completeTransactionRequest = new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIuns(List.of("iun"));
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setStatus(Const.PRELOADED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(raddTransactionEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        ResponseNotificationViewedDtoDto responseNotificationViewedDtoDto = new ResponseNotificationViewedDtoDto();
        Mono<ResponseNotificationViewedDtoDto> monoNotificationViewedDtoDto = Mono.just(responseNotificationViewedDtoDto);
        Mockito.when(pnDeliveryPushClient.notifyNotificationViewed(raddTransactionEntity)).thenReturn(monoNotificationViewedDtoDto);

        raddTransactionEntity.setStatus(Const.COMPLETED);
        Mockito.when(raddTransactionDAO.updateStatus(raddTransactionEntity)).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS));

        Mockito.when(raddTransactionDAO.getTransaction("operationId", OperationTypeEnum.ACT)).thenReturn(monoEntity);

        Mockito.when(raddTransactionDAO.updateStatus(Mockito.any())).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS));

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest).block();
        assertNotNull(completeTransactionResponse);
        assertNotNull(completeTransactionResponse.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_2, completeTransactionResponse.getStatus().getCode());
    }

    @Test
    void testWhenCompleteTransactionThrowErrorUpdateStatusFromExceptionElseNumber99() {
        CompleteTransactionRequest completeTransactionRequest = new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIuns(List.of("iun"));
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setStatus(Const.PRELOADED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(raddTransactionEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        ResponseNotificationViewedDtoDto responseNotificationViewedDtoDto = new ResponseNotificationViewedDtoDto();
        Mono<ResponseNotificationViewedDtoDto> monoNotificationViewedDtoDto = Mono.just(responseNotificationViewedDtoDto);
        Mockito.when(pnDeliveryPushClient.notifyNotificationViewed(raddTransactionEntity)).thenReturn(monoNotificationViewedDtoDto);

        raddTransactionEntity.setStatus(Const.ERROR);
        Mockito.when(raddTransactionDAO.updateStatus(raddTransactionEntity)).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS));

        Mockito.when(raddTransactionDAO.getTransaction("operationId", OperationTypeEnum.ACT)).thenReturn(monoEntity);

        Mockito.when(raddTransactionDAO.updateStatus(Mockito.any())).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS));

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest).block();
        assertNotNull(completeTransactionResponse);
        assertNotNull(completeTransactionResponse.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, completeTransactionResponse.getStatus().getCode());
    }

    @Test
    void testAbortTransactionReturnError(){
        Mockito.when(actService.raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenThrow(RaddGenericException.class);
        StepVerifier.create(actService.actInquiry("test", "test","test","test"))
                .expectError(PnInvalidInputException.class).verify();
    }

    @Test
    void testWhenAbortFunctionParametersAreInvalid(){
        AbortTransactionRequest abortTransactionRequest= new AbortTransactionRequest();
        abortTransactionRequest.setOperationId("");
        Mono<AbortTransactionResponse> response = actService.abortTransaction("", abortTransactionRequest );
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni paramentri come operazione id o data di operazione non sono valorizzate", exception.getMessage());
            return Mono.empty();
        }).block();
    }

    @Test
    void testWhenAbortTransactionReqNull(){
        Mono<AbortTransactionResponse> abortTransactionResponse = actService.abortTransaction("test", null);
        abortTransactionResponse.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni paramentri come operazione id o data di operazione non sono valorizzate", exception.getMessage() );
            return Mono.empty();}
         ).block();
    }

}
