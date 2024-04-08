package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.queue.producer.RaddAltCapCheckerProducer;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@CustomLog
public class AnagraficaService {
    private final RaddRegistryDAO raddRegistryDAO;
    private final PnRaddFsuConfig pnRaddFsuConfig;
    private final RaddAltCapCheckerProducer raddAltCapCheckerProducer;


    public Mono<RaddRegistryEntity> deleteRegistry(String xPagopaPnCxId, String registryId, String endDate) {
        log.info("deleteRegistry called with xPagopaPnCxId: {}, registryId: {}, endDate: {}", xPagopaPnCxId, registryId, endDate);
        return raddRegistryDAO.find(registryId, xPagopaPnCxId)
                .flatMap(registryEntity -> updateRegistryEntityIfValidDate(registryEntity, endDate, registryId, xPagopaPnCxId)
                        .doOnSuccess(updatedRegistryEntity -> {
                            String cap = updatedRegistryEntity.getZipCode();
                            log.info("Registry with id: {} and cap: {} updated successfully", registryId, cap);
                            raddAltCapCheckerProducer.sendCapCheckerEvent(cap);
                        }))
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.REGISTRY_NOT_FOUND,HttpStatus.NOT_FOUND)));

    }

    private Mono<RaddRegistryEntity> updateRegistryEntityIfValidDate(RaddRegistryEntity registryEntity, String date, String registryId, String xPagopaPnCxId) {
        Instant endDate = Instant.parse(date);
        if (isValidDate(date)) {
            log.info("Updating registry with id: {} and cxId: {}", registryId, xPagopaPnCxId);
            registryEntity.setEndValidity(endDate);
            return raddRegistryDAO.updateRegistryEntity(registryEntity);
        } else {
            log.error("not enough notice time for cancellation date: {}", endDate);
            return Mono.error(new RaddGenericException(ExceptionTypeEnum.DATE_NOTICE_ERROR,HttpStatus.BAD_REQUEST));
        }
    }

    private boolean isValidDate(String date) {
        try {
            Instant inputDate = Instant.parse(date);
            Instant minimumCancellationTime = Instant.now().plus(pnRaddFsuConfig.getRegistryDefaultEndValidity(), ChronoUnit.DAYS);
            return inputDate.isAfter(minimumCancellationTime);
        } catch (DateTimeParseException e) {
            throw new RaddGenericException(ExceptionTypeEnum.DATE_INVALID_ERROR,HttpStatus.BAD_REQUEST);
        }
    }
}
