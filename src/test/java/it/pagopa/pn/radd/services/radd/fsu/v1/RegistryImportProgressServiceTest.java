package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.RaddRegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.queue.producer.RegistryImportProgressProducer;
import it.pagopa.pn.radd.pojo.RaddRegistryImportStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {RegistryImportProgressService.class})
class RegistryImportProgressServiceTest {
    @Mock
    private static PnRaddFsuConfig pnRaddFsuConfig;
    @Mock
    private RaddRegistryRequestDAO raddRegistryRequestDAO;

    @Mock
    private RaddRegistryImportDAO raddRegistryImportDAO;

    @Mock
    RegistryImportProgressProducer registryImportProgressProducer;

    private void updateCfg() {
        PnRaddFsuConfig.RegistryImportProgress cfg = new PnRaddFsuConfig.RegistryImportProgress();
        cfg.setLockAtMost(1000);
        when(pnRaddFsuConfig.getRegistryImportProgress()).thenReturn(cfg);
    }

    @Test
    void testCsvImportBatchDoNotUpdateEntity() {
        String cxId = "cxId";
        String requestId = "requestId";

        updateCfg();

        RaddRegistryImportEntity pnRaddRegistryImportEntity = new RaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setCxId(cxId);
        pnRaddRegistryImportEntity.setRequestId(requestId);
        when(raddRegistryImportDAO.findWithStatusPending()).thenReturn(Flux.just(pnRaddRegistryImportEntity));

        RaddRegistryRequestEntity raddRegistryRequest = new RaddRegistryRequestEntity();
        when(raddRegistryRequestDAO.findByCxIdAndRequestIdAndStatusNotIn(eq(cxId), eq(requestId), anyList())).thenReturn(Flux.just(raddRegistryRequest));

        RegistryImportProgressService registryImportProgressService = new RegistryImportProgressService(raddRegistryImportDAO, raddRegistryRequestDAO, pnRaddFsuConfig, registryImportProgressProducer);
        registryImportProgressService.registryImportProgress();

        verify(raddRegistryImportDAO, never()).updateStatus(any(), any(), anyString());
        verify(registryImportProgressProducer, never()).sendRegistryImportCompletedEvent(anyString(), anyString());
    }

    @Test
    void testCsvImportBatchUpdateEntity() {
        String cxId = "cxId";
        String requestId = "requestId";

        updateCfg();

        RaddRegistryImportEntity pnRaddRegistryImportEntity = new RaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setCxId(cxId);
        pnRaddRegistryImportEntity.setRequestId(requestId);
        when(raddRegistryImportDAO.findWithStatusPending()).thenReturn(Flux.just(pnRaddRegistryImportEntity));
        when(raddRegistryRequestDAO.findByCxIdAndRequestIdAndStatusNotIn(eq(cxId), eq(requestId), anyList())).thenReturn(Flux.empty());
        when(raddRegistryImportDAO.updateStatus(pnRaddRegistryImportEntity, RaddRegistryImportStatus.DONE, null)).thenReturn(Mono.just(pnRaddRegistryImportEntity));
        doNothing().when(registryImportProgressProducer).sendRegistryImportCompletedEvent(anyString(), anyString());

        RegistryImportProgressService registryImportProgressService = new RegistryImportProgressService(raddRegistryImportDAO, raddRegistryRequestDAO, pnRaddFsuConfig, registryImportProgressProducer);
        registryImportProgressService.registryImportProgress();

        verify(raddRegistryImportDAO, times(1)).updateStatus(pnRaddRegistryImportEntity, RaddRegistryImportStatus.DONE, null);
        verify(registryImportProgressProducer, times(1)).sendRegistryImportCompletedEvent(anyString(), anyString());
    }

}
