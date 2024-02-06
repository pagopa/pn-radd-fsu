package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.mapper.TransactionDataMapper;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.radd.middleware.db.impl.RaddTransactionDAOImpl;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.ALREADY_COMPLETE_PRINT;
import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@Slf4j
class ActServiceTest extends BaseTest {

    @InjectMocks
    ActService actService;

    @Mock
    PnDataVaultClient pnDataVaultClient;

    @Mock
    RaddTransactionDAOImpl raddTransactionDAOImpl;

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
        baseEntity.setIun("iun");
        baseEntity.setOperationId("operationIdTest");
        baseEntity.setOperationType(OperationTypeEnum.ACT.name());
        baseEntity.setStatus(Const.STARTED);


        ResponseNotificationViewedDtoDto responseNotificationViewedDtoDto = new ResponseNotificationViewedDtoDto();
        Mono<ResponseNotificationViewedDtoDto> monoNotificationViewedDtoDto = Mono.just(responseNotificationViewedDtoDto);
        when(pnDeliveryPushClient.notifyNotificationViewed(any(), any())).thenReturn(monoNotificationViewedDtoDto);
    }



    @Test
    void testWhenResponseIsFull(){

        when(pnDataVaultClient.getEnsureFiscalCode(any(), any())
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

        when(pnDataVaultClient.getEnsureFiscalCode(any(), any()))
                .thenThrow(PnInvalidInputException.class);
        Mono<ActInquiryResponse> response = actService.actInquiry("test","", CxTypeAuthFleet.PG,"test","test", "test", "test");
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Codice fiscale, tipo utente o codice fiscale non valorizzati correttamente", exception.getMessage());
            return Mono.empty();
        }).block();

    }


    @Test
    void testWhenAddInquiryHasNoQrCode(){
        when(pnDataVaultClient.getEnsureFiscalCode(any(), any()))
                .thenReturn(Mono.just("ASSBBDDD"));
        StepVerifier.create(actService.actInquiry("test","", CxTypeAuthFleet.PG,"test","test", "test", "test"))
                .expectError(PnInvalidInputException.class).verify();
    }

    @Test
    void testStartTransactionReturnErrorPnInvalidInputException(){
        ActStartTransactionRequest startTransactionRequest = new ActStartTransactionRequest();
        startTransactionRequest.setQrCode("qrcode");
        startTransactionRequest.setOperationId("id");
        startTransactionRequest.setOperationId("id");
        startTransactionRequest.setRecipientTaxId("taxId");
        startTransactionRequest.setIun("iun");
        startTransactionRequest.setRecipientType(ActStartTransactionRequest.RecipientTypeEnum.PF);
        TransactionData transactionData = new TransactionData();
        transactionData.setQrCode("qrcode");
        transactionData.setIun("iun");
        when (transactionDataMapper.toTransaction("id", startTransactionRequest, CxTypeAuthFleet.PG, "cxId")).thenReturn(transactionData);
        when(raddTransactionDAOImpl.countFromIunAndStatus("iun")).thenReturn(Mono.just(0));
        StepVerifier.create(actService.startTransaction("id",  "cxId",CxTypeAuthFleet.PG, startTransactionRequest) )
                .expectError(PnInvalidInputException.class).verify();
    }

    @Test
    void testStartTransactionReturnError(){
        ActStartTransactionRequest startTransactionRequest = new ActStartTransactionRequest();
        startTransactionRequest.recipientType(ActStartTransactionRequest.RecipientTypeEnum.PG);
        Mono<StartTransactionResponse> response = actService.startTransaction("test", "123", CxTypeAuthFleet.PG, startTransactionRequest);
        response.onErrorResume(PnInvalidInputException.class, exception -> {
            log.info("Exception {}", exception.getReason());
            assertEquals("Id operazione non valorizzato", exception.getReason());
            return Mono.empty();
        }).block();

        startTransactionRequest.setOperationId("TestOperationId");


        Mono<StartTransactionResponse> response2 = actService.startTransaction("test", "cxId",CxTypeAuthFleet.PG, startTransactionRequest);
        response2.onErrorResume(PnInvalidInputException.class, exception -> {
            assertEquals("Codice fiscale non valorizzato", exception.getReason());
            return Mono.empty();
        }).block();

        startTransactionRequest.setRecipientTaxId("abc342psoeo22");

        Mono<StartTransactionResponse> response3 = actService.startTransaction("test","cxId",CxTypeAuthFleet.PG, startTransactionRequest);
        response3.onErrorResume(PnInvalidInputException.class, exception -> {
            assertEquals("Né IUN nè QrCode valorizzati", exception.getReason());
            return Mono.empty();
        }).block();
    }

    @Test
    void testStartTransactionReturnErrorBecauseAlreadyExistsQrCodeInCompleted() {
        ActStartTransactionRequest startTransactionRequest = new ActStartTransactionRequest();
        startTransactionRequest.setOperationId("id");
        startTransactionRequest.setRecipientType(ActStartTransactionRequest.RecipientTypeEnum.PF);
        startTransactionRequest.setRecipientTaxId("taxId");
        startTransactionRequest.setIun("iun");
        TransactionData transactionData = new TransactionData();
        transactionData.setQrCode(startTransactionRequest.getQrCode());
        transactionData.setRecipientId("234");
        transactionData.setRecipientType(ActStartTransactionRequest.RecipientTypeEnum.PF.getValue());

        when(transactionDataMapper.toTransaction("id", startTransactionRequest, CxTypeAuthFleet.PF,  "cxId")).thenReturn(transactionData);
        when(pnDataVaultClient.getEnsureFiscalCode(any(), any())).thenReturn(Mono.just("123"));
        when(pnDataVaultClient.getEnsureFiscalCode(any(), any())).thenReturn(Mono.just("123"));

        // si presuppone che in questo caso non esista già l'operazione RADD
        when(raddTransactionDAOImpl.getTransaction("", "", "id", OperationTypeEnum.ACT))
                .thenReturn(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST)));

        when(raddTransactionDAOImpl.countFromIunAndStatus("iun")).thenReturn(Mono.just(1));

        StartTransactionResponse response = actService.startTransaction("id","cxId",CxTypeAuthFleet.PF, startTransactionRequest).block();
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isNotNull();
        assertThat(response.getStatus().getCode()).isEqualTo(StartTransactionResponseStatus.CodeEnum.NUMBER_99);
        assertThat(response.getStatus().getMessage()).isEqualTo(new RaddGenericException(ALREADY_COMPLETE_PRINT).getExceptionType().getMessage());
    }

    @Test
    void testCompleteTransactionReturnOk() {
        baseEntity.setStatus(Const.STARTED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(monoEntity);

        when(raddTransactionDAOImpl.updateStatus(any(), any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test",completeRequest,CxTypeAuthFleet.valueOf("PF"), "cxId").block();
        assertNotNull(completeTransactionResponse);
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_0, completeTransactionResponse.getStatus().getCode());
        assertEquals(Const.OK, completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testCompleteWhenTransactionAlreadyCompletedThenReturnNumber2() {

        baseEntity.setStatus(Const.COMPLETED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeRequest,CxTypeAuthFleet.valueOf("PF"), "cxId").block();
        assertNotNull(completeTransactionResponse);
        assertNotNull(completeTransactionResponse.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_2, completeTransactionResponse.getStatus().getCode());

        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_COMPLETED.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testCompleteWhenTransactionIsAbortedThenReturnNumber2() {
        baseEntity.setStatus(Const.ABORTED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeRequest,CxTypeAuthFleet.valueOf("PF"), "cxId").block();
        assertNotNull(completeTransactionResponse);
        assertNotNull(completeTransactionResponse.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_2, completeTransactionResponse.getStatus().getCode());

        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_ABORTED.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testCompleteWhenTransactionIsInErrorReturn99() {
        baseEntity.setStatus(Const.ERROR);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeRequest,CxTypeAuthFleet.valueOf("PF"), "cxId").block();
        assertNotNull(completeTransactionResponse);
        assertNotNull(completeTransactionResponse.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, completeTransactionResponse.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ERROR_STATUS.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testCompleteWhenThrowErrorNotificationViewedAndNotUpdateStatusThrowPnRaddException() {
        baseEntity.setStatus(Const.STARTED);
        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(monoEntity);

        WebClientResponseException ex = new WebClientResponseException("Internal server Error", 500, "header", null, null, null);
        when(pnDeliveryPushClient.notifyNotificationViewed(any(), any()))
        Mockito.when(pnDeliveryPushClient.notifyNotificationRaddRetrieved(any(), any()))
                .thenReturn(Mono.error(new PnRaddException(ex)));

        when(raddTransactionDAOImpl.updateStatus(any(), any()))
                .thenReturn(Mono.just(baseEntity));

        actService.completeTransaction("test", completeRequest,CxTypeAuthFleet.valueOf("PF"), "cxId")
                .onErrorResume(PnRaddException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();
    }

    @Test
    void testCompleteWhenUpdateStatusSettingsErrorThrowException(){
        baseEntity.setStatus(Const.STARTED);
        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(monoEntity);

        WebClientResponseException ex = new WebClientResponseException("Internal server Error", 500, "header", null, null, null);
        Mockito.when(pnDeliveryPushClient.notifyNotificationRaddRetrieved(any(), any()))
                .thenReturn(Mono.error(new PnRaddException(ex)));

        when(raddTransactionDAOImpl.updateStatus(any(), any()))
                .thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS));

        actService.completeTransaction("test", completeRequest,CxTypeAuthFleet.valueOf("PF"), "cxId")
                .onErrorResume(PnRaddException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();
    }





    @Test
    void testCompleteWhenGetTransactionThrowExceptionThenReturnError1() {
        completeRequest.setOperationId("OperationIdTestNotExist");
        Mockito.when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any()))
                .thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST));

        CompleteTransactionResponse responseError1 = actService.completeTransaction("test", completeRequest,CxTypeAuthFleet.valueOf("PF"), "cxId").block();
        assertNotNull(responseError1);
        assertNotNull(responseError1.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_1, responseError1.getStatus().getCode());

    }

    // ----------------- //

    @Test
    void testAbortTransactionReturnError(){
        when(actService.raddTransactionDAO.getTransaction(any(), any(), any(), any())).thenThrow(RaddGenericException.class);
        StepVerifier.create(actService.actInquiry("test","", CxTypeAuthFleet.PG,"test","test", "test", "test"))
                .expectError(PnInvalidInputException.class).verify();
    }

    //@Test
    void testWhenAbortFunctionParametersAreInvalid(){
        AbortTransactionRequest abortTransactionRequest= new AbortTransactionRequest();
        abortTransactionRequest.setOperationId("");
        Mono<AbortTransactionResponse> response = actService.abortTransaction("", CxTypeAuthFleet.valueOf("PF"),"cxId",abortTransactionRequest );
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni paramentri come operazione id o data di operazione non sono valorizzate", exception.getMessage());
            return Mono.empty();
        }).block();
    }

    @Test
    void testWhenAbortTransactionReqNull(){
        Mono<AbortTransactionResponse> abortTransactionResponse = actService.abortTransaction("test", CxTypeAuthFleet.valueOf("PF"),"cxId",null);
        abortTransactionResponse.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni parametri come operazione id o data di operazione non sono valorizzate", exception.getMessage() );
            return Mono.empty();}
         ).block();
    }

    @Test
    void testActInquiryWhenControlCheckArrResponseError() {
        when(this.raddTransactionDAOImpl.countFromIunAndStatus(any())).thenReturn(Mono.just(0));
        when(pnDataVaultClient.getEnsureFiscalCode(any(), any())).thenReturn(Mono.just("ABCDEF12G34H567I"));
        when(pnDeliveryClient.getCheckAar(any(), any(), any())).thenReturn(Mono.just(new ResponseCheckAarDtoDto()));
        ActInquiryResponse monoResponse = actService.actInquiry("test","123", CxTypeAuthFleet.PF,"test","PF", "test", "").block();
        assertNotNull(monoResponse);
        assertNotNull(monoResponse.getResult());
        assertEquals(false, monoResponse.getResult());
        assertEquals(ExceptionTypeEnum.IUN_NOT_FOUND.getMessage(), monoResponse.getStatus().getMessage());
    }

    @Test
    void testActInquiryWhenRequestIsEmpty() {
        actService.completeTransaction("test", new CompleteTransactionRequest(),CxTypeAuthFleet.valueOf("PF"), "cxId")
            .onErrorResume(PnInvalidInputException.class, exception ->{
                assertEquals("Operation id non valorizzato", exception.getMessage() );
                return Mono.empty();}
            ).block();
    }


    @Test
    void testAbortTransactionReqNull (){
        actService.abortTransaction("test", CxTypeAuthFleet.valueOf("PF"),"cxId",null)
                .onErrorResume(PnInvalidInputException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();

        AbortTransactionRequest request = new AbortTransactionRequest();
        request.setOperationId(null);
        actService.abortTransaction("test", CxTypeAuthFleet.valueOf("PF"),"cxId",request)
                .onErrorResume(PnInvalidInputException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();

        request.setOperationId("Id");
        request.setReason(null);
        actService.abortTransaction("test", CxTypeAuthFleet.valueOf("PF"),"cxId",request)
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
        when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(Mono.just(entity));
        when( raddTransactionDAOImpl.updateStatus(any(), any())).thenReturn(Mono.just(entity));

        AbortTransactionResponse response = actService.abortTransaction("test",CxTypeAuthFleet.valueOf("PF"),"cxId" ,request).block();
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
        when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(Mono.just(entity));
        when( raddTransactionDAOImpl.updateStatus(any(), any())).thenThrow(RaddGenericException.class);
        actService.abortTransaction("test",CxTypeAuthFleet.valueOf("PF"),"cxId" ,request)
                .onErrorResume(RaddGenericException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();

    }



}
