package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.mapper.TransactionDataMapper;
import it.pagopa.pn.radd.microservice.msclient.generated.pndelivery.v1.dto.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.*;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileDownloadResponseDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.OperationResultCodeResponseDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.middleware.msclient.*;
import it.pagopa.pn.radd.pojo.TransactionData;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class ActServiceStartTransactionTest extends BaseTest {
    @InjectMocks
    ActService actService;

    @Mock
    PnDataVaultClient pnDataVaultClient;

    @Mock
    RaddTransactionDAO raddTransactionDAO;


    @Mock
    PnDeliveryPushClient pnDeliveryPushClient;

    @Mock
    PnDeliveryClient pnDeliveryClient;

    @Mock
    PnSafeStorageClient safeStorage ;

    @Mock
    PnDeliveryPushInternalClient pnDeliveryPushInternalClient;

    @Autowired
    @Spy
    private TransactionDataMapper transactionDataMapper;


    public ActStartTransactionRequest createActStartTransactionRequest(){
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

    public RaddTransactionEntity createRaddTransactionEntity(){
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setIuns(List.of("testIun"));
        raddTransactionEntity.setOperationId("Id");
        raddTransactionEntity.setStatus(Const.STARTED);
        raddTransactionEntity.setOperationType(OperationTypeEnum.ACT.name());
        raddTransactionEntity.setOperationStartDate(String.valueOf(new Date()));
        raddTransactionEntity.setRecipientId("recipientTaxIdResult");
        raddTransactionEntity.setDelegateId("delegateTaxIdResult");
        return raddTransactionEntity;
    }

    public FileDownloadResponseDto createFileDownloadResponseDto (){
        FileDownloadResponseDto fileDownloadResponseDto = new FileDownloadResponseDto();
        fileDownloadResponseDto.setDocumentStatus(Const.PRELOADED);
        fileDownloadResponseDto.setVersionId("VersionTokenX");
        fileDownloadResponseDto.setChecksum("Checksum");
        return fileDownloadResponseDto ;
    }

    public SentNotificationDto createSentNotificationDto(){
        NotificationPaymentInfoDto notificationPaymentInfoDto = new NotificationPaymentInfoDto();
        notificationPaymentInfoDto.setPagoPaForm(new NotificationPaymentAttachmentDto());

        NotificationRecipientDto notificationRecipientDto = new NotificationRecipientDto();
        notificationRecipientDto.setPayment(notificationPaymentInfoDto);
        notificationRecipientDto.setTaxId("recTaxId");

        NotificationDocumentDto notificationDocumentDto =new NotificationDocumentDto();
        notificationDocumentDto.setDocIdx("0");

        SentNotificationDto sentNotificationDto = new SentNotificationDto ();
        sentNotificationDto.setRecipients(List.of(notificationRecipientDto));
        sentNotificationDto.setDocuments(List.of(notificationDocumentDto));

        return sentNotificationDto;
    }

    public Stream<LegalFactListElementDto> createLegalFactListElementDto(){

        LegalFactsIdDto legalFactsIdDto = new LegalFactsIdDto();
        legalFactsIdDto.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactsIdDto.setKey("Key");
        LegalFactListElementDto legalFactListElementDto = new LegalFactListElementDto();
        legalFactListElementDto.setTaxId("recTaxId");
        legalFactListElementDto.setLegalFactsId(legalFactsIdDto);

        return List.of(legalFactListElementDto).stream();
    }


    @Test
    void testStartTransactionWithEntity() {
        ActStartTransactionRequest request = createActStartTransactionRequest() ;
        RaddTransactionEntity raddTransactionEntity = createRaddTransactionEntity();
        ResponseCheckAarDtoDto responseCheckAarDtoDto = new ResponseCheckAarDtoDto();
        responseCheckAarDtoDto.setIun("testIun");
        Mockito.when(pnDeliveryClient.getCheckAar(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(responseCheckAarDtoDto));
        Mockito.when(raddTransactionDAO.countFromIunAndOperationIdAndStatus(Mockito.any(), Mockito.any())).thenReturn(CompletableFuture.completedFuture(0));
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(request.getRecipientTaxId(), request.getRecipientType().getValue())).thenReturn(Mono.just("recipientTaxIdResult"));
        Mockito.when(pnDataVaultClient.getEnsureFiscalCode(request.getDelegateTaxId(), Const.PF)).thenReturn(Mono.just("delegateTaxIdResult"));
        Mockito.when(raddTransactionDAO.createRaddTransaction(Mockito.any())).thenReturn(Mono.just(raddTransactionEntity));
        FileDownloadResponseDto fileDownloadResponseDto = createFileDownloadResponseDto () ;
        Mockito.when(safeStorage.getFile (Mockito.any())).thenReturn(Mono.just(fileDownloadResponseDto));
        Mockito.when(safeStorage.updateFileMetadata(Mockito.any())).thenReturn(Mono.just(new OperationResultCodeResponseDto()));
        SentNotificationDto sentNotificationDto = createSentNotificationDto();
        Mockito.when(pnDeliveryClient.getNotifications(Mockito.any())).thenReturn(Mono.just(sentNotificationDto));
        NotificationAttachmentDownloadMetadataResponseDto notificationAttachmentDownloadMetadataResponseDto = new NotificationAttachmentDownloadMetadataResponseDto();
        notificationAttachmentDownloadMetadataResponseDto.setUrl("UrlDocument");
        Mockito.when(pnDeliveryClient.getPresignedUrlDocument (Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(notificationAttachmentDownloadMetadataResponseDto));
        NotificationAttachmentDownloadMetadataResponseDto notificationAttachmentDownloadMetadataResponseDto1 = new NotificationAttachmentDownloadMetadataResponseDto();
        notificationAttachmentDownloadMetadataResponseDto1.setUrl("UrlPayment");
        Mockito.when(pnDeliveryClient.getPresignedUrlPaymentDocument (Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(notificationAttachmentDownloadMetadataResponseDto1));
        Mockito.when(pnDeliveryPushInternalClient.getNotificationLegalFacts(Mockito.any(), Mockito.any())).thenReturn(Flux.fromStream(createLegalFactListElementDto()));
        LegalFactDownloadMetadataResponseDto legalFactDownloadMetadataResponseDto = new LegalFactDownloadMetadataResponseDto();
        legalFactDownloadMetadataResponseDto.setUrl("UrlLegalFact");
        Mockito.when(pnDeliveryPushInternalClient.getLegalFact(Mockito.any(), Mockito.any(), Mockito.any(),Mockito.any())).thenReturn(Mono.just(legalFactDownloadMetadataResponseDto));
        StartTransactionResponse startTransactionResponse = actService.startTransaction("test", request).block();
        assertNotNull(startTransactionResponse);
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_0, startTransactionResponse.getStatus().getCode());
        assertEquals(Const.OK, startTransactionResponse.getStatus().getMessage());
    }
}