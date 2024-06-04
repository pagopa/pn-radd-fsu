package it.pagopa.pn.radd.services.radd.fsu.v1;

import de.neuland.assertj.logging.ExpectedLogging;
import de.neuland.assertj.logging.ExpectedLoggingAssertions;
import it.pagopa.pn.commons.log.PnAuditLog;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileDownloadInfoDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileDownloadResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.OperationResultCodeResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.mapper.TransactionDataMapper;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.db.impl.RaddTransactionDAOImpl;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AorServiceTest {
    private static final String ENSURE_FC = "PF-4fc75df3-0913-407e-bdaa-e50329708b7d";
    @InjectMocks
    private AorService aorService;
    @Mock
    PnRaddFsuConfig pnRaddFsuConfig;
    @Mock
    private PnDeliveryPushClient pnDeliveryPushClient;
    @Mock
    private PnDataVaultClient pnDataVaultClient;
    @Mock
    private RaddTransactionDAOImpl raddTransactionDAOImpl;
    @Mock
    private PnSafeStorageClient pnSafeStorageClient;
    @Spy
    private TransactionDataMapper transactionDataMapper;
    @RegisterExtension
    ExpectedLogging logging = ExpectedLogging.forSource(PnAuditLog.class);

    private AbortTransactionRequest abortTransactionRequest;
    private CompleteTransactionRequest completeTransactionRequest;
    private RaddTransactionEntity entityComplete;
    private AorStartTransactionRequest startTransactionRequest;

    @BeforeEach
    public void setUp() {
        abortTransactionRequest = new AbortTransactionRequest();
        abortTransactionRequest.setOperationId("1234AOR");
        abortTransactionRequest.setReason("cancelled by user");
        abortTransactionRequest.setOperationDate(new Date());

        completeTransactionRequest = new CompleteTransactionRequest();
        completeTransactionRequest.setOperationId("1234AOR");
        completeTransactionRequest.setOperationDate(new Date());

        startTransactionRequest = new AorStartTransactionRequest();
        startTransactionRequest.setRecipientType(AorStartTransactionRequest.RecipientTypeEnum.PF);
        startTransactionRequest.setRecipientTaxId("FRMTTR76M06B715E");
        startTransactionRequest.setOperationId("12345");
        startTransactionRequest.setChecksum("checksum-test");
        startTransactionRequest.setFileKey("file-key-test");
        startTransactionRequest.setVersionToken("version-token");

        entityComplete = new RaddTransactionEntity();
        entityComplete.setOperationType("1234AOR");
        entityComplete.setStatus(Const.COMPLETED);
    }

    // AOR START //
    @Test
    void testStartWhenValidateRequestThenThrowInvalidInputException() {
        AorStartTransactionRequest request = new AorStartTransactionRequest();
        StepVerifier.create(aorService.startTransaction("uid", request, CxTypeAuthFleet.valueOf("PF"), "xPagopaPnCxId"))
                .expectError(PnInvalidInputException.class).verify();

        request.setOperationId("1234AOR");
        StepVerifier.create(aorService.startTransaction("uid", request, it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet.valueOf("PF"), "xPagopaPnCxId"))
                .expectError(PnInvalidInputException.class).verify();
    }

    @Test
    void testStartOK() {
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(startTransactionRequest.getRecipientTaxId(), startTransactionRequest.getRecipientType().getValue()))
                .thenReturn(Mono.just("PF-4fc75df3-0913-407e-bdaa-e50329708b7d"));

        ResponsePaperNotificationFailedDtoDto paperFailed = new ResponsePaperNotificationFailedDtoDto();
        paperFailed.setRecipientInternalId("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        paperFailed.setIun("ABC-123-IUN");
        paperFailed.setAarUrl("//url:safestorage");
        Mockito.when(pnDeliveryPushClient.getPaperNotificationFailed(Mockito.any()))
                .thenReturn(Flux.just(paperFailed));

        Mockito.when(raddTransactionDAOImpl.createRaddTransaction(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(new RaddTransactionEntity()));

        FileDownloadResponseDto fileDownloadResponseDto = new FileDownloadResponseDto();
        //Da decommentare dopo l'aggiornamento dell'interfaccia ss
        //fileDownloadResponseDto.setDocumentStatus(Const.PRELOADED);
        fileDownloadResponseDto.setChecksum(startTransactionRequest.getChecksum());
        fileDownloadResponseDto.setVersionId(startTransactionRequest.getVersionToken());
        Mockito.when(pnSafeStorageClient.getFile(startTransactionRequest.getFileKey()))
                .thenReturn(Mono.just(fileDownloadResponseDto));

        Mockito.when(pnSafeStorageClient.updateFileMetadata(startTransactionRequest.getFileKey()))
                .thenReturn(Mono.just(new OperationResultCodeResponseDto()));

        FileDownloadResponseDto file1 = new FileDownloadResponseDto();
        FileDownloadInfoDto infoDto = new FileDownloadInfoDto();
        infoDto.setUrl("http://safestorage/PN_AAR_0000?");
        file1.setDownload(infoDto);
        Mockito.when(pnSafeStorageClient.getFile("//url:safestorage"))
                .thenReturn(Mono.just(file1));

        Mockito.when(pnRaddFsuConfig.getApplicationBasepath()).thenReturn("123");

        StartTransactionResponse response = this.aorService.startTransaction("uid", startTransactionRequest, CxTypeAuthFleet.valueOf("PF"), "1").block();

        assertNotNull(response);
        // assertNotNull(response.getUrlList());
        //  assertFalse(response.getUrlList().isEmpty());
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_0, response.getStatus().getCode());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR startTransaction - uid=uid cxId=1 cxType=PF operationId=12345 requestFileKey=file-key-test");
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] SUCCESS - End AOR starTransaction - uid=uid cxId=1 cxType=PF operationId=12345 transactionId=PF#1#12345 recipientInternalId=PF-4fc75df3-0913-407e-bdaa-e50329708b7d delegateInternalId=null requestFileKey=file-key-test downloadedFilekeys=[ PN_AAR_0000 ] iuns=[ ABC-123-IUN ] status=StartTransactionResponseStatus(code=0, message=OK, retryAfter=null)");
    }


    // ---------------- //


    // AOR COMPLETE //
    @Test
    void testCompleteWhenValidateRequestThenThrowInvalidInputException() {
        CompleteTransactionRequest request = new CompleteTransactionRequest();
        Assertions.assertThrows(PnInvalidInputException.class, (() -> aorService.completeTransaction("uid", request, CxTypeAuthFleet.valueOf("PF"), "cxId")));
    }

    @Test
    void testCompleteWhenDaoNotFindThenReturnResponseKO() {
        completeTransactionRequest.setOperationId("OperationIdTestNotExist");
        Mockito.when(raddTransactionDAOImpl.getTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST)));
        CompleteTransactionResponse response = aorService.completeTransaction("uid", completeTransactionRequest, CxTypeAuthFleet.valueOf("PF"), "cxId").block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_1, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_NOT_EXIST.getMessage(), response.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR completeTransaction - uid=uid cxId=cxId cxType=PF operationId=OperationIdTestNotExist");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_AORTRAN] FAILURE - End ACT completeTransaction with error Transazione inesistente - uid=uid cxId=cxId cxType=PF operationId=OperationIdTestNotExist status=TransactionResponseStatus(code=1, message=Transazione inesistente)");
    }

    @Test
    void testCompleteWhenTransactionAlreadyCompletedThenReturnResponseKO() {
        entityComplete.setStatus(Const.COMPLETED);
        Mockito.when(raddTransactionDAOImpl.getTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        CompleteTransactionResponse response = aorService.completeTransaction("uid", completeTransactionRequest, CxTypeAuthFleet.valueOf("PF"), "cxId").block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_2, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_COMPLETED.getMessage(), response.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR completeTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_AORTRAN] FAILURE - End ACT completeTransaction with error La transazione risulta già completa - uid=uid cxId=cxId cxType=PF operationId=1234AOR status=TransactionResponseStatus(code=2, message=La transazione risulta già completa)");
    }

    @Test
    void testCompleteWhenTransactionAlreadyAbortedThenReturnResponseKO() {
        entityComplete.setStatus(Const.ABORTED);
        Mockito.when(raddTransactionDAOImpl.getTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        CompleteTransactionResponse response = aorService.completeTransaction("uid", completeTransactionRequest, CxTypeAuthFleet.valueOf("PF"), "cxId").block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_2, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_ABORTED.getMessage(), response.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR completeTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_AORTRAN] FAILURE - End ACT completeTransaction with error La transazione risulta annullata - uid=uid cxId=cxId cxType=PF operationId=1234AOR status=TransactionResponseStatus(code=2, message=La transazione risulta annullata)");
    }

    @Test
    void testCompleteWhenTransactionInErrorThenReturnResponseKO() {
        entityComplete.setStatus(Const.ERROR);
        Mockito.when(raddTransactionDAOImpl.getTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        CompleteTransactionResponse response = aorService.completeTransaction("uid", completeTransactionRequest, CxTypeAuthFleet.valueOf("PF"), "cxId").block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ERROR_STATUS.getMessage(), response.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR completeTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_AORTRAN] FAILURE - End ACT completeTransaction with error La transazione risulta in errore - uid=uid cxId=cxId cxType=PF operationId=1234AOR status=TransactionResponseStatus(code=99, message=La transazione risulta in errore)");

    }

    @Test
    void testCompleteWhenUpdateStatusThenReturnKO() {
        entityComplete.setStatus(Const.STARTED);
        Mockito.when(raddTransactionDAOImpl.getTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        Mockito.when(raddTransactionDAOImpl.updateStatus(Mockito.any(), Mockito.any())).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS));

        CompleteTransactionResponse response = aorService.completeTransaction("uid", completeTransactionRequest, CxTypeAuthFleet.valueOf("PF"), "cxId").block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS.getMessage(), response.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR completeTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_AORTRAN] FAILURE - End ACT completeTransaction with error Lo stato della transazione non è stato aggiornato - uid=uid cxId=cxId cxType=PF operationId=1234AOR status=TransactionResponseStatus(code=99, message=Lo stato della transazione non è stato aggiornato)");
    }

    @Test
    void testCompleteAllOKThenReturnOK() {
        entityComplete.setStatus(Const.STARTED);
        Mockito.when(raddTransactionDAOImpl.getTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        Mockito.when(raddTransactionDAOImpl.updateStatus(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));

        CompleteTransactionResponse response = aorService.completeTransaction("uid", completeTransactionRequest, CxTypeAuthFleet.valueOf("PF"), "cxId").block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_0, response.getStatus().getCode());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR completeTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR");
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] SUCCESS - End AOR completeTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR status=TransactionResponseStatus(code=0, message=OK)");
    }

    // ---------------- //

    // AOR ABORT //

    @Test
    void testAbortWhenValidateRequestThenThrowInvalidInputException() {
        AbortTransactionRequest request = new AbortTransactionRequest();
        StepVerifier.create(aorService.abortTransaction("uid", CxTypeAuthFleet.valueOf("PF"), "cxId", request))
                .expectError(PnInvalidInputException.class).verify();

        request.setOperationId("1234AOR");
        StepVerifier.create(aorService.abortTransaction("uid", CxTypeAuthFleet.valueOf("PF"), "cxId", request))
                .expectError(PnInvalidInputException.class).verify();

        request.setReason("cancelled by user");
        StepVerifier.create(aorService.abortTransaction("uid", CxTypeAuthFleet.valueOf("PF"), "cxId", request))
                .expectError(PnInvalidInputException.class).verify();
    }

    @Test
    void testAbortWhenDaoNotFindThenReturnResponseKO() {
        Mockito.when(raddTransactionDAOImpl.getTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.error(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_EXIST)));
        AbortTransactionResponse response = aorService.abortTransaction("uid", CxTypeAuthFleet.valueOf("PF"), "cxId", abortTransactionRequest).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_1, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_NOT_EXIST.getMessage(), response.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR abortTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_AORTRAN] FAILURE - End AOR abortTransaction with error Transazione inesistente - uid=uid cxId=cxId cxType=PF operationId=1234AOR status=TransactionResponseStatus(code=1, message=Transazione inesistente)");
    }

    @Test
    void testAbortWhenTransactionAlreadyCompletedThenReturnResponseKO() {
        Mockito.when(raddTransactionDAOImpl.getTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        AbortTransactionResponse response = aorService.abortTransaction("uid", CxTypeAuthFleet.valueOf("PF"), "cxId", abortTransactionRequest).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_2, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_COMPLETED.getMessage(), response.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR abortTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_AORTRAN] FAILURE - End AOR abortTransaction with error La transazione risulta già completa - uid=uid cxId=cxId cxType=PF operationId=1234AOR status=TransactionResponseStatus(code=2, message=La transazione risulta già completa)");
    }

    @Test
    void testAbortWhenTransactionAlreadyAbortedThenReturnResponseKO() {
        entityComplete.setStatus(Const.ABORTED);
        Mockito.when(raddTransactionDAOImpl.getTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        AbortTransactionResponse response = aorService.abortTransaction("uid", CxTypeAuthFleet.valueOf("PF"), "cxId", abortTransactionRequest).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_ABORTED.getMessage(), response.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR abortTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_AORTRAN] FAILURE - End AOR abortTransaction with error La transazione risulta annullata - uid=uid cxId=cxId cxType=PF operationId=1234AOR status=TransactionResponseStatus(code=99, message=La transazione risulta annullata)");
    }

    @Test
    void testAbortWhenTransactionInErrorThenReturnResponseKO() {
        entityComplete.setStatus(Const.ERROR);
        Mockito.when(raddTransactionDAOImpl.getTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        AbortTransactionResponse response = aorService.abortTransaction("uid", CxTypeAuthFleet.valueOf("PF"), "cxId", abortTransactionRequest).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ERROR_STATUS.getMessage(), response.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR abortTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_AORTRAN] FAILURE - End AOR abortTransaction with error La transazione risulta in errore - uid=uid cxId=cxId cxType=PF operationId=1234AOR status=TransactionResponseStatus(code=99, message=La transazione risulta in errore)");
    }

    @Test
    void testAbortWhenUpdateStatusThenReturnKO() {
        entityComplete.setStatus(Const.STARTED);
        Mockito.when(raddTransactionDAOImpl.getTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        Mockito.when(raddTransactionDAOImpl.updateStatus(Mockito.any(), Mockito.any())).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS));

        AbortTransactionResponse response = aorService.abortTransaction("uid", CxTypeAuthFleet.valueOf("PF"), "cxId", abortTransactionRequest).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_99, response.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_NOT_UPDATE_STATUS.getMessage(), response.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR abortTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_AORTRAN] FAILURE - End AOR abortTransaction with error Lo stato della transazione non è stato aggiornato - uid=uid cxId=cxId cxType=PF operationId=1234AOR status=TransactionResponseStatus(code=99, message=Lo stato della transazione non è stato aggiornato)");
    }

    @Test
    void testAbortAllOKThenReturnOK() {
        entityComplete.setStatus(Const.STARTED);
        Mockito.when(raddTransactionDAOImpl.getTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));
        Mockito.when(raddTransactionDAOImpl.updateStatus(Mockito.any(), Mockito.any())).thenReturn(Mono.just(entityComplete));

        AbortTransactionResponse response = aorService.abortTransaction("uid", CxTypeAuthFleet.valueOf("PF"), "cxId", abortTransactionRequest).block();

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(TransactionResponseStatus.CodeEnum.NUMBER_0, response.getStatus().getCode());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] BEFORE - Start AOR abortTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR");
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORTRAN] SUCCESS - End AOR abortTransaction - uid=uid cxId=cxId cxType=PF operationId=1234AOR status=TransactionResponseStatus(code=0, message=OK)");
    }

    // -------------------- //

    //  AOR INQUIRY //
    @Test
    void testWhenSearchReturnEmptyThrowException() {
        Mockito.when(pnDeliveryPushClient.getPaperNotificationFailed(Mockito.any())).thenReturn(Flux.just(new ResponsePaperNotificationFailedDtoDto()));
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any())).thenReturn(Mono.just(ENSURE_FC));
        aorService.aorInquiry("uid", "FRMTTR76M06B715E", "PF", CxTypeAuthFleet.valueOf("PF"), "cxId")
                .onErrorResume(ex -> {
                    if (ex instanceof RaddGenericException) {
                        assertNotNull(((RaddGenericException) ex).getExceptionType());
                        assertEquals(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF, ((RaddGenericException) ex).getExceptionType());
                    }
                    fail("Bad type exception");
                    return null;
                }).block();
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORINQUIRY] BEFORE - Start AOR Inquiry - uid=uid cxId=cxId cxType=PF");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_AORINQUIRY] FAILURE - End AOR Inquiry with error Non ci sono notifiche non consegnate per questo codice fiscale - uid=uid cxId=cxId cxType=PF recipientInternalId=PF-4fc75df3-0913-407e-bdaa-e50329708b7d status=ResponseStatus(code=99, message=Non ci sono notifiche non consegnate per questo codice fiscale)");
    }

    @Test
    void testWhenSearchListEmptyReturnResponseKO() {
        ResponsePaperNotificationFailedDtoDto response1 = new ResponsePaperNotificationFailedDtoDto();
        response1.setRecipientInternalId("testCF1");

        ResponsePaperNotificationFailedDtoDto response2 = new ResponsePaperNotificationFailedDtoDto();
        response2.setRecipientInternalId("testCF2");

        Mockito.when(pnDeliveryPushClient.getPaperNotificationFailed(Mockito.any())).thenReturn(Flux.just(response1, response2));
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any())).thenReturn(Mono.just(ENSURE_FC));

        AORInquiryResponse inquiryResponse = aorService.aorInquiry("uid", "FRMTTR76M06B715E", "PF", CxTypeAuthFleet.valueOf("PF"), "cxId").block();
        assertNotNull(inquiryResponse);
        assertFalse(inquiryResponse.getResult());
        assertEquals(new BigDecimal(99), inquiryResponse.getStatus().getCode().getValue());
        assertEquals(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF.getMessage(), inquiryResponse.getStatus().getMessage());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORINQUIRY] BEFORE - Start AOR Inquiry - uid=uid cxId=cxId cxType=PF");
        ExpectedLoggingAssertions.assertThat(logging).hasErrorMessage("[AUD_RADD_AORINQUIRY] FAILURE - End AOR Inquiry with error Non ci sono notifiche non consegnate per questo codice fiscale - uid=uid cxId=cxId cxType=PF recipientInternalId=PF-4fc75df3-0913-407e-bdaa-e50329708b7d status=ResponseStatus(code=99, message=Non ci sono notifiche non consegnate per questo codice fiscale)");
    }

    @Test
    void testWhenSearchListReturnOK() {
        ResponsePaperNotificationFailedDtoDto response1 = new ResponsePaperNotificationFailedDtoDto();
        response1.setRecipientInternalId(ENSURE_FC);
        response1.setIun("ABC-456-IUN");
        response1.setAarUrl("//safeStorage:PN_AAR_0000");
        ResponsePaperNotificationFailedDtoDto response2 = new ResponsePaperNotificationFailedDtoDto();
        response2.setRecipientInternalId("testCF2");
        Mockito.when(pnDeliveryPushClient.getPaperNotificationFailed(Mockito.any())).thenReturn(Flux.just(response1, response2));
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(Mockito.any(), Mockito.any())).thenReturn(Mono.just(ENSURE_FC));
        AORInquiryResponse inquiryResponse = aorService.aorInquiry("uid", "FRMTTR76M06B715E", "PF", CxTypeAuthFleet.valueOf("PF"), "cxId").block();
        log.info("Response {}", inquiryResponse);
        assertNotNull(inquiryResponse);
        assertTrue(inquiryResponse.getResult());
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORINQUIRY] BEFORE - Start AOR Inquiry - uid=uid cxId=cxId cxType=PF");
        ExpectedLoggingAssertions.assertThat(logging).hasInfoMessage("[AUD_RADD_AORINQUIRY] SUCCESS - End AOR Inquiry - uid=uid cxId=cxId cxType=PF recipientInternalId=PF-4fc75df3-0913-407e-bdaa-e50329708b7d aarFilekeys=[ //safeStorage:PN_AAR_0000 ] iuns=[ ABC-456-IUN ] result=true status=ResponseStatus(code=0, message=OK)");
    }

    @Test
    void testWhenRecipientIdIsNullThrowPnInvalidInput() {
        try {
            aorService.aorInquiry("uid", "", "type", CxTypeAuthFleet.valueOf("PF"), "cxId").block();
        } catch (PnInvalidInputException ex) {
            assertNotNull(ex);
            assertEquals("Il campo codice fiscale non è valorizzato", ex.getReason());
            return;
        }
        fail("No PnInvalidInput throw");

    }
    // ------------------- //
}
