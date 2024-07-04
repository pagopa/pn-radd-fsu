package it.pagopa.pn.radd.services.radd.fsu.v1;

import de.neuland.assertj.logging.ExpectedLogging;
import de.neuland.assertj.logging.ExpectedLoggingAssertions;
import it.pagopa.pn.commons.log.PnAuditLog;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.ResponseCheckAarDtoDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.TransactionDataMapper;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.db.impl.RaddTransactionDAOImpl;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ActServiceTest  {

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

    @Mock
    PnRaddFsuConfig pnRaddFsuConfig;

    CompleteTransactionRequest completeRequest;
    RaddTransactionEntity baseEntity;
    @RegisterExtension
    ExpectedLogging logging = ExpectedLogging.forSource(PnAuditLog.class);

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

        Mono<ActInquiryResponse> response = actService.actInquiry("test","", CxTypeAuthFleet.PG,"test","test", "test", "test");
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Recipient Type non valorizzato correttamente", exception.getMessage());
            return Mono.empty();
        }).block();

    }


    @Test
    void testWhenAddInquiryHasNoQrCode(){
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
        startTransactionRequest.setFileKey("fileKey");
        startTransactionRequest.setIun("iun");
        startTransactionRequest.setRecipientType(ActStartTransactionRequest.RecipientTypeEnum.PF);
        TransactionData transactionData = new TransactionData();
        transactionData.setQrCode("qrcode");
        transactionData.setIun("iun");
        StepVerifier.create(actService.startTransaction("id",  "cxId",CxTypeAuthFleet.PG, "RADD_UPLOADER", startTransactionRequest) )
                .expectError(PnInvalidInputException.class).verify();
    }

    @Test
    void testStartTransactionReturnErrorResponseStatusExceptionNoFileKey(){
        ActStartTransactionRequest startTransactionRequest = new ActStartTransactionRequest();
        startTransactionRequest.setQrCode("qrcode");
        startTransactionRequest.setOperationId("id");
        startTransactionRequest.setOperationId("id");
        startTransactionRequest.setRecipientTaxId("taxId");
        startTransactionRequest.setIun("iun");
        startTransactionRequest.setFileKey(null);
        startTransactionRequest.setRecipientType(ActStartTransactionRequest.RecipientTypeEnum.PF);
        TransactionData transactionData = new TransactionData();
        transactionData.setQrCode("qrcode");
        transactionData.setIun("iun");
        StepVerifier.create(actService.startTransaction("id",  "cxId",CxTypeAuthFleet.PG, "RADD_UPLOADER", startTransactionRequest) )
                .expectErrorMatches(throwable -> throwable instanceof PnRaddBadRequestException &&
                        "Campo fileKey obbligatorio mancante".equals(throwable.getMessage()))
                .verify();
    }

    @Test
    void testStartTransactionReturnErrorResponseStatusExceptionNoFileKe(){
        ActStartTransactionRequest startTransactionRequest = new ActStartTransactionRequest();
        startTransactionRequest.setQrCode("qrcode");
        startTransactionRequest.setOperationId("id");
        startTransactionRequest.setOperationId("id");
        startTransactionRequest.setRecipientTaxId("taxId");
        startTransactionRequest.setIun("iun");
        startTransactionRequest.setFileKey("fileKey");
        startTransactionRequest.setRecipientType(ActStartTransactionRequest.RecipientTypeEnum.PF);
        TransactionData transactionData = new TransactionData();
        transactionData.setQrCode("qrcode");
        transactionData.setIun("iun");
        StepVerifier.create(actService.startTransaction("id",  "cxId",CxTypeAuthFleet.PG, "RADD", startTransactionRequest) )
                .expectErrorMatches(throwable -> throwable instanceof PnRaddBadRequestException &&
                        "Campo fileKey inaspettato".equals(throwable.getMessage()))
                .verify();
    }

    @Test
    void testStartTransactionReturnError(){
        ActStartTransactionRequest startTransactionRequest = new ActStartTransactionRequest();
        startTransactionRequest.recipientType(ActStartTransactionRequest.RecipientTypeEnum.PG);
        startTransactionRequest.setFileKey("fileKey");
        Mono<StartTransactionResponse> response = actService.startTransaction("test", "123", CxTypeAuthFleet.PG, "RADD_UPLOADER", startTransactionRequest);
        response.onErrorResume(PnInvalidInputException.class, exception -> {
            log.info("Exception {}", exception.getReason());
            assertEquals("Operation id non valorizzato", exception.getReason());
            return Mono.empty();
        }).block();

        startTransactionRequest.setOperationId("TestOperationId");


        Mono<StartTransactionResponse> response2 = actService.startTransaction("test", "cxId",CxTypeAuthFleet.PG, "RADD_UPLOADER", startTransactionRequest);
        response2.onErrorResume(PnInvalidInputException.class, exception -> {
            assertEquals("Codice fiscale non valorizzato", exception.getReason());
            return Mono.empty();
        }).block();

        startTransactionRequest.setRecipientTaxId("abc342psoeo22");

        Mono<StartTransactionResponse> response3 = actService.startTransaction("test","cxId",CxTypeAuthFleet.PG, "RADD_UPLOADER", startTransactionRequest);
        response3.onErrorResume(PnInvalidInputException.class, exception -> {
            assertEquals("Né IUN nè QrCode valorizzati", exception.getReason());
            return Mono.empty();
        }).block();
    }

    @Test
    void testStartTransactionReturnErrorBecauseAlreadyExistsQrCodeInCompletedAndPrintsAreLimitedTo1() {
        ActStartTransactionRequest startTransactionRequest = new ActStartTransactionRequest();
        startTransactionRequest.setOperationId("id");
        startTransactionRequest.setRecipientType(ActStartTransactionRequest.RecipientTypeEnum.PF);
        startTransactionRequest.setRecipientTaxId("taxId");
        startTransactionRequest.setIun("iun");
        startTransactionRequest.setFileKey("fileKey");
        TransactionData transactionData = new TransactionData();
        transactionData.setQrCode(startTransactionRequest.getQrCode());
        transactionData.setRecipientId("234");
        transactionData.setRecipientType(ActStartTransactionRequest.RecipientTypeEnum.PF.getValue());

        when(transactionDataMapper.toTransaction("id", startTransactionRequest, CxTypeAuthFleet.PF,  "cxId")).thenReturn(transactionData);
        when(pnDataVaultClient.getEnsureFiscalCode(any(), any())).thenReturn(Mono.just("123"));
        when(pnDataVaultClient.getEnsureFiscalCode(any(), any())).thenReturn(Mono.just("123"));

        when(pnRaddFsuConfig.getMaxPrintRequests()).thenReturn(1);
        when(raddTransactionDAOImpl.countFromIunAndStatus(any(),any())).thenReturn(Mono.just(1));

        StartTransactionResponse response = actService.startTransaction("id","cxId",CxTypeAuthFleet.PF, "RADD_UPLOADER", startTransactionRequest).block();
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isNotNull();
        assertThat(response.getStatus().getCode()).isEqualTo(StartTransactionResponseStatus.CodeEnum.NUMBER_3);
        assertThat(response.getStatus().getMessage()).isEqualTo("Limite di 1 stampa superato");
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_ACTTRAN] BEFORE - Start ACT startTransaction - uid=id cxId=cxId cxType=PF operationId=id");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_ACTTRAN] FAILURE - End ACT startTransaction with error Limite di 1 stampa superato - uid=id cxId=cxId cxType=PF operationId=id recipientInternalId=123 status=StartTransactionResponseStatus(code=3, message=Limite di 1 stampa superato, retryAfter=null)");
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
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_ACTTRAN] BEFORE - Start ACT completeTransaction - uid=test cxId=cxId cxType=PF operationId=operationIdTest");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_ACTTRAN] FAILURE - End ACT completeTransaction with error La transazione risulta già completa - uid=test cxId=cxId cxType=PF operationId=operationIdTest status=TransactionResponseStatus(code=2, message=La transazione risulta già completa)");
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
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_ACTTRAN] BEFORE - Start ACT completeTransaction - uid=test cxId=cxId cxType=PF operationId=operationIdTest");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_ACTTRAN] FAILURE - End ACT completeTransaction with error La transazione risulta annullata - uid=test cxId=cxId cxType=PF operationId=operationIdTest status=TransactionResponseStatus(code=2, message=La transazione risulta annullata)");
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
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_ACTTRAN] BEFORE - Start ACT completeTransaction - uid=test cxId=cxId cxType=PF operationId=operationIdTest");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_ACTTRAN] FAILURE - End ACT completeTransaction with error La transazione risulta in errore - uid=test cxId=cxId cxType=PF operationId=operationIdTest status=TransactionResponseStatus(code=99, message=La transazione risulta in errore)");
    }

    @Test
    void testCompleteWhenThrowErrorNotificationViewedAndNotUpdateStatusThrowPnRaddException() {
        baseEntity.setStatus(Const.STARTED);
        Mono<RaddTransactionEntity> monoEntity = Mono.just(baseEntity);
        when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(monoEntity);

        WebClientResponseException ex = new WebClientResponseException("Internal server Error", 500, "header", null, null, null);
        Mockito.when(pnDeliveryPushClient.notifyNotificationRaddRetrieved(any(), any()))
                .thenReturn(Mono.error(new PnRaddException(ex)));

        when(raddTransactionDAOImpl.updateStatus(any(), any()))
                .thenReturn(Mono.just(baseEntity));

        actService.completeTransaction("test", completeRequest,CxTypeAuthFleet.valueOf("PF"), "cxId")
                .onErrorResume(PnRaddException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_ACTTRAN] BEFORE - Start ACT completeTransaction - uid=test cxId=cxId cxType=PF operationId=operationIdTest");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_ACTTRAN] FAILURE - End ACT completeTransaction with error Internal server Error - uid=test cxId=cxId cxType=PF operationId=operationIdTest");
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
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_ACTTRAN] BEFORE - Start ACT completeTransaction - uid=test cxId=cxId cxType=PF operationId=operationIdTest");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_ACTTRAN] FAILURE - End ACT completeTransaction with error Internal server Error - uid=test cxId=cxId cxType=PF operationId=operationIdTest");
    }





    @Test
    void testCompleteWhenGetTransactionThrowExceptionThenReturnError1() {
        completeRequest.setOperationId("OperationIdTestNotExist");
        Mockito.when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any()))
                .thenReturn(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST)));
        CompleteTransactionResponse responseError1 = actService.completeTransaction("test", completeRequest,CxTypeAuthFleet.valueOf("PF"), "cxId").block();
        assertNotNull(responseError1);
        assertNotNull(responseError1.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_1, responseError1.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_NOT_EXIST.getMessage(), responseError1.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_ACTTRAN] BEFORE - Start ACT completeTransaction - uid=test cxId=cxId cxType=PF operationId=OperationIdTestNotExist");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_ACTTRAN] FAILURE - End ACT completeTransaction with error Transazione inesistente - uid=test cxId=cxId cxType=PF operationId=OperationIdTestNotExist status=TransactionResponseStatus(code=1, message=Transazione inesistente)");
    }

    // ----------------- //

    @Test
    void testAbortTransactionReturnError(){
        StepVerifier.create(actService.actInquiry("test","", CxTypeAuthFleet.PG,"test","test", "test", "test"))
                .expectError(PnInvalidInputException.class).verify();
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
        when(pnDataVaultClient.getEnsureFiscalCode(any(), any())).thenReturn(Mono.just("ABCDEF12G34H567I"));
        when(pnDeliveryClient.getCheckAar(any(), any(), any())).thenReturn(Mono.just(new ResponseCheckAarDtoDto()));
        ActInquiryResponse monoResponse = actService.actInquiry("test","123", CxTypeAuthFleet.PF,"test","PF", "test", "").block();
        assertNotNull(monoResponse);
        assertNotNull(monoResponse.getResult());
        assertEquals(false, monoResponse.getResult());
        assertEquals(ExceptionTypeEnum.IUN_NOT_FOUND.getMessage(), monoResponse.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_ACTINQUIRY] BEFORE - Start ACT Inquiry - uid=test cxId=123 cxType=PF");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_ACTINQUIRY] FAILURE - End ACT Inquiry with error Iun not found with params - uid=test cxId=123 cxType=PF recipientInternalId=ABCDEF12G34H567I status=ActInquiryResponseStatus(code=99, message=Iun not found with params)");
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
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_ACTTRAN] BEFORE - Start ACT abortTransaction - uid=test cxId=cxId cxType=PF operationId=Id");
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_ACTTRAN] SUCCESS - End ACT abortTransaction - uid=test cxId=cxId cxType=PF operationId=Id status=TransactionResponseStatus(code=0, message=OK)");
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
        when( raddTransactionDAOImpl.updateStatus(any(), any())).thenThrow(new RaddGenericException(ExceptionTypeEnum.GENERIC_ERROR));
        actService.abortTransaction("test",CxTypeAuthFleet.valueOf("PF"),"cxId" ,request)
                .onErrorResume(RaddGenericException.class, exception ->{
                    assertNotNull(exception);
                    return Mono.empty();
                }).block();
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_ACTTRAN] BEFORE - Start ACT abortTransaction - uid=test cxId=cxId cxType=PF operationId=Id");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_ACTTRAN] FAILURE - End ACT abortTransaction with error Si è verificato un errore - uid=test cxId=cxId cxType=PF operationId=Id status=TransactionResponseStatus(code=99, message=Si è verificato un errore)");
    }



}
