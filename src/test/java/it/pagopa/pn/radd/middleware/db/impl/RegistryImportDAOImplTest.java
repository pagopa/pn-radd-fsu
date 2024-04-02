package it.pagopa.pn.radd.middleware.db.impl;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.entities.PnRaddRegistryImportEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.DirectProcessor;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {PnRaddFsuConfig.class})
@ExtendWith(SpringExtension.class)
class RegistryImportDAOImplTest {
    @MockBean
    private DynamoDbAsyncClient dynamoDbAsyncClient;

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Autowired
    private PnRaddFsuConfig pnRaddFsuConfig;

    @MockBean
    private RegistryImportDAOImpl registryImportDAOImpl;

    /**
     * Method under test: {@link RegistryImportDAOImpl#getRegistryImportByCxId(String)}
     */
    @Test
    void testGetRegistryImportByCxId() {
        DirectProcessor<PnRaddRegistryImportEntity> createResult = DirectProcessor.create();
        when(registryImportDAOImpl.getRegistryImportByCxId(Mockito.<String>any())).thenReturn(createResult);
        assertSame(createResult, registryImportDAOImpl.getRegistryImportByCxId("42"));
        verify(registryImportDAOImpl).getRegistryImportByCxId(Mockito.<String>any());
    }

    @Test
    void testPutRaddRegistryImportEntity() {
        when(registryImportDAOImpl.putRaddRegistryImportEntity(Mockito.<PnRaddRegistryImportEntity>any()))
                .thenReturn(null);

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        StepVerifier.create(registryImportDAOImpl.putRaddRegistryImportEntity(pnRaddRegistryImportEntity))
                .expectNext(pnRaddRegistryImportEntity);
    }
}

