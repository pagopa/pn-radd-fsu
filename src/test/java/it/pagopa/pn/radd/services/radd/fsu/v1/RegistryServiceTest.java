package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.UpdateRegistryRequest;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {RegistryService.class})
class RegistryServiceTest {

    @Mock
    private RaddRegistryDAO raddRegistryDAO;
    @Mock
    private UpdateRegistryRequest request;
    private RegistryService registryService;

    @BeforeEach
    void setUp() {
        registryService = new RegistryService(raddRegistryDAO);
    }

    @Test
    void updateRegistry() {
        RaddRegistryEntity entity = new RaddRegistryEntity();
        entity.setRegistryId("registryId");
        when(raddRegistryDAO.updateRegistryEntity(entity)).thenReturn(Mono.just(entity));
        StepVerifier.create(Mono.just(registryService.updateRegistry("registryId", request))).
            expectNext(entity).verifyComplete();
    }
}