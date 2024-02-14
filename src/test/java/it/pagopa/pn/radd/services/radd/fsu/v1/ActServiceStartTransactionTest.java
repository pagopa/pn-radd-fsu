package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.mapper.TransactionDataMapper;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.v1.dto.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileDownloadResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.OperationResultCodeResponseDto;
import it.pagopa.pn.radd.middleware.db.impl.RaddTransactionDAOImpl;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.*;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@Slf4j
class ActServiceStartTransactionTest extends BaseTest {
    @InjectMocks
    ActService actService;

    @Mock
    PnDataVaultClient pnDataVaultClient;

    @Mock
    RaddTransactionDAOImpl raddTransactionDAOImpl;


    @Mock
    PnDeliveryPushClient pnDeliveryPushClient;

    @Mock
    PnDeliveryClient pnDeliveryClient;

    @Mock
    PnSafeStorageClient safeStorage ;

    @Mock
    PnRaddFsuConfig pnRaddFsuConfig;

    @Autowired
    @Spy
    private TransactionDataMapper transactionDataMapper;


    private ActStartTransactionRequest createActStartTransactionRequest(){
        ActStartTransactionRequest actStartTransactionRequest = new ActStartTransactionRequest();
        actStartTransactionRequest.setOperationId("Id");
        actStartTransactionRequest.setQrCode("QrCode");
        actStartTransactionRequest.setChecksum("Checksum");
        actStartTransactionRequest.setFileKey("FileKey");
        actStartTransactionRequest.setDelegateTaxId("DelTaxId");
        actStartTransactionRequest.setRecipientTaxId("recTaxId");
        actStartTransactionRequest.setVersionToken("VersionTokenX");
        actStartTransactionRequest.setOperationDate(new Date());
        actStartTransactionRequest.setRecipientType(ActStartTransactionRequest.RecipientTypeEnum.PF);
        return actStartTransactionRequest;
    }

    private RaddTransactionEntity createRaddTransactionEntity(){
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIun("testIun");
        raddTransactionEntity.setOperationId("Id");
        raddTransactionEntity.setStatus(Const.STARTED);
        raddTransactionEntity.setOperationType(OperationTypeEnum.ACT.name());
        raddTransactionEntity.setOperationStartDate(String.valueOf(new Date()));
        raddTransactionEntity.setRecipientId("recipientTaxIdResult");
        raddTransactionEntity.setDelegateId("delegateTaxIdResult");
        return raddTransactionEntity;
    }

    private FileDownloadResponseDto createFileDownloadResponseDto (){
        FileDownloadResponseDto fileDownloadResponseDto = new FileDownloadResponseDto();
        //Da decommentare dopo l'aggiornamento dell'interfaccia ss
        //fileDownloadResponseDto.setDocumentStatus(Const.PRELOADED);
        fileDownloadResponseDto.setVersionId("VersionTokenX");
        fileDownloadResponseDto.setChecksum("Checksum");
        return fileDownloadResponseDto ;
    }

    private SentNotificationV23Dto createSentNotificationDto(){
        NotificationPaymentItemDto notificationPaymentInfoDto = new NotificationPaymentItemDto();
        notificationPaymentInfoDto.setPagoPa(new PagoPaPaymentDto());

        NotificationRecipientV23Dto notificationRecipientDto = new NotificationRecipientV23Dto();
        notificationRecipientDto.setPayments(List.of(notificationPaymentInfoDto));
        notificationRecipientDto.setTaxId("recTaxId");
        notificationRecipientDto.setInternalId("internalId");
        NotificationDocumentDto notificationDocumentDto =new NotificationDocumentDto();
        notificationDocumentDto.setDocIdx("0");

        SentNotificationV23Dto sentNotificationDto = new SentNotificationV23Dto ();
        sentNotificationDto.setRecipients(List.of(notificationRecipientDto));
        sentNotificationDto.setDocuments(List.of(notificationDocumentDto));

        return sentNotificationDto;
    }

    private Stream<LegalFactListElementDto> createLegalFactListElementDto(){

        LegalFactsIdDto legalFactsIdDto = new LegalFactsIdDto();
        legalFactsIdDto.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactsIdDto.setKey("Key");
        LegalFactListElementDto legalFactListElementDto = new LegalFactListElementDto();
        legalFactListElementDto.setTaxId("recTaxId");
        legalFactListElementDto.setLegalFactsId(legalFactsIdDto);

        return Stream.of(legalFactListElementDto);
    }


    @Test
    void testStartTransactionWithEntity() {
        ActStartTransactionRequest request = createActStartTransactionRequest();
        RaddTransactionEntity raddTransactionEntity = createRaddTransactionEntity();
        ResponseCheckAarDtoDto responseCheckAarDtoDto = new ResponseCheckAarDtoDto();
        responseCheckAarDtoDto.setIun("testIun");


        Mockito.when(pnDeliveryClient.getCheckAar(any(), any(), any())).thenReturn(Mono.just(responseCheckAarDtoDto));
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(request.getRecipientTaxId(), request.getRecipientType().getValue())).thenReturn(Mono.just("recipientTaxIdResult"));
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(request.getDelegateTaxId(), Const.PF)).thenReturn(Mono.just("delegateTaxIdResult"));
        Mockito.when(raddTransactionDAOImpl.createRaddTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        Mockito.when(raddTransactionDAOImpl.countFromIunAndStatus(any())).thenReturn(Mono.just(0));
        FileDownloadResponseDto fileDownloadResponseDto = createFileDownloadResponseDto () ;
        Mockito.when(pnDeliveryPushClient.getNotificationHistory(any())).thenReturn(Mono.just(new NotificationHistoryResponseDto()));

        Mockito.when(safeStorage.getFile (any())).thenReturn(Mono.just(fileDownloadResponseDto));
        Mockito.when(safeStorage.updateFileMetadata(any())).thenReturn(Mono.just(new OperationResultCodeResponseDto()));
        SentNotificationV23Dto sentNotificationDto = createSentNotificationDto();
        Mockito.when(pnDeliveryClient.getNotifications(any())).thenReturn(Mono.just(sentNotificationDto));
        NotificationAttachmentDownloadMetadataResponseDto notificationAttachmentDownloadMetadataResponseDto = new NotificationAttachmentDownloadMetadataResponseDto();
        notificationAttachmentDownloadMetadataResponseDto.setUrl("UrlDocument");
        Mockito.when(pnDeliveryClient.getPresignedUrlDocument (any(), any(), any())).thenReturn(Mono.just(notificationAttachmentDownloadMetadataResponseDto));
        NotificationAttachmentDownloadMetadataResponseDto notificationAttachmentDownloadMetadataResponseDto1 = new NotificationAttachmentDownloadMetadataResponseDto();
        notificationAttachmentDownloadMetadataResponseDto1.setUrl("UrlPayment");
        Mockito.when(pnDeliveryClient.getPresignedUrlPaymentDocument (any(), any(), any(), any())).thenReturn(Mono.just(notificationAttachmentDownloadMetadataResponseDto1));
        Mockito.when(pnDeliveryPushClient.getNotificationLegalFacts(any(), any())).thenReturn(Flux.fromStream(createLegalFactListElementDto()));
        LegalFactDownloadMetadataWithContentTypeResponseDto legalFactDownloadMetadataResponseDto = new LegalFactDownloadMetadataWithContentTypeResponseDto();
        legalFactDownloadMetadataResponseDto.setUrl("UrlLegalFact");
        legalFactDownloadMetadataResponseDto.setContentType("application/pdf");
        Mockito.when(pnDeliveryPushClient.getLegalFact(any(), any(), any(), any())).thenReturn(Mono.just(legalFactDownloadMetadataResponseDto));

        Mockito.when(raddTransactionDAOImpl.updateZipAttachments(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        StartTransactionResponse startTransactionResponse = actService.startTransaction("test", "cxId", CxTypeAuthFleet.PG, request).block();
        assertNotNull(startTransactionResponse);
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_0, startTransactionResponse.getStatus().getCode());
        assertEquals(Const.OK, startTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testStartWhenLegalFactHasRetryAfterReturnNumber2() {
        ActStartTransactionRequest request = createActStartTransactionRequest();
        RaddTransactionEntity raddTransactionEntity = createRaddTransactionEntity();
        ResponseCheckAarDtoDto responseCheckAarDtoDto = new ResponseCheckAarDtoDto();
        responseCheckAarDtoDto.setIun("testIun");
        Mockito.when(pnDeliveryClient.getCheckAar(any(), any(), any())).thenReturn(Mono.just(responseCheckAarDtoDto));
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(request.getRecipientTaxId(), request.getRecipientType().getValue())).thenReturn(Mono.just("recipientTaxIdResult"));
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(request.getDelegateTaxId(), Const.PF)).thenReturn(Mono.just("delegateTaxIdResult"));
        Mockito.when(raddTransactionDAOImpl.createRaddTransaction(any(), any())).thenReturn(Mono.just(raddTransactionEntity));
        FileDownloadResponseDto fileDownloadResponseDto = createFileDownloadResponseDto () ;
        Mockito.when(safeStorage.getFile (any())).thenReturn(Mono.just(fileDownloadResponseDto));
        Mockito.when(safeStorage.updateFileMetadata(any())).thenReturn(Mono.just(new OperationResultCodeResponseDto()));
        SentNotificationV23Dto sentNotificationDto = createSentNotificationDto();
        Mockito.when(pnDeliveryClient.getNotifications(any())).thenReturn(Mono.just(sentNotificationDto));
        NotificationAttachmentDownloadMetadataResponseDto notificationAttachmentDownloadMetadataResponseDto = new NotificationAttachmentDownloadMetadataResponseDto();
        notificationAttachmentDownloadMetadataResponseDto.setUrl("UrlDocument");
        Mockito.when(pnDeliveryClient.getPresignedUrlDocument (any(), any(), any())).thenReturn(Mono.just(notificationAttachmentDownloadMetadataResponseDto));
        NotificationAttachmentDownloadMetadataResponseDto notificationAttachmentDownloadMetadataResponseDto1 = new NotificationAttachmentDownloadMetadataResponseDto();
        notificationAttachmentDownloadMetadataResponseDto1.setUrl("UrlPayment");
        Mockito.when(pnDeliveryClient.getPresignedUrlPaymentDocument (any(), any(), any(), any())).thenReturn(Mono.just(notificationAttachmentDownloadMetadataResponseDto1));
        Mockito.when(pnDeliveryPushClient.getNotificationLegalFacts(any(), any())).thenReturn(Flux.fromStream(createLegalFactListElementDto()));
        Mockito.when(pnRaddFsuConfig.getApplicationBasepath()).thenReturn("123");
        LegalFactDownloadMetadataWithContentTypeResponseDto legalFactDownloadMetadataResponseDto = new LegalFactDownloadMetadataWithContentTypeResponseDto();
        legalFactDownloadMetadataResponseDto.setUrl("UrlLegalFact");
        legalFactDownloadMetadataResponseDto.setContentType("application/pdf");
        legalFactDownloadMetadataResponseDto.setRetryAfter(new BigDecimal(20));
        Mockito.when(pnDeliveryPushClient.getLegalFact(any(), any(), any(), any()))
                .thenReturn(Mono.just(legalFactDownloadMetadataResponseDto));

        Mockito.when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(Mono.just(new RaddTransactionEntity()));
        Mockito.when(raddTransactionDAOImpl.updateStatus(any(), any())).thenReturn(Mono.just(new RaddTransactionEntity()));
        Mockito.when(raddTransactionDAOImpl.countFromIunAndStatus(any())).thenReturn(Mono.just(0));

        Mockito.when(pnDeliveryPushClient.getNotificationHistory(any())).thenReturn(Mono.just(new NotificationHistoryResponseDto()));

        StartTransactionResponse startTransactionResponse = actService.startTransaction("test", "cxId", CxTypeAuthFleet.PG, request).block();
        assertNotNull(startTransactionResponse);
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_2, startTransactionResponse.getStatus().getCode());
        assertEquals(new BigDecimal(20), startTransactionResponse.getStatus().getRetryAfter());
    }

    @Test
    void testStartWhenIunNotFoundThenReturnNumber99(){
        ActStartTransactionRequest request = createActStartTransactionRequest();
        ResponseCheckAarDtoDto responseCheckAarDtoDto = new ResponseCheckAarDtoDto();
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(request.getRecipientTaxId(), request.getRecipientType().getValue())).thenReturn(Mono.just("recipientTaxIdResult"));
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(request.getDelegateTaxId(), Const.PF)).thenReturn(Mono.just("delegateTaxIdResult"));
        Mockito.when(pnDeliveryClient.getCheckAar(any(), any(), any())).thenReturn(Mono.just(responseCheckAarDtoDto));
        Mockito.when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(Mono.just(new RaddTransactionEntity()));
        Mockito.when(raddTransactionDAOImpl.updateStatus(any(), any())).thenReturn(Mono.just(new RaddTransactionEntity()));
        Mockito.when(raddTransactionDAOImpl.countFromIunAndStatus(any())).thenReturn(Mono.just(0));


        StartTransactionResponse startTransactionResponse = actService.startTransaction("test", "cxId", CxTypeAuthFleet.PG, request).block();
        assertNotNull(startTransactionResponse);
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_99, startTransactionResponse.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.IUN_NOT_FOUND.getMessage(), startTransactionResponse.getStatus().getMessage());
    }

    @Test
    void testStartWhenGetCheckAarThrow500ThenReturnNumber99(){
        ActStartTransactionRequest request = createActStartTransactionRequest();
        WebClientResponseException exMock = new WebClientResponseException("Internal server Error", 500, "header", null, null, null);
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(request.getRecipientTaxId(), request.getRecipientType().getValue())).thenReturn(Mono.just("recipientTaxIdResult"));
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(request.getDelegateTaxId(), Const.PF)).thenReturn(Mono.just("delegateTaxIdResult"));
        Mockito.when(pnDeliveryClient.getCheckAar(any(), any(), any()))
                .thenReturn(Mono.error(new PnRaddException(exMock)));
        Mockito.when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(Mono.just(new RaddTransactionEntity()));
        Mockito.when(raddTransactionDAOImpl.updateStatus(any(), any())).thenReturn(Mono.just(new RaddTransactionEntity()));
        Mockito.when(raddTransactionDAOImpl.countFromIunAndStatus(any())).thenReturn(Mono.just(0));


        StepVerifier.create(actService.startTransaction("test", "cxId", CxTypeAuthFleet.PG, request))
                .expectError(PnRaddException.class).verify();
    }


    @Test
    void testStartWhenMoreTranscationThenReturnNumber99(){
        ActStartTransactionRequest request = createActStartTransactionRequest();
        ResponseCheckAarDtoDto responseCheckAarDtoDto = new ResponseCheckAarDtoDto();
        responseCheckAarDtoDto.setIun("testIun");
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(request.getRecipientTaxId(), request.getRecipientType().getValue())).thenReturn(Mono.just("recipientTaxIdResult"));
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(request.getDelegateTaxId(), Const.PF)).thenReturn(Mono.just("delegateTaxIdResult"));
        Mockito.when(pnDeliveryClient.getCheckAar(any(), any(), any()))
                .thenReturn(Mono.just(responseCheckAarDtoDto));
        Mockito.when(raddTransactionDAOImpl.createRaddTransaction(any(), any())).thenThrow(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_ALREADY_EXIST));
        Mockito.when(raddTransactionDAOImpl.countFromIunAndStatus(any())).thenReturn(Mono.just(0));

        Mockito.when(pnDeliveryPushClient.getNotificationHistory(any())).thenReturn(Mono.just(new NotificationHistoryResponseDto()));

        Mockito.when(raddTransactionDAOImpl.getTransaction(any(), any(), any(), any())).thenReturn(Mono.just(new RaddTransactionEntity()));
        Mockito.when(raddTransactionDAOImpl.updateStatus(any(), any())).thenReturn(Mono.just(new RaddTransactionEntity()));


        StartTransactionResponse startTransactionResponse = actService.startTransaction("test", "cxId", CxTypeAuthFleet.PG, request).block();
        assertNotNull(startTransactionResponse);
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_99, startTransactionResponse.getStatus().getCode());
        assertEquals(ExceptionTypeEnum.TRANSACTION_ALREADY_EXIST.getMessage(), startTransactionResponse.getStatus().getMessage());

    }

}