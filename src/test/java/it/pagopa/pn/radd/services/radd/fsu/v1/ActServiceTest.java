package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.TransactionDataMapper;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.*;
import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

    @Test
    void testWhenResponseIsFull(){

        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any())
        ).thenReturn( Mono.just("data"));
        Mono<String> response = actService.getEnsureFiscalCode("test", Const.PF);
        assertTrue(!response.toString().isEmpty());

    }



    @Test
    void testWhenBundleIdIsEmpty(){
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        PnDataVaultClient pnDataVaultClient = new PnDataVaultClient(pnRaddFsuConfig);
        Mono<String> response = actService.getEnsureFiscalCode("", Const.PF);
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("recipientTaxId o recipientType non valorizzato correttamente", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    void testWhenFiscalCodeIsNotCorrect(){
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        PnDataVaultClient pnDataVaultClient = new PnDataVaultClient(pnRaddFsuConfig);
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

    //@Test
    void testStartTransactionReturnError(){
        ActStartTransactionRequest startTransactionRequest = new ActStartTransactionRequest();
        Mono<StartTransactionResponse> response = actService.startTransaction("test", startTransactionRequest);
        response.onErrorResume(PnInvalidInputException.class, exception -> {
            assertEquals("parametri non validi", exception.getMessage());
            return Mono.empty();
        }).block();
    }

    @Test
    void testCompleteTransactionWithEntity() {
        CompleteTransactionRequest completeTransactionRequest= new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIun("iun");
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
        raddTransactionEntity.setIun("iun");
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setStatus(Const.COMPLETED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(raddTransactionEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest).block();
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_COMPLETED.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testWhenCompleteTransactionThrowErrorStatusAborted() {
        CompleteTransactionRequest completeTransactionRequest= new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIun("iun");
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setStatus(Const.ABORTED);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(raddTransactionEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest).block();
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_ABORTED.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testWhenCompleteTransactionThrowErrorStatusError() {
        CompleteTransactionRequest completeTransactionRequest= new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIun("iun");
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setStatus(Const.ERROR);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(raddTransactionEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest).block();
        assertEquals(ExceptionTypeEnum.TRANSACTION_ERROR_STATUS.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testWhenCompleteTransactionThrowErrorGetTransaction() {
        CompleteTransactionRequest completeTransactionRequest= new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");

        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIun("iun");
        raddTransactionEntity.setOperationId("operationId");
        raddTransactionEntity.setStatus(Const.ERROR);

        Mono<RaddTransactionEntity> monoEntity = Mono.just(raddTransactionEntity);
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenReturn(monoEntity);

        CompleteTransactionResponse completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest).block();
        assertEquals(ExceptionTypeEnum.TRANSACTION_NOT_EXIST.getMessage(), completeTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testWhenCompleteTransactionReturnsCorrectly(){
        CompleteTransactionRequest completeTransactionRequest= new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("Id");
        Mono<CompleteTransactionResponse> completeTransactionResponse = actService.completeTransaction("test", completeTransactionRequest );
        assertNotNull(completeTransactionResponse);

    }

    @Test
    void testCompleteTransactionReturnError(){
        Mockito.when(raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenThrow(RaddGenericException.class);
       // ActInquiryResponse response = actService.actInquiry("test", "test","test","test").block();
        StepVerifier.create(actService.actInquiry("test", "test","test","test"))
                .expectError(PnInvalidInputException.class).verify();
    }

    @Test
    void testAbortTransactionReturnError(){
        Mockito.when(actService.raddTransactionDAO.getTransaction(Mockito.any(), Mockito.any())).thenThrow(RaddGenericException.class);
        //ActInquiryResponse response = actService.actInquiry("test", "test","test","test").block();
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
        /*AbortTransactionRequest abortTransactionRequest=null;
        Mono<AbortTransactionResponse> abortTransactionResponse = actService.abortTransaction("test", null);
        abortTransactionResponse.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni paramentri come operazione id o data di operazione non sono valorizzate", exception.getMessage() );
            return Mono.empty();}
         ).block();*/
        String message= "Alcuni paramentri come operazione id o data di operazione non sono valorizzate";
        assertThrows(PnInvalidInputException.class, () ->{
            actService.abortTransaction("test", null).block();
        });

        /*StepVerifier.create(actService.abortTransaction("test", null))
                .expectError(PnInvalidInputException.class).verify();*/


    }

}
