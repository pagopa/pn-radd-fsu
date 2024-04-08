package it.pagopa.pn.radd.services.radd.fsu.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.UpdateRegistryRequest;
import it.pagopa.pn.radd.mapper.RaddRegistryRequestEntityMapper;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.queue.producer.CorrelationIdEventsProducer;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {RegistrySelfService.class})
class RegistrySelfServiceTest {

    @Mock
    private RaddRegistryDAO raddRegistryDAO;

    @Mock
    private RaddRegistryRequestDAO registryRequestDAO;

    @Mock
    private CorrelationIdEventsProducer correlationIdEventsProducer;

    private final RaddRegistryRequestEntityMapper raddRegistryRequestEntityMapper = new RaddRegistryRequestEntityMapper(new ObjectMapperUtil(new ObjectMapper()));

    private RegistrySelfService registrySelfService;


    @BeforeEach
    void setUp() {
        registrySelfService = new RegistrySelfService(raddRegistryDAO, registryRequestDAO, raddRegistryRequestEntityMapper, correlationIdEventsProducer);
    }

    @Test
    void updateRegistryNotFound() {
        UpdateRegistryRequest updateRegistryRequest = new UpdateRegistryRequest();
        when(raddRegistryDAO.find("registryId", "cxId")).thenReturn(Mono.empty());
        StepVerifier.create(registrySelfService.updateRegistry("registryId", "cxId", updateRegistryRequest))
                .verifyErrorMessage("Punto di ritiro SEND non trovato");
    }

    @Test
    void updateRegistry() {
        UpdateRegistryRequest updateRegistryRequest = new UpdateRegistryRequest();
        updateRegistryRequest.setDescription("description");
        updateRegistryRequest.setOpeningTime("openingTime");
        updateRegistryRequest.setPhoneNumber("phoneNumber");
        RaddRegistryEntity entity = new RaddRegistryEntity();
        entity.setRegistryId("registryId");
        when(raddRegistryDAO.find("registryId", "cxId")).thenReturn(Mono.just(entity));
        when(raddRegistryDAO.updateRegistryEntity(entity)).thenReturn(Mono.just(entity));
        StepVerifier.create(registrySelfService.updateRegistry("registryId", "cxId", updateRegistryRequest))
                .expectNextMatches(raddRegistryEntity -> entity.getDescription().equalsIgnoreCase("description")
                        && entity.getOpeningTime().equalsIgnoreCase("openingTime")
                        && entity.getPhoneNumber().equalsIgnoreCase("phoneNumber"))
                .verifyComplete();
    }

    @Test
    public void shouldAddRegistrySuccessfully() {
        CreateRegistryRequest request = new CreateRegistryRequest();
        RaddRegistryRequestEntity entity = new RaddRegistryRequestEntity();
        entity.setRequestId("testRequestId");
        when(registryRequestDAO.createEntity(any())).thenReturn(Mono.just(entity));
        doNothing().when(correlationIdEventsProducer).sendCorrelationIdEvent(any());

        Mono<CreateRegistryResponse> result = registrySelfService.addRegistry("cxId", request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("testRequestId", response.getRequestId());
                })
                .verifyComplete();
    }
}