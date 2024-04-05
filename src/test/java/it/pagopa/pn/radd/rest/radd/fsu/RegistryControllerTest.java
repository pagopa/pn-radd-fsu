package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.UpdateRegistryRequest;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryImportService;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {RegistryController.class})
class RegistryControllerTest {

    @Mock
    private RegistryService registryService;

    @InjectMocks
    private RegistryController registryController;

    @BeforeEach
    void setUp() {
        registryController = new RegistryController(registryService);
    }
    @Test
    void updateRegistry() {
        UpdateRegistryRequest request = new UpdateRegistryRequest();
        when(registryService.updateRegistry(any(), any())).thenReturn(any());
        StepVerifier.create(registryController.updateRegistry(any(), any(), any(), any(), Mono.just(request), any())).expectComplete();
    }
}