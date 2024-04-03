package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadResponse;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.RegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.entities.PnRaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.middleware.queue.consumer.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.pojo.OriginalRequest;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RegistryService.class, PnRaddFsuConfig.class})
@ExtendWith(SpringExtension.class)
@PropertySource("classpath:application-test.properties")
@EnableConfigurationProperties
class RegistryServiceTest {
    @MockBean
    private ObjectMapperUtil objectMapperUtil;

    @MockBean
    private PnRaddFsuConfig pnRaddFsuConfig;

    @MockBean
    private PnSafeStorageClient pnSafeStorageClient;

    @MockBean
    private RegistryImportDAO registryImportDAO;

    @Autowired
    private RegistryService registryService;

    @Test
    void testUploadRegistryRequests() {
        RegistryUploadRequest request = new RegistryUploadRequest();
        request.setChecksum("checksum");
        RegistryUploadResponse registryUploadResponse = new RegistryUploadResponse();
        registryUploadResponse.setUrl("url");
        registryUploadResponse.setFileKey("key");
        registryUploadResponse.setSecret("secret");
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        FileCreationResponseDto fileCreationResponseDto = new FileCreationResponseDto();
        fileCreationResponseDto.setKey("key");
        fileCreationResponseDto.setSecret("secret");
        fileCreationResponseDto.setUploadUrl("url");
        when(registryImportDAO.getRegistryImportByCxId(any())).thenReturn(Flux.just(pnRaddRegistryImportEntity));
        when(pnSafeStorageClient.createFile(any(), any())).thenReturn(Mono.just(fileCreationResponseDto));
        when(objectMapperUtil.toJson(any())).thenReturn("string");
        when(pnRaddFsuConfig.getRegistryDefaultEndValidity()).thenReturn(1);
        when(pnRaddFsuConfig.getRegistryDefaultDeleteRule()).thenReturn("role");
        when(registryImportDAO.putRaddRegistryImportEntity(any())).thenReturn(Mono.just(pnRaddRegistryImportEntity));

        StepVerifier.create(registryService.uploadRegistryRequests("cxId", Mono.just(request)))
                        .expectNextMatches(registryUploadResponse1 -> registryUploadResponse1.getFileKey().equals("key")).verifyComplete();
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldProcessMessageSuccessfully() throws JsonProcessingException {
        RegistryService registryService = new RegistryService(pnRaddRegistryRequestDAO, pnRaddRegistryDAO);
        OriginalRequest originalRequest = new OriginalRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        RaddRegistryEntity raddRegistryEntity = mock(RaddRegistryEntity.class);
        when(raddRegistryRequestEntity.getOriginalRequest()).thenReturn(objectMapper.writeValueAsString(originalRequest));
        when(raddRegistryRequestEntity.getPk()).thenReturn("id");
        when(pnRaddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(pnRaddRegistryDAO.find(any(), any())).thenReturn(Mono.empty());
        when(pnRaddRegistryDAO.putItemIfAbsent(any())).thenReturn(Mono.just(raddRegistryEntity));
        when(pnRaddRegistryRequestDAO.updateRegistryRequestStatus(any(),any())).thenReturn(Mono.empty());
        Mono<Void> result = registryService.handleMessage(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void shouldProcessMessageSuccessfullyWithError() {
        RegistryService registryService = new RegistryService(pnRaddRegistryRequestDAO, pnRaddRegistryDAO);
        PnAddressManagerEvent.ResultItem resultItem = new PnAddressManagerEvent.ResultItem();
        resultItem.setError("error");
        resultItem.setId("id");
        List<PnAddressManagerEvent.ResultItem> resultItems = Collections.singletonList(resultItem);
        when(message.getPayload()).thenReturn(payload);
        when(payload.getResultItems()).thenReturn(resultItems);
        when(payload.getCorrelationId()).thenReturn("correlationId");
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        when(raddRegistryRequestEntity.getPk()).thenReturn("id");
        when(pnRaddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(pnRaddRegistryRequestDAO.updateStatusAndError(any(), any(), any())).thenReturn(Mono.empty());

        Mono<Void> result = registryService.handleMessage(message);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    public void shouldProcessMessageSuccessfullyWithDuplicate() throws JsonProcessingException {
        RegistryService registryService = new RegistryService(pnRaddRegistryRequestDAO, pnRaddRegistryDAO);
        OriginalRequest originalRequest = new OriginalRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        RaddRegistryEntity raddRegistryEntity = mock(RaddRegistryEntity.class);
        when(raddRegistryEntity.getRequestId()).thenReturn("requestId");
        when(raddRegistryRequestEntity.getOriginalRequest()).thenReturn(objectMapper.writeValueAsString(originalRequest));
        when(raddRegistryRequestEntity.getPk()).thenReturn("id");
        when(raddRegistryRequestEntity.getRequestId()).thenReturn("requestId");
        when(pnRaddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(pnRaddRegistryDAO.find(any(), any())).thenReturn(Mono.just(raddRegistryEntity));
        when(pnRaddRegistryRequestDAO.updateStatusAndError(any(), any(), any())).thenReturn(Mono.just(raddRegistryRequestEntity));
        when(pnRaddRegistryDAO.putItemIfAbsent(any())).thenReturn(Mono.just(raddRegistryEntity));
        Mono<Void> result = registryService.handleMessage(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void shouldProcessMessageSuccessfullyWithAlreadyExistingRegistry() throws JsonProcessingException {
        RegistryService registryService = new RegistryService(pnRaddRegistryRequestDAO, pnRaddRegistryDAO);
        OriginalRequest originalRequest = new OriginalRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        RaddRegistryEntity raddRegistryEntity = mock(RaddRegistryEntity.class);
        when(raddRegistryEntity.getRequestId()).thenReturn("requestId");
        when(raddRegistryRequestEntity.getOriginalRequest()).thenReturn(objectMapper.writeValueAsString(originalRequest));
        when(raddRegistryRequestEntity.getPk()).thenReturn("id");
        when(raddRegistryRequestEntity.getRequestId()).thenReturn("requestId2");
        when(pnRaddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        when(pnRaddRegistryDAO.find(any(), any())).thenReturn(Mono.just(raddRegistryEntity));
        when(pnRaddRegistryDAO.updateRegistryEntity(any())).thenReturn(Mono.just(raddRegistryEntity));
        when(pnRaddRegistryDAO.putItemIfAbsent(any())).thenReturn(Mono.just(raddRegistryEntity));
        when(pnRaddRegistryRequestDAO.updateRegistryRequestStatus(any(),any())).thenReturn(Mono.empty());
        Mono<Void> result = registryService.handleMessage(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void shouldProcessMessageSuccessfullyWithRelatedRegistryNotFount() throws JsonProcessingException {
        RegistryService registryService = new RegistryService(pnRaddRegistryRequestDAO, pnRaddRegistryDAO);
        OriginalRequest originalRequest = new OriginalRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        PnAddressManagerEvent pnAddressManagerEvent = getMessage();
        RaddRegistryRequestEntity raddRegistryRequestEntity = mock(RaddRegistryRequestEntity.class);
        RaddRegistryEntity raddRegistryEntity = mock(RaddRegistryEntity.class);
        when(raddRegistryEntity.getRequestId()).thenReturn("requestId");
        when(raddRegistryRequestEntity.getOriginalRequest()).thenReturn(objectMapper.writeValueAsString(originalRequest));
        when(raddRegistryRequestEntity.getPk()).thenReturn("id2");
        when(raddRegistryRequestEntity.getRequestId()).thenReturn("requestId2");
        when(pnRaddRegistryRequestDAO.findByCorrelationIdWithStatus(any(), any())).thenReturn(Flux.just(raddRegistryRequestEntity));
        Mono<Void> result = registryService.handleMessage(pnAddressManagerEvent);

        StepVerifier.create(result).verifyComplete();
    }

    private static PnAddressManagerEvent getMessage() {

        PnAddressManagerEvent.ResultItem resultItem = new PnAddressManagerEvent.ResultItem();
        resultItem.setError(null);
        resultItem.setId("id");
        resultItem.setNormalizedAddress(new PnAddressManagerEvent.NormalizedAddress());
        List<PnAddressManagerEvent.ResultItem> resultItems = Collections.singletonList(resultItem);
        PnAddressManagerEvent.Payload payload = PnAddressManagerEvent.Payload.builder().correlationId("id").resultItems(resultItems).build();
        PnAddressManagerEvent pnAddressManagerEvent = new PnAddressManagerEvent();
        pnAddressManagerEvent.setPayload(payload);
        return pnAddressManagerEvent;
    }
}

