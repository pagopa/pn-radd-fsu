package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.RaddRegistryImportStatus;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.radd.pojo.RegistryRequestStatus.ACCEPTED;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RaddRegistryRequestDAOImplTest extends BaseTest.WithLocalStack{
    @Autowired
    @SpyBean
    private RaddRegistryRequestDAO registryRequestDAO;
    private RaddRegistryRequestEntity baseEntity;
    @Autowired
    private BaseDao<RaddRegistryRequestEntity> baseDao;
    @Mock
    DynamoDbAsyncClient dynamoDbAsyncClient;
    @Mock
    DynamoDbAsyncTable<RaddRegistryRequestEntity> dynamoDbAsyncTable;

    @BeforeEach
    public void setUp() {
        baseEntity = new RaddRegistryRequestEntity();
        baseEntity.setPk("testPk");
        baseEntity.setRequestId("testRequestId");
        baseEntity.setCorrelationId("testCorrelationId");
        baseEntity.setCreatedAt(Instant.now());
        baseEntity.setUpdatedAt(Instant.now());
        baseEntity.setOriginalRequest("testOriginalRequest");
        baseEntity.setZipCode("testZipCode");
        baseEntity.setStatus(RegistryRequestStatus.NOT_WORKED.name());
        baseEntity.setError("testError");
        baseEntity.setCxId("testCxId");
        baseEntity.setRegistryId("testRegistryId");

    }

    @Test
    void testPutRaddRegistryImportEntity(){
        RaddRegistryRequestEntity response = baseDao.putItem(baseEntity).block();
        assertNotNull(response);
        Assertions.assertEquals(baseEntity.getPk(), response.getPk());
        Assertions.assertEquals(baseEntity.getCxId(), response.getCxId());
        Assertions.assertEquals(baseEntity.getRegistryId(), response.getRegistryId());
        Assertions.assertEquals(baseEntity.getRequestId(), response.getRequestId());
        Assertions.assertEquals(baseEntity.getCorrelationId(), response.getCorrelationId());
        Assertions.assertEquals(baseEntity.getCreatedAt(), response.getCreatedAt());
        Assertions.assertEquals(baseEntity.getUpdatedAt(), response.getUpdatedAt());
        Assertions.assertEquals(baseEntity.getOriginalRequest(), response.getOriginalRequest());
        Assertions.assertEquals(baseEntity.getZipCode(), response.getZipCode());
        Assertions.assertEquals(baseEntity.getStatus(), response.getStatus());
        Assertions.assertEquals(baseEntity.getError(), response.getError());
    }
    @Test
    void testFindByCorrelationIdWithStatus(){
        RaddRegistryRequestEntity response = baseDao.putItem(baseEntity).block();
        StepVerifier.create(registryRequestDAO.findByCorrelationIdWithStatus(baseEntity.getCorrelationId(), RegistryRequestStatus.valueOf(baseEntity.getStatus())))
                .expectNextMatches(foundedEntity -> foundedEntity.getRequestId().equals(baseEntity.getRequestId()))
                .verifyComplete();
    }

    @Test
    void testUpdateRegistryRequestStatus(){
        RaddRegistryRequestEntity object = new RaddRegistryRequestEntity();
        object.setPk("Pk");
        object.setCorrelationId("correlationId");
        object.setRequestId("RequestId");
        object.setStatus(RaddRegistryImportStatus.REJECTED.name());
        object.setUpdatedAt(Instant.now());
        StepVerifier.create(registryRequestDAO.updateRegistryRequestStatus(object, RegistryRequestStatus.valueOf(object.getStatus())))
                .expectNextMatches(updatedEntity -> updatedEntity.getRequestId().equals(object.getRequestId()))
                .verifyComplete();
    }
    @Test
    void testUpdateStatusAndError(){
        RaddRegistryRequestEntity object = new RaddRegistryRequestEntity();
        object.setPk("Pk");
        object.setCorrelationId("correlationId");
        object.setRequestId("RequestId");
        object.setStatus(RaddRegistryImportStatus.REJECTED.name());
        object.setUpdatedAt(Instant.now());
        object.setError("error");
        StepVerifier.create(registryRequestDAO.updateStatusAndError(object, RegistryRequestStatus.valueOf(object.getStatus()), object.getError()))
                .expectNextMatches(updatedEntity -> updatedEntity.getRequestId().equals(object.getRequestId()))
                .verifyComplete();
    }
    @Test
    void testUpdateRecordsInPending(){
        RaddRegistryRequestEntity object = new RaddRegistryRequestEntity();
        object.setPk("Pk");
        object.setCorrelationId("correlationId");
        object.setRequestId("RequestId");
        object.setStatus(RaddRegistryImportStatus.PENDING.name());
        List<RaddRegistryRequestEntity> addresses = new ArrayList<>();
        addresses.add(object);
        Mono<Void> result = registryRequestDAO.updateRecordsInPending(addresses);
        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }
    @Test
    void testGetAllFromCorrelationId(){
        baseDao.putItem(baseEntity).block();
        StepVerifier.create(registryRequestDAO.getAllFromCorrelationId(baseEntity.getCorrelationId(), baseEntity.getStatus()))
                .expectNextMatches(updatedEntity -> updatedEntity.getRequestId().equals(baseEntity.getRequestId()))
                .verifyComplete();
    }

    @Test
    void testGetAllFromCxidAndRequestIdWithState(){
        baseEntity.setStatus(ACCEPTED.name());
        baseDao.putItem(baseEntity).block();
        baseEntity.setPk("testPk2");
        baseDao.putItem(baseEntity).block();
        baseEntity.setPk("testPk3");
        baseDao.putItem(baseEntity).block();
        StepVerifier.create(registryRequestDAO.getAllFromCxidAndRequestIdWithState(baseEntity.getCxId(), baseEntity.getRequestId(), ACCEPTED.name()))
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void writeCsvAddressesError(){
        baseDao.putItem(baseEntity).block();
        RaddRegistryRequestEntity newEntity = new RaddRegistryRequestEntity();
        newEntity.setPk("cxId#requestId#testPk2");
        StepVerifier.create(registryRequestDAO.writeCsvAddresses(List.of(baseEntity, newEntity), "testKey"))
                .expectError()
                .verify();

    }


    @Test
    void writeCsvAddressesErrorOK(){
        RaddRegistryRequestEntity newEntity = new RaddRegistryRequestEntity();
        newEntity.setPk(UUID.randomUUID().toString());
        baseEntity.setPk(UUID.randomUUID().toString());
        StepVerifier.create(registryRequestDAO.writeCsvAddresses(List.of(newEntity), "testKey"))
                .verifyComplete();

    }

    @Test
    void testGetRegistryByCxIdAndRequestId(){
        baseDao.putItem(baseEntity).block();
        StepVerifier.create(registryRequestDAO.getRegistryByCxIdAndRequestId(baseEntity.getCxId(), baseEntity.getRequestId(), 1, null))
                .expectNextMatches(result -> result.getResultsPage().size() == 1)
                .verifyComplete();
    }
}
