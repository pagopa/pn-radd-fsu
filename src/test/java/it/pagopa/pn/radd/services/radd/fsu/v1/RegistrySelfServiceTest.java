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
@ContextConfiguration(classes = {RegistrySelfService.class})
class RegistrySelfServiceTest {

    @Mock
    private RaddRegistryDAO raddRegistryDAO;
    private RegistrySelfService registrySelfService;

    @BeforeEach
    void setUp() {
        registrySelfService = new RegistrySelfService(raddRegistryDAO);
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
}