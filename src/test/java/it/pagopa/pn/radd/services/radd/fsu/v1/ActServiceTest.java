package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.mapper.TransactionDataMapper;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


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

    @Mock
    PnDeliveryClient pnDeliveryClient;

    CompleteTransactionRequest completeRequest;
    RaddTransactionEntity baseEntity;

    @BeforeEach
    void setUp(){
        completeRequest = new CompleteTransactionRequest();
        completeRequest.setOperationId("operationIdTest");
        completeRequest.setOperationDate(new Date());

        baseEntity = new RaddTransactionEntity();
        baseEntity.setIuns(List.of("iun"));
        baseEntity.setOperationId("operationIdTest");
        baseEntity.setOperationType(OperationTypeEnum.ACT.name());
        baseEntity.setStatus(Const.STARTED);


        ResponseNotificationViewedDtoDto responseNotificationViewedDtoDto = new ResponseNotificationViewedDtoDto();
        Mono<ResponseNotificationViewedDtoDto> monoNotificationViewedDtoDto = Mono.just(responseNotificationViewedDtoDto);
        Mockito.when(pnDeliveryPushClient.notifyNotificationViewed(Mockito.any())).thenReturn(monoNotificationViewedDtoDto);
    }



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

        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any()))
                .thenThrow(PnInvalidInputException.class);
        Mono<ActInquiryResponse> response = actService.actInquiry("test","","test","test");
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("recipientTaxId o recipientType non valorizzato correttamente", exception.getMessage());
            return Mono.empty();
        }).block();

    }


    @Test
    void testWhenAddInquiryHasNoQrCode(){
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just("ASSBBDDD"));
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
    void testCompleteTransactionReturnOk() {
        baseEntity.setStatus(Const.STARTED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        Mockito.when(raddTransactionDAO.updateStatus(Mockito.any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeRequest).block();
        assertNotNull(completeTransactionResponse);
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_0, completeTransactionResponse.getStatus().getCode());
        assertEquals(Const.OK, completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testCompleteWhenTransactionAlreadyCompletedThenReturnNumber2() {

        baseEntity.setStatus(Const.COMPLETED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeRequest).block();
        assertNotNull(completeTransactionResponse);
        assertNotNull(completeTransactionResponse.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_2, completeTransactionResponse.getStatus().getCode());

        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_COMPLETED.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testCompleteWhenTransactionIsAbortedThenReturnNumber2() {
        baseEntity.setStatus(Const.ABORTED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeRequest).block();
        assertNotNull(completeTransactionResponse);
        assertNotNull(completeTransactionResponse.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_2, completeTransactionResponse.getStatus().getCode());

        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_ABORTED.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testCompleteWhenTransactionIsInErrorReturn99() {
        baseEntity.setStatus(Const.ERROR);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeRequest).block();
        assertNotNull(completeTransactionResponse);
        assertNotNull(completeTransactionResponse.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, completeTransactionResponse.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ERROR_STATUS.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testCompleteWhenThrowErrorNotificationViewedAndNotUpdateStatusThrowPnRaddException() {
        baseEntity.setStatus(Const.STARTED);
        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        WebClientResponseException ex = new WebClientResponseException("Internal server Error", 500, "header", null, null, null);
        Mockito.when(pnDeliveryPushClient.notifyNotificationViewed(Mockito.any()))
                .thenReturn(Mono.error(new PnRaddException(ex)));

        Mockito.when(raddTransactionDAO.updateStatus(Mockito.any()))
                .thenReturn(Mono.just(baseEntity));

        actService.completeTransaction("test", completeRequest)
                .onErrorResume(PnRaddException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();
    }

    @Test
    void testCompleteWhenUpdateStatusSettingsErrorThrowException(){
        baseEntity.setStatus(Const.STARTED);
        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        WebClientResponseException ex = new WebClientResponseException("Internal server Error", 500, "header", null, null, null);
        Mockito.when(pnDeliveryPushClient.notifyNotificationViewed(Mockito.any()))
                .thenReturn(Mono.error(new PnRaddException(ex)));

        Mockito.when(raddTransactionDAO.updateStatus(Mockito.any()))
                .thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS));

        actService.completeTransaction("test", completeRequest)
                .onErrorResume(PnRaddException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();
    }





    @Test
    void testCompleteWhenGetTransactionThrowExceptionThenReturnError1() {
        completeRequest.setOperationId("OperationIdTestNotExist");
        Mockito.when(raddTransactionDAO.getTransaction(completeRequest.getOperationId(), OperationTypeEnum.ACT))
                .thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST));

        CompleteTransactionResponse responseError1 = actService.completeTransaction("test", completeRequest).block();
        assertNotNull(responseError1);
        assertNotNull(responseError1.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_1, responseError1.getStatus().getCode());

    }

    // ----------------- //

    @Test
    void testAbortTransactionReturnError(){
        Mockito.when(actService.raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenThrow(RaddGenericException.class);
        StepVerifier.create(actService.actInquiry("test", "test","test","test"))
                .expectError(PnInvalidInputException.class).verify();
    }

    //@Test
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

    @Test
    void testActInquiryWhenControlCheckArrResponseError() {
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any())).thenReturn(Mono.just("ABCDEF12G34H567I"));
        Mockito.when(pnDeliveryClient.getCheckAar(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(new ResponseCheckAarDtoDto()));
        ActInquiryResponse monoResponse = actService.actInquiry("test", "test",Const.PF,"test").block();
        assertNotNull(monoResponse.getResult());
        assertEquals(false, monoResponse.getResult());
        assertEquals(ExceptionTypeEnum.IUN_NOT_FOUND.getMessage(), monoResponse.getStatus().getMessage());
    }

    @Test
    void testActInquiryWhenRequestIsEmpty() {
        actService.completeTransaction("test", new CompleteTransactionRequest())
            .onErrorResume(PnInvalidInputException.class, exception ->{
                assertEquals("Operation id non valorizzato", exception.getMessage() );
                return Mono.empty();}
            ).block();
    }


    @Test
    void testAbortTransactionReqNull (){
        actService.abortTransaction("test", null)
                .onErrorResume(PnInvalidInputException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();

        AbortTransactionRequest request = new AbortTransactionRequest();
        request.setOperationId(null);
        actService.abortTransaction("test", request)
                .onErrorResume(PnInvalidInputException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();

        request.setOperationId("Id");
        request.setReason(null);
        actService.abortTransaction("test", request)
                .onErrorResume(PnInvalidInputException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();


    }



    @Test
    void testAbortTransactionOk (){
        AbortTransactionRequest request = new AbortTransactionRequest();
        request.setOperationId("Id");
        request.setReason("reason");
        request.setOperationDate(new Date());
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setStatus(Const.STARTED);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entity));
        Mockito.when( raddTransactionDAO.updateStatus(Mockito.any())).thenReturn(Mono.just(entity));

        AbortTransactionResponse response = actService.abortTransaction("test", request).block();
        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_0, response.getStatus().getCode());


    }

    @Test
    void abortTransactionReturnsRaddGenericException(){
        AbortTransactionRequest request = new AbortTransactionRequest();
        request.setOperationId("Id");
        request.setReason("reason");
        request.setOperationDate(new Date());
        RaddTransactionEntity entity = new RaddTransactionEntity();
        entity.setStatus(Const.STARTED);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entity));
        Mockito.when( raddTransactionDAO.updateStatus(Mockito.any())).thenThrow(RaddGenericException.class);
        actService.abortTransaction("test", request)
                .onErrorResume(RaddGenericException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();

    }



}
