package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.pojo.RaddRegistryImportStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RaddRegistryImportDaoImplTest extends BaseTest.WithLocalStack {
    @Autowired
    @SpyBean
    private RaddRegistryImportDAO raddRegistryImportDAO;
    private RaddRegistryImportEntity baseEntity;
    @Mock
    DynamoDbAsyncClient dynamoDbAsyncClient;
    @Mock
    DynamoDbAsyncTable<RaddRegistryImportEntity> raddRegistryImportEntityDynamoDbAsyncTable;
    @BeforeEach
    public void setUp() {
        baseEntity = new RaddRegistryImportEntity();
        baseEntity.setCxId("cxId");
        baseEntity.setFileKey("fileKey");
        baseEntity.setChecksum("checksum");
        baseEntity.setRequestId("requestId");
        baseEntity.setError("error");
        baseEntity.setConfig("config");
        baseEntity.setTtl(1L);
        baseEntity.setStatus(RaddRegistryImportStatus.TO_PROCESS.name());
        Instant now = Instant.now();
        baseEntity.setCreatedAt(now);
        baseEntity.setUpdatedAt(now);
        Instant dueDate = now.plus(Duration.ofDays(7));
        baseEntity.setFileUploadDueDate(dueDate);
    }
    @Test
    void testPutRaddRegistryImportEntity(){
        RaddRegistryImportEntity response = raddRegistryImportDAO.putRaddRegistryImportEntity(baseEntity).block();
        assertNotNull(response);
        Assertions.assertEquals(baseEntity.getCxId(), response.getCxId());
        Assertions.assertEquals(baseEntity.getFileKey(), response.getFileKey());
        Assertions.assertEquals(baseEntity.getChecksum(), response.getChecksum());
        Assertions.assertEquals(baseEntity.getRequestId(), response.getRequestId());
        Assertions.assertEquals(baseEntity.getError(), response.getError());
        Assertions.assertEquals(baseEntity.getConfig(), response.getConfig());
        Assertions.assertEquals(baseEntity.getTtl(), response.getTtl());
        Assertions.assertEquals(baseEntity.getStatus(), response.getStatus());
        Assertions.assertEquals(baseEntity.getCreatedAt(), response.getCreatedAt());
        Assertions.assertEquals(baseEntity.getUpdatedAt(), response.getUpdatedAt());
        Assertions.assertEquals(baseEntity.getFileUploadDueDate(), response.getFileUploadDueDate());
    }
    @Test
     void testGetRegistryImportByCxId() {
        StepVerifier.create(raddRegistryImportDAO.getRegistryImportByCxId(baseEntity.getCxId()))
                .expectNextMatches(entity -> entity.getCxId().equals(baseEntity.getCxId()));
    }
    @Test
    void testGetRegistryImportByCxIdAndRequestId() {
        StepVerifier.create(raddRegistryImportDAO.getRegistryImportByCxIdAndRequestId(baseEntity.getCxId(),baseEntity.getRequestId()))
                .expectNextMatches(entity -> entity.getCxId().equals(baseEntity.getCxId())&& entity.getRequestId().equals(baseEntity.getRequestId()));
    }
    @Test
    void testGetRegistryImportByCxIdAndRequestIdFilterByStatus() {
        StepVerifier.create(raddRegistryImportDAO.getRegistryImportByCxIdFilterByStatus(baseEntity.getCxId(), baseEntity.getRequestId(), RaddRegistryImportStatus.TO_PROCESS))
                .expectNextMatches(entity -> entity.getCxId().equals(baseEntity.getCxId()) && entity.getRequestId().equals(baseEntity.getRequestId()) && entity.getStatus().equals(RaddRegistryImportStatus.TO_PROCESS.name()));
    }

}
