package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadRequest;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.middleware.queue.consumer.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.pojo.OriginalRequest;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import it.pagopa.pn.radd.utils.RaddRegistryUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static it.pagopa.pn.radd.pojo.ImportStatus.PENDING;
import static it.pagopa.pn.radd.pojo.ImportStatus.TO_PROCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {RegistryService.class})
class RegistryServiceTest {

    @Mock
    private PnRaddFsuConfig pnRaddFsuConfig;
    @Mock
    private RaddRegistryDAO raddRegistryDAO;

    @Mock
    private RaddRegistryRequestDAO raddRegistryRequestDAO;

    @Mock
    private PnSafeStorageClient pnSafeStorageClient;
    @Mock
    private RaddRegistryImportDAO raddRegistryImportDAO;
    @Mock
    private PnAddressManagerEvent message;

    @Test
    void testUploadRegistryRequests() {
        RegistryService registryService = new RegistryService(raddRegistryRequestDAO, raddRegistryDAO, raddRegistryImportDAO, pnSafeStorageClient, new RaddRegistryUtils(new ObjectMapperUtil(new com.fasterxml.jackson.databind.ObjectMapper()), pnRaddFsuConfig));
        RegistryUploadRequest request = new RegistryUploadRequest();
        request.setChecksum("checksum");
        RaddRegistryImportEntity pnRaddRegistryImportEntity = new RaddRegistryImportEntity();
        FileCreationResponseDto fileCreationResponseDto = new FileCreationResponseDto();
        fileCreationResponseDto.setKey("key");
        fileCreationResponseDto.setSecret("secret");
        fileCreationResponseDto.setUploadUrl("url");
        when(raddRegistryImportDAO.getRegistryImportByCxId(any())).thenReturn(Flux.just(pnRaddRegistryImportEntity));
        when(pnSafeStorageClient.createFile(any(), any())).thenReturn(Mono.just(fileCreationResponseDto));
        when(pnRaddFsuConfig.getRegistryDefaultEndValidity()).thenReturn(1);
        when(pnRaddFsuConfig.getRegistryDefaultDeleteRule()).thenReturn("role");
        when(raddRegistryImportDAO.putRaddRegistryImportEntity(any())).thenReturn(Mono.just(pnRaddRegistryImportEntity));

        StepVerifier.create(registryService.uploadRegistryRequests("cxId", Mono.just(request)))
                        .expectNextMatches(registryUploadResponse1 -> registryUploadResponse1.getFileKey().equals("key")).verifyComplete();
    }

    @Test
    void testUploadRegistryRequestsNotValid() {
        RegistryService registryService = new RegistryService(raddRegistryRequestDAO, raddRegistryDAO, raddRegistryImportDAO, pnSafeStorageClient, new RaddRegistryUtils(new ObjectMapperUtil(new com.fasterxml.jackson.databind.ObjectMapper()), pnRaddFsuConfig));
        RegistryUploadRequest request = new RegistryUploadRequest();
        request.setChecksum("checksum");
        RaddRegistryImportEntity pnRaddRegistryImportEntity = new RaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setStatus(TO_PROCESS.name());
        pnRaddRegistryImportEntity.setChecksum("checksum");
        pnRaddRegistryImportEntity.setFileUploadDueDate(Instant.now().minus(10, ChronoUnit.DAYS));
        when(raddRegistryImportDAO.getRegistryImportByCxId(any())).thenReturn(Flux.just(pnRaddRegistryImportEntity));

        StepVerifier.create(registryService.uploadRegistryRequests("cxId", Mono.just(request)))
                .expectErrorMessage("Richiesta Duplicata. il file inviato è già in fase di elaborazione").verify();
    }

    @Test
    void testUploadRegistryRequestsNotValid2() {
        RegistryService registryService = new RegistryService(raddRegistryRequestDAO, raddRegistryDAO, raddRegistryImportDAO, pnSafeStorageClient, new RaddRegistryUtils(new ObjectMapperUtil(new com.fasterxml.jackson.databind.ObjectMapper()), pnRaddFsuConfig));
        RegistryUploadRequest request = new RegistryUploadRequest();
        request.setChecksum("checksum");
        RaddRegistryImportEntity pnRaddRegistryImportEntity = new RaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setStatus(PENDING.name());
        when(raddRegistryImportDAO.getRegistryImportByCxId(any())).thenReturn(Flux.just(pnRaddRegistryImportEntity));

        StepVerifier.create(registryService.uploadRegistryRequests("cxId", Mono.just(request)))
                .expectErrorMessage("Una precedente richiesta di import è ancora in corso").verify();
    }

    @Test
    public void shouldProcessMessageSuccessfully() throws JsonProcessingException {
        RegistryService registryService = new RegistryService(raddRegistryRequestDAO, raddRegistryDAO, raddRegistryImportDAO, pnSafeStorageClient, new RaddRegistryUtils(new ObjectMapperUtil(new com.fasterxml.jackson.databind.ObjectMapper()), pnRaddFsuConfig));

        OriginalRequest originalRequest = new OriginalRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        RaddRegistryEntity raddRegistryEntity = mock(RaddRegistryEntity.class);
        when(raddRegistryRequestEntity.getOriginalRequest()).thenReturn(objectMapper.writeValueAsString(originalRequest));
        when(raddRegistryRequestEntity.getPk()).thenReturn("cxId#requestId#addressId");
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(any(), any(), any())).thenReturn(Flux.just(new RaddRegistryImportEntity()));
        when(raddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(raddRegistryDAO.find(any(), any())).thenReturn(Mono.empty());
        when(raddRegistryDAO.putItemIfAbsent(any())).thenReturn(Mono.just(raddRegistryEntity));
        when(raddRegistryRequestDAO.updateRegistryRequestStatus(any(),any())).thenReturn(Mono.empty());
        Mono<Void> result = registryService.handleMessage(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void shouldProcessMessageSuccessfullyWithError() {
        RegistryService registryService = new RegistryService(raddRegistryRequestDAO, raddRegistryDAO, raddRegistryImportDAO, pnSafeStorageClient, new RaddRegistryUtils(new ObjectMapperUtil(new com.fasterxml.jackson.databind.ObjectMapper()), pnRaddFsuConfig));

        PnAddressManagerEvent.ResultItem resultItem = new PnAddressManagerEvent.ResultItem();
        resultItem.setError("error");
        resultItem.setId("cxId#requestId#addressId");
        List<PnAddressManagerEvent.ResultItem> resultItems = Collections.singletonList(resultItem);
        PnAddressManagerEvent.Payload payload = mock(PnAddressManagerEvent.Payload.class);
        when(message.getPayload()).thenReturn(payload);
        when(payload.getResultItems()).thenReturn(resultItems);
        when(payload.getCorrelationId()).thenReturn("correlationId");
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        when(raddRegistryRequestEntity.getPk()).thenReturn("cxId#requestId#addressId");
        when(raddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(any(), any(), any())).thenReturn(Flux.just(new RaddRegistryImportEntity()));
        when(raddRegistryRequestDAO.updateStatusAndError(any(), any(), any())).thenReturn(Mono.just(raddRegistryRequestEntity));

        Mono<Void> result = registryService.handleMessage(message);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    public void shouldProcessMessageSuccessfullyWithDuplicate(){
        RegistryService registryService = new RegistryService(raddRegistryRequestDAO, raddRegistryDAO, raddRegistryImportDAO, pnSafeStorageClient, new RaddRegistryUtils(new ObjectMapperUtil(new com.fasterxml.jackson.databind.ObjectMapper()), pnRaddFsuConfig));

        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        RaddRegistryEntity raddRegistryEntity = mock(RaddRegistryEntity.class);
        when(raddRegistryEntity.getRequestId()).thenReturn("requestId");
        when(raddRegistryRequestEntity.getPk()).thenReturn("cxId#requestId#addressId");
        when(raddRegistryRequestEntity.getRequestId()).thenReturn("requestId");
        when(raddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(raddRegistryDAO.find(any(), any())).thenReturn(Mono.just(raddRegistryEntity));
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(any(), any(), any())).thenReturn(Flux.just(new RaddRegistryImportEntity()));
        when(raddRegistryRequestDAO.updateStatusAndError(any(), any(), any())).thenReturn(Mono.just(raddRegistryRequestEntity));
        Mono<Void> result = registryService.handleMessage(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void shouldProcessMessageSuccessfullyWithExistingOldRegistry() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        RegistryService registryService = new RegistryService(raddRegistryRequestDAO, raddRegistryDAO, raddRegistryImportDAO, pnSafeStorageClient, new RaddRegistryUtils(new ObjectMapperUtil(new com.fasterxml.jackson.databind.ObjectMapper()), pnRaddFsuConfig));
        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        RaddRegistryEntity raddRegistryEntity = mock(RaddRegistryEntity.class);
        OriginalRequest originalRequest = new OriginalRequest();
        when(raddRegistryRequestEntity.getOriginalRequest()).thenReturn(objectMapper.writeValueAsString(originalRequest));
        when(raddRegistryEntity.getRequestId()).thenReturn("requestIdOld");
        when(raddRegistryRequestEntity.getPk()).thenReturn("cxId#requestId#addressId");
        when(raddRegistryRequestEntity.getRequestId()).thenReturn("requestId");
        when(raddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(raddRegistryDAO.find(any(), any())).thenReturn(Mono.just(raddRegistryEntity));
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(any(), any(), any())).thenReturn(Flux.just(new RaddRegistryImportEntity()));
        when(raddRegistryDAO.updateRegistryEntity(any())).thenReturn(Mono.just(raddRegistryEntity));
        when(raddRegistryRequestDAO.updateRegistryRequestStatus(any(),any())).thenReturn(Mono.empty());
        when(raddRegistryDAO.putItemIfAbsent(any())).thenReturn(Mono.just(raddRegistryEntity));
        Mono<Void> result = registryService.handleMessage(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }


    @Test
    public void shouldProcessMessageSuccessfullyWithRelatedRegistryNotFount() throws JsonProcessingException {
        RegistryService registryService = new RegistryService(raddRegistryRequestDAO, raddRegistryDAO, raddRegistryImportDAO, pnSafeStorageClient, new RaddRegistryUtils(new ObjectMapperUtil(new com.fasterxml.jackson.databind.ObjectMapper()), pnRaddFsuConfig));

        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        when(raddRegistryRequestEntity.getPk()).thenReturn("id2");
        when(raddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(any(), any(), any())).thenReturn(Flux.just(new RaddRegistryImportEntity()));
        Mono<Void> result = registryService.handleMessage(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void shouldProcessMessageSuccessfullyWithImportNotFound() {
        RegistryService registryService = new RegistryService(raddRegistryRequestDAO, raddRegistryDAO, raddRegistryImportDAO, pnSafeStorageClient, new RaddRegistryUtils(new ObjectMapperUtil(new com.fasterxml.jackson.databind.ObjectMapper()), pnRaddFsuConfig));
        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(any(), any(), any())).thenReturn(Flux.empty());
        Mono<Void> result = registryService.handleMessage(pnAddressManagerEvent);
        StepVerifier.create(result).verifyComplete();
    }

    private static PnAddressManagerEvent getMessage() {

        PnAddressManagerEvent.ResultItem resultItem = new PnAddressManagerEvent.ResultItem();
        resultItem.setError(null);
        resultItem.setId("cxId#requestId#addressId");
        resultItem.setNormalizedAddress(new PnAddressManagerEvent.NormalizedAddress());
        List<PnAddressManagerEvent.ResultItem> resultItems = Collections.singletonList(resultItem);
        PnAddressManagerEvent.Payload payload = PnAddressManagerEvent.Payload.builder().correlationId("id").resultItems(resultItems).build();
        PnAddressManagerEvent pnAddressManagerEvent = new PnAddressManagerEvent();
        pnAddressManagerEvent.setPayload(payload);
        return pnAddressManagerEvent;
    }

}