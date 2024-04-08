package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.queue.producer.RaddAltCapCheckerProducer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {AnagraficaService.class})
class AnagraficaServiceTest {

    private AnagraficaService anagraficaService;

    @Mock
    private RaddRegistryDAO raddRegistryDAO;

    @Mock
    private PnRaddFsuConfig pnRaddFsuConfig;

    @Mock
    private RaddAltCapCheckerProducer raddAltCapCheckerProducer;

    private static final String xPagopaPnCxId = "xPagopaPnCxId";

    @BeforeEach
    void setUp() {
        anagraficaService = new AnagraficaService(raddRegistryDAO, pnRaddFsuConfig, raddAltCapCheckerProducer);
    }

    @Test
    void testDeleteRegistry() {
        String registryId = "testRegistryId";
        String endDate = "2025-12-31T23:59:59Z";

        RaddRegistryEntity registryEntity = getRaddRegistryEntity();


        when(raddRegistryDAO.find(anyString(),anyString())).thenReturn(Mono.just(registryEntity));
        when(raddRegistryDAO.updateRegistryEntity(any(RaddRegistryEntity.class))).thenReturn(Mono.just(registryEntity));

        StepVerifier.create(anagraficaService.deleteRegistry(xPagopaPnCxId, registryId, endDate))
                .expectNext(registryEntity)
                .verifyComplete();
    }

    private static @NotNull RaddRegistryEntity getRaddRegistryEntity() {
        RaddRegistryEntity registryEntity = new RaddRegistryEntity();
        registryEntity.setRegistryId("testRegistryId");
        registryEntity.setCxId(xPagopaPnCxId);
        registryEntity.setRequestId("testRequestId");
        registryEntity.setNormalizedAddress("testNormalizedAddress");
        registryEntity.setDescription("testDescription");
        registryEntity.setPhoneNumber("testPhoneNumber");
        registryEntity.setGeoLocation("testGeoLocation");
        registryEntity.setZipCode("00100");
        registryEntity.setOpeningTime("testOpeningTime");
        registryEntity.setStartValidity(Instant.parse("2020-04-06T12:00:00Z"));
        registryEntity.setEndValidity(Instant.parse("2025-10-06T12:00:00Z"));
        return registryEntity;
    }

    @Test
    void testDeleteRegistryWhenRegistryNotFound() {
        String registryId = "testRegistryId";
        String endDate = "2022-12-31T23:59:59Z";

        when(raddRegistryDAO.find(registryId, xPagopaPnCxId)).thenReturn(Mono.empty());

        StepVerifier.create(anagraficaService.deleteRegistry(xPagopaPnCxId, registryId, endDate))
                .expectError(RaddGenericException.class)
                .verify();
    }

    @Test
    void testDeleteRegistryWhenInvalidDate() {
        String registryId = "testRegistryId";
        String endDate = "2020-12-31T23:59:59Z";

        RaddRegistryEntity registryEntity = new RaddRegistryEntity();
        registryEntity.setZipCode("00100");

        when(raddRegistryDAO.find(registryId, xPagopaPnCxId)).thenReturn(Mono.just(registryEntity));

        StepVerifier.create(anagraficaService.deleteRegistry(xPagopaPnCxId, registryId, endDate))
                .expectError(RaddGenericException.class)
                .verify();
    }
}