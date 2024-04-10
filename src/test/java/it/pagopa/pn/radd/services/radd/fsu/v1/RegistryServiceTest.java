package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.api.dto.events.PnEvaluatedZipCodeEvent;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.AcceptedResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadRequest;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.eventbus.EventBridgeProducer;
import it.pagopa.pn.radd.middleware.msclient.PnAddressManagerClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.middleware.queue.consumer.event.ImportCompletedRequestEvent;
import it.pagopa.pn.radd.middleware.queue.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.middleware.queue.event.PnInternalCapCheckerEvent;
import it.pagopa.pn.radd.middleware.queue.event.PnRaddAltNormalizeRequestEvent;
import it.pagopa.pn.radd.middleware.queue.producer.RaddAltCapCheckerProducer;
import it.pagopa.pn.radd.pojo.RaddRegistryImportConfig;
import it.pagopa.pn.radd.pojo.RaddRegistryImportStatus;
import it.pagopa.pn.radd.pojo.RaddRegistryOriginalRequest;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import it.pagopa.pn.radd.utils.RaddRegistryUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
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

import static it.pagopa.pn.radd.pojo.RaddRegistryImportStatus.PENDING;
import static it.pagopa.pn.radd.pojo.RaddRegistryImportStatus.TO_PROCESS;
import static it.pagopa.pn.radd.utils.Const.ERROR_DUPLICATE;
import static it.pagopa.pn.radd.utils.Const.REQUEST_ID_PREFIX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


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
    @Mock
    private PnAddressManagerClient pnAddressManagerClient;

    @Mock
    private RaddAltCapCheckerProducer raddAltCapCheckerProducer;

    @Mock
    private PnRaddAltNormalizeRequestEvent.Payload payload;

    @Mock
    private SecretService secretService;

    @Mock
    private EventBridgeProducer<PnEvaluatedZipCodeEvent> eventBridgeProducer;

    @Mock
    private ObjectMapperUtil objectMapperUtil;


    private RegistryService registryService;

    @BeforeEach
    void setUp() {
        registryService = new RegistryService(raddRegistryRequestDAO, raddRegistryDAO, raddRegistryImportDAO, pnSafeStorageClient, new RaddRegistryUtils(new ObjectMapperUtil(new com.fasterxml.jackson.databind.ObjectMapper()), pnRaddFsuConfig, secretService), pnAddressManagerClient, raddAltCapCheckerProducer, pnRaddFsuConfig, eventBridgeProducer, objectMapperUtil);
    }

    @Test
    void testUploadRegistryRequests() {
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

        RaddRegistryOriginalRequest raddRegistryOriginalRequest = new RaddRegistryOriginalRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        RaddRegistryEntity raddRegistryEntity = mock(RaddRegistryEntity.class);
        when(raddRegistryRequestEntity.getOriginalRequest()).thenReturn(objectMapper.writeValueAsString(raddRegistryOriginalRequest));
        when(raddRegistryRequestEntity.getPk()).thenReturn("cxId#requestId#addressId");
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(any(), any(), any())).thenReturn(Flux.just(new RaddRegistryImportEntity()));
        when(raddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(raddRegistryDAO.find(any(), any())).thenReturn(Mono.empty());
        when(raddRegistryDAO.putItemIfAbsent(any())).thenReturn(Mono.just(raddRegistryEntity));
        when(raddRegistryRequestDAO.updateRegistryRequestStatus(any(), any())).thenReturn(Mono.empty());
        Mono<Void> result = registryService.handleAddressManagerEvent(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void shouldProcessMessageWithDuplicate() throws JsonProcessingException {

        RaddRegistryOriginalRequest raddRegistryOriginalRequest = new RaddRegistryOriginalRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        when(raddRegistryRequestEntity.getOriginalRequest()).thenReturn(objectMapper.writeValueAsString(raddRegistryOriginalRequest));
        when(raddRegistryRequestEntity.getPk()).thenReturn("cxId#requestId#addressId");
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(any(), any(), any())).thenReturn(Flux.just(new RaddRegistryImportEntity()));
        when(raddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(raddRegistryDAO.find(any(), any())).thenReturn(Mono.empty());
        when(raddRegistryDAO.putItemIfAbsent(any())).thenReturn(Mono.error(new RaddGenericException(ERROR_DUPLICATE)));
        when(raddRegistryRequestDAO.updateStatusAndError(any(), any(), any())).thenReturn(Mono.just(raddRegistryRequestEntity));
        Mono<Void> result = registryService.handleAddressManagerEvent(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void shouldProcessMessageSuccessfullyWithError() {

        PnAddressManagerEvent.ResultItem resultItem = new PnAddressManagerEvent.ResultItem();
        resultItem.setError("error");
        resultItem.setId("cxId#requestId#addressId");

        Mono<Void> result = registryService.handleAddressManagerEvent(message);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testVerifyRegistryRequests_ValidCase() {

        RaddRegistryImportEntity pnRaddRegistryImportEntity = new RaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setStatus("DONE");
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestId(any(), any())).thenReturn(Mono.just(pnRaddRegistryImportEntity));

        StepVerifier.create(registryService.verifyRegistriesImportRequest("cxId", "requestId"))
                .expectNextMatches(response -> response.getStatus().equals("DONE") && StringUtils.isBlank(response.getError()))
                .verifyComplete();
    }

    @Test
    void testVerifyRegistryRequests_ValidCaseWithError() {

        RaddRegistryImportEntity pnRaddRegistryImportEntity = new RaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setStatus("REJECTED");
        pnRaddRegistryImportEntity.setError("error");
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestId(any(), any())).thenReturn(Mono.just(pnRaddRegistryImportEntity));

        StepVerifier.create(registryService.verifyRegistriesImportRequest("cxId", "requestId"))
                .expectNextMatches(response -> response.getStatus().equals("REJECTED") && response.getError().equals("error"))
                .verifyComplete();
    }


    @Test
    void testVerifyRegistryRequests_ExceptionCase() {

        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestId(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(registryService.verifyRegistriesImportRequest("cxId", "requestId"))
                .expectErrorMessage("Richiesta di import non trovata")
                .verify();
    }

    @Test
    public void shouldProcessMessageSuccessfullyWithDuplicate() {
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
        Mono<Void> result = registryService.handleAddressManagerEvent(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void shouldProcessMessageSuccessfullyWithExistingOldRegistry() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        RaddRegistryEntity raddRegistryEntity = mock(RaddRegistryEntity.class);
        RaddRegistryOriginalRequest raddRegistryOriginalRequest = new RaddRegistryOriginalRequest();
        when(raddRegistryRequestEntity.getOriginalRequest()).thenReturn(objectMapper.writeValueAsString(raddRegistryOriginalRequest));
        when(raddRegistryEntity.getRequestId()).thenReturn("requestIdOld");
        when(raddRegistryRequestEntity.getPk()).thenReturn("cxId#requestId#addressId");
        when(raddRegistryRequestEntity.getRequestId()).thenReturn("requestId");
        when(raddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(raddRegistryDAO.find(any(), any())).thenReturn(Mono.just(raddRegistryEntity));
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(any(), any(), any())).thenReturn(Flux.just(new RaddRegistryImportEntity()));
        when(raddRegistryDAO.updateRegistryEntity(any())).thenReturn(Mono.just(raddRegistryEntity));
        when(raddRegistryRequestDAO.updateRegistryRequestStatus(any(), any())).thenReturn(Mono.empty());
        when(raddRegistryDAO.putItemIfAbsent(any())).thenReturn(Mono.just(raddRegistryEntity));
        Mono<Void> result = registryService.handleAddressManagerEvent(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }


    @Test
    public void shouldProcessMessageSuccessfullyWithRelatedRegistryNotFount() {

        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        when(raddRegistryRequestEntity.getPk()).thenReturn("id2");
        when(raddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(any(), any(), any())).thenReturn(Flux.just(new RaddRegistryImportEntity()));
        Mono<Void> result = registryService.handleAddressManagerEvent(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void shouldProcessMessageSuccessfullyWithImportNotFound() {
        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(any(), any(), any())).thenReturn(Flux.empty());
        Mono<Void> result = registryService.handleAddressManagerEvent(pnAddressManagerEvent);
        StepVerifier.create(result).verifyComplete();
    }

    private static PnAddressManagerEvent getMessage() {

        PnAddressManagerEvent.ResultItem resultItem = new PnAddressManagerEvent.ResultItem();
        resultItem.setError(null);
        resultItem.setId("addressId");
        resultItem.setNormalizedAddress(new PnAddressManagerEvent.NormalizedAddress());
        List<PnAddressManagerEvent.ResultItem> resultItems = Collections.singletonList(resultItem);
        PnAddressManagerEvent payload = new PnAddressManagerEvent();
        payload.setCorrelationId("id");
        payload.setResultItems(resultItems);
        return payload;
    }

    @Test
    public void shouldHandleRequestSuccessfully() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        when(payload.getCorrelationId()).thenReturn("correlationId");
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        when(raddRegistryRequestEntity.getPk()).thenReturn("cxId#requestId#addressId");
        RaddRegistryOriginalRequest raddRegistryOriginalRequest = new RaddRegistryOriginalRequest();
        raddRegistryOriginalRequest.setGeoLocation("test");
        raddRegistryOriginalRequest.setPr("RM");
        when(raddRegistryRequestEntity.getOriginalRequest()).thenReturn(objectMapper.writeValueAsString(raddRegistryOriginalRequest));
        when(raddRegistryRequestDAO.getAllFromCorrelationId(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(pnAddressManagerClient.normalizeAddresses(any(), any())).thenReturn(Mono.just(new AcceptedResponseDto()));
        when(raddRegistryRequestDAO.updateRecordsInPending(any())).thenReturn(Mono.empty());

        StepVerifier.create(registryService.handleNormalizeRequestEvent(payload)).verifyComplete();
    }

    @Test
    void handleImportCompletedRequest() {
        ImportCompletedRequestEvent.Payload payload = ImportCompletedRequestEvent.Payload.builder().cxId("cxId").requestId("requestId").build();
        when(raddRegistryRequestDAO.getAllFromCxidAndRequestIdWithState("cxId", "requestId", RegistryRequestStatus.ACCEPTED.name()))
                .thenReturn(Flux.just(mock(RaddRegistryRequestEntity.class)));
        StepVerifier.create(registryService.handleImportCompletedRequest(payload)).expectComplete();
    }

    @Test
    void testDeleteOlderRequestRegistriesAndGetCapListForFirstImportRequest() {
        String xPagopaPnCxId = "testCxId";
        String requestId = "testRequestId";

        RaddRegistryImportEntity raddRegistryImportEntity = new RaddRegistryImportEntity();
        raddRegistryImportEntity.setConfig("json");

        RaddRegistryEntity raddRegistryEntity = new RaddRegistryEntity();
        raddRegistryEntity.setRegistryId("testRegistryId");
        raddRegistryEntity.setZipCode("00100");

        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDeleteRole("duplicate");
        raddRegistryImportConfig.setDefaultEndValidity(1);

        RaddRegistryRequestEntity raddRegistryRequestEntity = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity.setPk("cxId#requestId#index");

        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(xPagopaPnCxId, requestId, RaddRegistryImportStatus.DONE))
                .thenReturn(Flux.just(raddRegistryImportEntity));
        when(raddRegistryDAO.findByCxIdAndRequestId(xPagopaPnCxId, REQUEST_ID_PREFIX))
                .thenReturn(Flux.just(raddRegistryEntity));
        when(objectMapperUtil.toObject(raddRegistryImportEntity.getConfig(), RaddRegistryImportConfig.class))
                .thenReturn(raddRegistryImportConfig);
        when(raddRegistryDAO.updateRegistryEntity(raddRegistryEntity))
                .thenReturn(Mono.just(raddRegistryEntity));
        when(raddRegistryRequestDAO.findByCxIdAndRegistryId(raddRegistryEntity.getCxId(), raddRegistryEntity.getRegistryId()))
                .thenReturn(Flux.just(raddRegistryRequestEntity));
        when(raddRegistryRequestDAO.putRaddRegistryRequestEntity(any()))
                .thenReturn(Mono.just(raddRegistryRequestEntity));

        Flux<String> result = registryService.deleteOlderRegistriesAndGetZipCodeList(xPagopaPnCxId, requestId);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        verify(raddRegistryImportDAO, times(1)).getRegistryImportByCxIdAndRequestIdFilterByStatus(xPagopaPnCxId, requestId, RaddRegistryImportStatus.DONE);
        verify(raddRegistryDAO, times(1)).findByCxIdAndRequestId(xPagopaPnCxId, REQUEST_ID_PREFIX);
        verify(raddRegistryDAO, times(1)).updateRegistryEntity(raddRegistryEntity);
        verify(raddRegistryRequestDAO, times(1)).findByCxIdAndRegistryId(raddRegistryEntity.getCxId(), raddRegistryEntity.getRegistryId());
        verify(raddRegistryRequestDAO, times(1)).putRaddRegistryRequestEntity(any());
    }

    @Test
    void testDeleteOlderRequestRegistriesAndGetCapListForSubsequentImportRequest() {
        String xPagopaPnCxId = "testCxId";
        String requestId = "testNewRequestId";
        String oldRequestId = "testOldRequestId";

        RaddRegistryImportEntity newRaddRegistryImportEntity = new RaddRegistryImportEntity();
        newRaddRegistryImportEntity.setRequestId(requestId);
        newRaddRegistryImportEntity.setCxId(xPagopaPnCxId);
        newRaddRegistryImportEntity.setStatus(RaddRegistryImportStatus.DONE.name());

        RaddRegistryImportEntity oldRaddRegistryImportEntity = new RaddRegistryImportEntity();
        oldRaddRegistryImportEntity.setRequestId(oldRequestId);
        oldRaddRegistryImportEntity.setCxId(xPagopaPnCxId);
        oldRaddRegistryImportEntity.setStatus(RaddRegistryImportStatus.DONE.name());

        RaddRegistryEntity raddRegistryEntityMadeByCrud = new RaddRegistryEntity();
        raddRegistryEntityMadeByCrud.setRegistryId(REQUEST_ID_PREFIX + "registryId");
        raddRegistryEntityMadeByCrud.setZipCode("00100");

        RaddRegistryEntity raddRegistryEntityMadeByOldImport = new RaddRegistryEntity();
        raddRegistryEntityMadeByOldImport.setRegistryId(oldRequestId);
        raddRegistryEntityMadeByOldImport.setZipCode("00200");

        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDeleteRole("differentFromDuplicate");
        raddRegistryImportConfig.setDefaultEndValidity(1);

        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(xPagopaPnCxId, requestId, RaddRegistryImportStatus.DONE))
                .thenReturn(Flux.just(newRaddRegistryImportEntity, oldRaddRegistryImportEntity));
        when(raddRegistryDAO.findByCxIdAndRequestId(xPagopaPnCxId, oldRequestId))
                .thenReturn(Flux.just(raddRegistryEntityMadeByOldImport));
        when(raddRegistryDAO.findByCxIdAndRequestId(xPagopaPnCxId, REQUEST_ID_PREFIX))
                .thenReturn(Flux.just(raddRegistryEntityMadeByCrud));
        when(raddRegistryImportDAO.updateStatusAndTtl(any(), any(), any()))
                .thenReturn(Mono.just(newRaddRegistryImportEntity));
        when(objectMapperUtil.toObject(newRaddRegistryImportEntity.getConfig(), RaddRegistryImportConfig.class))
                .thenReturn(raddRegistryImportConfig);
        when(raddRegistryDAO.updateRegistryEntity(any()))
                .thenReturn(Mono.just(new RaddRegistryEntity()));

        Flux<String> result = registryService.deleteOlderRegistriesAndGetZipCodeList(xPagopaPnCxId, requestId);

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();

        verify(raddRegistryImportDAO, times(1)).getRegistryImportByCxIdAndRequestIdFilterByStatus(xPagopaPnCxId, requestId, RaddRegistryImportStatus.DONE);
        verify(raddRegistryDAO, times(1)).findByCxIdAndRequestId(xPagopaPnCxId, REQUEST_ID_PREFIX);
        verify(raddRegistryDAO, times(1)).findByCxIdAndRequestId(xPagopaPnCxId, oldRequestId);
        verify(raddRegistryDAO, times(2)).updateRegistryEntity(any());
        verify(raddRegistryRequestDAO, times(0)).findByCxIdAndRegistryId(anyString(), anyString());
        verify(raddRegistryRequestDAO, times(0)).putRaddRegistryRequestEntity(any());
    }

    @Test
    void testDeleteOlderRequestRegistriesAndGetCapListFails() {
        String xPagopaPnCxId = "testCxId";
        String requestId = "testNewRequestId";

        when(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestIdFilterByStatus(xPagopaPnCxId, requestId, RaddRegistryImportStatus.DONE))
                .thenReturn(Flux.empty());

        Flux<String> result = registryService.deleteOlderRegistriesAndGetZipCodeList(xPagopaPnCxId, requestId);

        StepVerifier.create(result)
                .expectError(RaddGenericException.class)
                .verify();

        verify(raddRegistryImportDAO, times(1)).getRegistryImportByCxIdAndRequestIdFilterByStatus(xPagopaPnCxId, requestId, RaddRegistryImportStatus.DONE);
        verify(raddRegistryDAO, times(0)).findByCxIdAndRequestId(xPagopaPnCxId, REQUEST_ID_PREFIX);
    }


    @Test
    public void handleInternalCapCheckerMessageTest() {
        PnInternalCapCheckerEvent event = new PnInternalCapCheckerEvent();
        PnInternalCapCheckerEvent.Payload payload = new PnInternalCapCheckerEvent.Payload("zipCode");
        event.setPayload(payload);
        Instant start = Instant.now();
        Instant end = Instant.now();
        RaddRegistryEntity raddRegistryEntity = new RaddRegistryEntity();
        raddRegistryEntity.setZipCode("zipCode");
        raddRegistryEntity.setStartValidity(start);
        raddRegistryEntity.setEndValidity(end);
        when(raddRegistryDAO.getRegistriesByZipCode(any())).thenReturn(Flux.just(raddRegistryEntity));

        StepVerifier.create(registryService.handleInternalCapCheckerMessage(event)).expectComplete();
    }

}