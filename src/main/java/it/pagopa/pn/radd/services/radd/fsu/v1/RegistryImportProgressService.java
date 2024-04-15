package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.RaddRegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.queue.producer.RegistryImportProgressProducer;
import it.pagopa.pn.radd.pojo.RaddRegistryImportStatus;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.time.StopWatch;
import org.jboss.logging.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;


@Service
@CustomLog
@RequiredArgsConstructor
public class RegistryImportProgressService {

    private final RaddRegistryImportDAO registryImportDAO;
    private final RaddRegistryRequestDAO registryRequestDAO;
    private final PnRaddFsuConfig raddFsuConfig;
    private final RegistryImportProgressProducer registryImportProgressProducer;

    @Scheduled(fixedDelayString = "${pn.radd.registryImportProgress.delay}")
    @SchedulerLock(name = "verifyImport", lockAtMostFor = "${pn.radd.registryImportProgress.lock-at-most}", lockAtLeastFor = "${pn.radd.registryImportProgress.lock-at-least}")
    protected void registryImportProgressLock() {
        try {
            LockAssert.assertLocked();
            log.info("batch registryImportProgress start on: {}", LocalDateTime.now());
            registryImportProgress();
        } catch (Exception ex) {
            log.error("Exception in actionPool", ex);
        }
    }

    public void registryImportProgress() {
        StopWatch watch = StopWatch.createStarted();
        retriveAndCheckImportRequest().block();
        watch.stop();
        log.debug("RegistryImport verify end. Time spent is {} millis", watch.getTime());
        if ((watch.getTime() / 1000) > raddFsuConfig.getRegistryImportProgress().getLockAtMost()) {
            log.warn("Time spent is greater than lockAtMostFor. Multiple nodes could schedule the same actions.");
        }
    }

    private Mono<Void> retriveAndCheckImportRequest() {
        return this.registryImportDAO.findWithStatusPending()
                .flatMap(this::checkRegistryRequest)
                .then();
    }

    private Mono<Void> checkRegistryRequest(RaddRegistryImportEntity item) {
        MDC.put(MDCUtils.MDC_PN_CTX_REQUEST_ID, item.getRequestId());
        MDC.put(MDCUtils.MDC_CX_ID_KEY, item.getCxId());

        Mono<Void> voidMono = registryRequestDAO.findByCxIdAndRequestIdAndStatusNotIn(item.getCxId(), item.getRequestId(), List.of(RegistryRequestStatus.ACCEPTED, RegistryRequestStatus.REJECTED))
                .hasElements()
                .flatMap(hasElement -> {
                    if (Boolean.FALSE.equals(hasElement)) {
                        return registryImportDAO.updateStatus(item, RaddRegistryImportStatus.DONE, null)
                                .flatMap(entity -> {
                                    log.info("Registry import status updated to DONE for cxId: {} and requestId: {}", item.getCxId(), item.getRequestId());
                                    return sendSqsImportCompleted(item.getCxId(), item.getRequestId());
                                });
                    } else {
                        log.info("No registry request found for cxId: {} and requestId: {}", item.getCxId(), item.getRequestId());
                        return Mono.empty();
                    }
                });

        return MDCUtils.addMDCToContextAndExecute(voidMono);
    }


    private Mono<Void> sendSqsImportCompleted(String cxId, String requestId) {
        return Mono.fromRunnable(() -> {
            log.info("Sending registry import completed event for cxId: {} and requestId: {}", cxId, requestId);
            registryImportProgressProducer.sendRegistryImportCompletedEvent(cxId, requestId);
        });
    }
}
