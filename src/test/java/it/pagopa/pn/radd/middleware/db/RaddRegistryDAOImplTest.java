package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.config.BaseTest;
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
    private RaddRegistryEntity baseEntity;
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
        addressEntity.setPr("pr");
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
        RaddRegistryEntity response= raddRegistryDAO.putItemIfAbsent(baseEntity).block();
        RaddRegistryEntity foundResponse= raddRegistryDAO.find(baseEntity.getRegistryId(), baseEntity.getCxId()).block();
        Assertions.assertEquals(response,foundResponse);
        assertNotNull(response);
        Assertions.assertEquals(baseEntity.getCxId(), response.getCxId());
        Assertions.assertEquals(baseEntity.getRegistryId(), response.getRegistryId());
        Assertions.assertEquals(baseEntity.getRequestId(), response.getRequestId());
        Assertions.assertEquals(baseEntity.getNormalizedAddress(), response.getNormalizedAddress());
        Assertions.assertEquals(baseEntity.getDescription(), response.getDescription());
        Assertions.assertEquals(baseEntity.getPhoneNumber(), response.getPhoneNumber());
        Assertions.assertEquals(baseEntity.getGeoLocation(), response.getGeoLocation());
        Assertions.assertEquals(baseEntity.getZipCode(), response.getZipCode());
        Assertions.assertEquals(baseEntity.getOpeningTime(), response.getOpeningTime());
        Assertions.assertEquals(baseEntity.getStartValidity(), response.getStartValidity());
        Assertions.assertEquals(baseEntity.getEndValidity(), response.getEndValidity());
    }
    @Test
    void testUpdateRegistryEntity(){
        RaddRegistryEntity objct = new RaddRegistryEntity();
        objct.setCxId("CxId");
        objct.setRegistryId("RegistryId");
        objct.setRequestId("RequestId");
        NormalizedAddressEntity addressEntity = new NormalizedAddressEntity();
        addressEntity.setCountry("country");
        addressEntity.setPr("pr");
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
        RaddRegistryEntity response= raddRegistryDAO.putItemIfAbsent(objct).block();
        StepVerifier.create(raddRegistryDAO.updateRegistryEntity(objct))
                .expectNextMatches(entity -> entity.getCxId().equals(objct.getCxId()) && entity.getRegistryId().equals(objct.getRegistryId()))
                .verifyComplete();
    }
//Todo: Fix the exception cases
    /*@Test
    void testUpdateRegistryEntityError() {
        StepVerifier.create(raddRegistryDAO.updateRegistryEntity(null))
                .expectErrorMatches(ex ->
                        ex instanceof RaddGenericException raddExc && raddExc.getExceptionType() == ExceptionTypeEnum.MISSING_REQUIRED_PARAMETER
                )
                .verify();
    }
    @Test
    void testPutItemIfAbsentError(){
        StepVerifier.create(raddRegistryDAO.putItemIfAbsent(null))
                .expectErrorMatches(ex ->
                        ex instanceof IllegalArgumentException)
                .verify();
    }*/

}
