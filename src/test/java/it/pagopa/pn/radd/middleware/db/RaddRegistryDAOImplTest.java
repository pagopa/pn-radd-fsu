package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.config.RestExceptionHandler;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.entities.NormalizedAddressEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RaddRegistryDAOImplTest extends BaseTest.WithLocalStack {
    @Autowired
    @SpyBean
    private RaddRegistryDAO raddRegistryDAO;

    @Autowired
    @SpyBean
    private RestExceptionHandler exceptionHandler;
    private RaddRegistryEntity baseEntity;

    @Autowired
    private BaseDao<RaddRegistryEntity> baseDao;
    @Mock
    DynamoDbAsyncClient dynamoDbAsyncClient;
    @Mock
    DynamoDbAsyncTable<RaddRegistryEntity> raddRegistryImportEntityDynamoDbAsyncTable;
    @BeforeEach
    public void setUp() {
        baseEntity = new RaddRegistryEntity();
        baseEntity.setRegistryId("testRegistryId");
        baseEntity.setCxId("testCxId");
        baseEntity.setRequestId("testRequestId");
        NormalizedAddressEntity addressEntity = new NormalizedAddressEntity();
        addressEntity.setCountry("country");
        addressEntity.setProvince("pr");
        addressEntity.setCity("city");
        addressEntity.setCap("cap");
        baseEntity.setNormalizedAddress(addressEntity);
        baseEntity.setDescription("testDescription");
        baseEntity.setPhoneNumber("testPhoneNumber");
        baseEntity.setGeoLocation("testGeoLocation");
        baseEntity.setZipCode("testZipCode");
        baseEntity.setOpeningTime("testOpeningTime");
        baseEntity.setStartValidity(Instant.now());
        baseEntity.setEndValidity(Instant.now().plusSeconds(3600));
    }
    @Test
    void testPutItemIfAbsent(){
        RaddRegistryEntity registryEntity = getRegistryEntity("registryId1", "cxId1");
        RaddRegistryEntity response= raddRegistryDAO.putItemIfAbsent(registryEntity).block();
        RaddRegistryEntity foundResponse= raddRegistryDAO.find(registryEntity.getRegistryId(), registryEntity.getCxId()).block();
        Assertions.assertEquals(response,foundResponse);
        assertNotNull(response);
        Assertions.assertEquals(registryEntity.getCxId(), response.getCxId());
        Assertions.assertEquals(registryEntity.getRegistryId(), response.getRegistryId());
        Assertions.assertEquals(registryEntity.getRequestId(), response.getRequestId());
        Assertions.assertEquals(registryEntity.getNormalizedAddress(), response.getNormalizedAddress());
        Assertions.assertEquals(registryEntity.getDescription(), response.getDescription());
        Assertions.assertEquals(registryEntity.getPhoneNumber(), response.getPhoneNumber());
        Assertions.assertEquals(registryEntity.getGeoLocation(), response.getGeoLocation());
        Assertions.assertEquals(registryEntity.getZipCode(), response.getZipCode());
        Assertions.assertEquals(registryEntity.getOpeningTime(), response.getOpeningTime());
        Assertions.assertEquals(registryEntity.getStartValidity(), response.getStartValidity());
        Assertions.assertEquals(registryEntity.getEndValidity(), response.getEndValidity());
    }
    @Test
    void testUpdateRegistryEntity(){
        RaddRegistryEntity objct = new RaddRegistryEntity();
        objct.setCxId("CxId");
        objct.setRegistryId("RegistryId");
        objct.setRequestId("RequestId");
        NormalizedAddressEntity addressEntity = new NormalizedAddressEntity();
        addressEntity.setCountry("country");
        addressEntity.setProvince("pr");
        addressEntity.setCity("city");
        addressEntity.setCap("cap");
        objct.setNormalizedAddress(addressEntity);
        objct.setDescription("testDescription");
        objct.setPhoneNumber("testPhoneNumber");
        objct.setGeoLocation("testGeoLocation");
        objct.setZipCode("testZipCode");
        objct.setOpeningTime("testOpeningTime");
        objct.setStartValidity(Instant.now());
        objct.setEndValidity(Instant.now().plusSeconds(3600));
        baseDao.putItem(objct).block();
        StepVerifier.create(raddRegistryDAO.updateRegistryEntity(objct))
                .expectNextMatches(entity -> entity.getCxId().equals(objct.getCxId()) && entity.getRegistryId().equals(objct.getRegistryId()))
                .verifyComplete();
    }

    @Test
    void scanRegistriesLastKeyNull() {
        RaddRegistryEntity entity =  getRegistryEntity("registryId3", "cxId3");

        baseDao.putItem(baseEntity).block();
        baseDao.putItem(entity).block();

        StepVerifier.create(raddRegistryDAO.scanRegistries(1, null))
                .expectNextMatches(raddRegistryEntityPage -> raddRegistryEntityPage.items().size() == 1 &&
                        raddRegistryEntityPage.lastEvaluatedKey() != null)
                .verifyComplete();
    }

    @Test
    void scanRegistriesInvalidLastKeyNotNull() {
        RaddRegistryEntity entity =  getRegistryEntity("registryId3", "cxId3");

        baseDao.putItem(baseEntity).block();
        baseDao.putItem(entity).block();

        Assertions.assertThrows(RaddGenericException.class, () -> raddRegistryDAO.scanRegistries(1, "test"));
    }

    @Test
    void scanRegistriesLastKeyNotNull() {
        RaddRegistryEntity entity =  getRegistryEntity("registryId3", "cxId3");

        baseDao.putItem(baseEntity).block();
        baseDao.putItem(entity).block();

        StepVerifier.create(raddRegistryDAO.scanRegistries(1,
                        "eyJlayI6IlRBY3hpZCIsImlrIjp7InJlZ2lzdHJ5SWQiOiI4ZGMxZWQwYS0wZTFjLTNmYTctOTlmOC1hOGEzNTczYWY0ODMiLCJjeElkIjoiVEFjeGlkIn19"))
                .expectNextMatches(raddRegistryEntityPage -> raddRegistryEntityPage.items().isEmpty())
                .verifyComplete();
    }

    private RaddRegistryEntity getRegistryEntity(String registryId, String cxId) {
        RaddRegistryEntity entity = new RaddRegistryEntity();
        entity.setRegistryId(registryId);
        entity.setCxId(cxId);
        entity.setRequestId("testRequestId2");
        NormalizedAddressEntity addressEntity = new NormalizedAddressEntity();
        addressEntity.setCountry("country2");
        addressEntity.setProvince("pr2");
        addressEntity.setCity("city2");
        addressEntity.setCap("cap2");
        entity.setNormalizedAddress(addressEntity);
        entity.setDescription("testDescription2");
        entity.setPhoneNumber("testPhoneNumber2");
        entity.setGeoLocation("testGeoLocation2");
        entity.setZipCode("testZipCode2");
        entity.setOpeningTime("testOpeningTime2");
        entity.setStartValidity(Instant.now());
        entity.setEndValidity(Instant.now().plusSeconds(3600));
        return entity;
    }
}
