package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.config.RestExceptionHandler;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.entities.BiasPointEntity;
import it.pagopa.pn.radd.middleware.db.entities.NormalizedAddressEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntityV2;
import it.pagopa.pn.radd.pojo.PnLastEvaluatedKey;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CustomLog
class RaddRegistryV2DAOImplTest extends BaseTest.WithLocalStack {
    @Autowired
    @SpyBean
    private RaddRegistryV2DAO raddRegistryDAO;

    @Autowired
    @SpyBean
    private RestExceptionHandler exceptionHandler;

    @Autowired
    private BaseDao<RaddRegistryEntityV2> baseDao;
    @Mock
    DynamoDbAsyncClient dynamoDbAsyncClient;
    @Mock
    DynamoDbAsyncTable<RaddRegistryEntityV2> raddRegistryImportEntityDynamoDbAsyncTable;

    private RaddRegistryEntityV2 buildEntity() {
        RaddRegistryEntityV2 entity = new RaddRegistryEntityV2();
        entity.setPartnerId("partner-" + UUID.randomUUID());
        entity.setLocationId("loc-" + UUID.randomUUID());
        entity.setExternalCodes(List.of("EXT1", "EXT2"));
        entity.setDescription("Test description");
        entity.setEmail("test@example.com");
        entity.setPhoneNumbers(List.of("123456789"));
        entity.setOpeningTime("08:00-18:00");
        entity.setStartValidity(Instant.now());
        entity.setEndValidity(Instant.now().plusSeconds(3600));
        entity.setAppointmentRequired(Boolean.TRUE);
        entity.setWebsite("https://test.it");
        entity.setPartnerType("TYPE1");

        NormalizedAddressEntity normalizedAddress = new NormalizedAddressEntity();
        normalizedAddress.setAddressRow("123 Test St");
        normalizedAddress.setCity("Test City");
        normalizedAddress.setProvince("TP");
        normalizedAddress.setCountry("Italy");
        normalizedAddress.setLongitude("12.345678");
        normalizedAddress.setLatitude("34.567890");

        BiasPointEntity biasPoint = new BiasPointEntity();
        biasPoint.setOverall(BigDecimal.ONE);
        biasPoint.setAddressNumber(BigDecimal.ONE);
        biasPoint.setLocality(BigDecimal.ONE);
        biasPoint.setSubRegion(BigDecimal.ONE);
        biasPoint.setPostalCode(BigDecimal.ONE);
        biasPoint.setCountry(BigDecimal.ONE);
        normalizedAddress.setBiasPoint(biasPoint);

        entity.setNormalizedAddress(normalizedAddress);

        entity.setCreationTimestamp(Instant.now());
        entity.setUpdateTimestamp(Instant.now());
        entity.setUpdateTimestamp(Instant.now());
        return entity;
    }

    @Test
    void shouldInsertAndFindEntity() {
        // given
        RaddRegistryEntityV2 entity = buildEntity();

        // when + then
        StepVerifier.create(raddRegistryDAO.putItemIfAbsent(entity))
                .assertNext(inserted -> assertThat(inserted).isEqualTo(entity))
                .verifyComplete();

        StepVerifier.create(raddRegistryDAO.find(entity.getPartnerId(), entity.getLocationId()))
                .assertNext(found -> assertThat(found).isEqualTo(entity))
                .verifyComplete();
    }

    @Test
    void shouldUpdateEntityDescription() {
        // given
        RaddRegistryEntityV2 entity = buildEntity();
        entity.setDescription("Initial");

        StepVerifier.create(raddRegistryDAO.putItemIfAbsent(entity))
                .assertNext(inserted -> assertThat(inserted.getDescription()).isEqualTo("Initial"))
                .verifyComplete();

        // when
        entity.setDescription("Updated");

        // then
        StepVerifier.create(raddRegistryDAO.updateRegistryEntity(entity))
                .assertNext(updated -> assertThat(updated.getDescription()).isEqualTo("Updated"))
                .verifyComplete();
    }

    @Test
    void shouldDeleteEntityAndNotFindIt() {
        // given
        RaddRegistryEntityV2 entity = buildEntity();

        StepVerifier.create(raddRegistryDAO.putItemIfAbsent(entity))
                .assertNext(inserted -> assertThat(inserted).isEqualTo(entity))
                .verifyComplete();

        // when
        StepVerifier.create(raddRegistryDAO.delete(entity.getPartnerId(), entity.getLocationId()))
                .assertNext(deleted -> assertThat(deleted).isEqualTo(entity))
                .verifyComplete();

        // then
        StepVerifier.create(raddRegistryDAO.find(entity.getPartnerId(), entity.getLocationId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldFindAllEntitiesByPartnerId() {
        // given
        RaddRegistryEntityV2 entity1 = buildEntity();
        RaddRegistryEntityV2 entity2 = buildEntity();
        entity2.setPartnerId(entity1.getPartnerId()); // stesso partnerId

        StepVerifier.create(
                raddRegistryDAO.putItemIfAbsent(entity1)
                        .then(raddRegistryDAO.putItemIfAbsent(entity2))
                        .then()
        ).verifyComplete();

        // when + then
        StepVerifier.create(raddRegistryDAO.findByPartnerId(entity1.getPartnerId()).collectList())
                .assertNext(list -> {
                    assertThat(list).hasSizeGreaterThanOrEqualTo(2);
                    assertThat(list).extracting(RaddRegistryEntityV2::getPartnerId)
                            .allMatch(pid -> pid.equals(entity1.getPartnerId()));
                })
                .verifyComplete();
    }

    @Test
    void shouldFindEntitiesPaginatedByPartnerId() {
        // given
        RaddRegistryEntityV2 entity1 = buildEntity();
        RaddRegistryEntityV2 entity2 = buildEntity();
        entity2.setPartnerId(entity1.getPartnerId());

        StepVerifier.create(
                raddRegistryDAO.putItemIfAbsent(entity1)
                        .then(raddRegistryDAO.putItemIfAbsent(entity2))
                        .then()
        ).verifyComplete();

        var readPaginatedItems = raddRegistryDAO.findPaginatedByPartnerId(entity1.getPartnerId(), 1, null)
                .flatMap(page -> raddRegistryDAO.findPaginatedByPartnerId(entity1.getPartnerId(), 1, page.getLastKey()))
                .flatMap(page -> raddRegistryDAO.findPaginatedByPartnerId(entity1.getPartnerId(), 1, page.getLastKey()));

        // when + then
        StepVerifier.create(readPaginatedItems)
                .assertNext(page -> Assertions.assertNull(page.getLastKey()))
                .verifyComplete();
    }

    @Test
    void shouldFindAllEntitiesPaginatedByPartnerId() {
        // given
        RaddRegistryEntityV2 entity1 = buildEntity();
        RaddRegistryEntityV2 entity2 = buildEntity();
        entity2.setPartnerId(entity1.getPartnerId());

        StepVerifier.create(
                raddRegistryDAO.putItemIfAbsent(entity1)
                        .then(raddRegistryDAO.putItemIfAbsent(entity2))
                        .then()
        ).verifyComplete();

        var readPaginatedItems = raddRegistryDAO.findPaginatedByPartnerId(entity1.getPartnerId(), null, null);

        // when + then
        StepVerifier.create(readPaginatedItems)
                .assertNext(page -> {
                    log.info("Page: {}", page);
                    Assertions.assertNotNull(page.getItems());
                    Assertions.assertEquals(2, page.getItems().size());
                    Assertions.assertNull(page.getLastKey());
                })
                .verifyComplete();
    }

    @Test
    void scanRegistriesLastKeyNull() {
        RaddRegistryEntityV2 entity1 =  buildEntity();
        RaddRegistryEntityV2 entity2 = buildEntity();

        raddRegistryDAO.putItemIfAbsent(entity1).then(raddRegistryDAO.putItemIfAbsent(entity2)).block();

        StepVerifier.create(raddRegistryDAO.scanRegistries(1, null))
                .expectNextMatches(raddRegistryEntityPage -> raddRegistryEntityPage.items().size() == 1 &&
                        raddRegistryEntityPage.lastEvaluatedKey() != null)
                .verifyComplete();
    }

    @Test
    void scanRegistriesInvalidLastKeyNotNull() {
        RaddRegistryEntityV2 entity1 =  buildEntity();
        RaddRegistryEntityV2 entity2 = buildEntity();

        raddRegistryDAO.putItemIfAbsent(entity1).then(raddRegistryDAO.putItemIfAbsent(entity2)).block();

        Assertions.assertThrows(RaddGenericException.class, () -> raddRegistryDAO.scanRegistries(1, "test"));
    }

}
