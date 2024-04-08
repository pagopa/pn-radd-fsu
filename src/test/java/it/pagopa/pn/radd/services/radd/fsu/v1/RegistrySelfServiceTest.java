package it.pagopa.pn.radd.services.radd.fsu.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryResponse;
import it.pagopa.pn.radd.mapper.RaddRegistryRequestEntityMapper;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.queue.producer.CorrelationIdEventsProducer;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
@ContextConfiguration(classes = {RegistrySelfService.class, RaddRegistryRequestEntityMapper.class})
class RegistrySelfServiceTest {

    @Mock
    private RaddRegistryRequestDAO registryRequestDAO;

    @Mock
    private CorrelationIdEventsProducer correlationIdEventsProducer;

    private RegistrySelfService registrySelfService;
    private final RaddRegistryRequestEntityMapper raddRegistryRequestEntityMapper = new RaddRegistryRequestEntityMapper(new ObjectMapperUtil(new ObjectMapper()));

    @BeforeEach
    void setUp() {
        registrySelfService = new RegistrySelfService(registryRequestDAO, raddRegistryRequestEntityMapper, correlationIdEventsProducer);
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