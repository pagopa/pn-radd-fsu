package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.UpdateRegistryRequest;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.queue.producer.RaddAltCapCheckerProducer;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@CustomLog
public class RegistrySelfService {

    private final RaddRegistryDAO raddRegistryDAO;
    private final RaddAltCapCheckerProducer raddAltCapCheckerProducer;
    private final PnRaddFsuConfig pnRaddFsuConfig;

    public Mono<RaddRegistryEntity> updateRegistry(String registryId, String xPagopaPnCxId, UpdateRegistryRequest request) {
        log.info("start updateRegistry for registryId [{}] and cxId [{}]", registryId, xPagopaPnCxId);
        return raddRegistryDAO.find(registryId, xPagopaPnCxId)
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.RADD_REGISTRY_NOT_FOUND, HttpStatus.NOT_FOUND)))
                .flatMap(registryEntity -> raddRegistryDAO.updateRegistryEntity(mapFieldToUpdate(registryEntity, request)))
                .doOnError(throwable -> log.error("Error during update registry request for registryId: [{}] and cxId: [{}]", registryId, xPagopaPnCxId, throwable));
    }

    private RaddRegistryEntity mapFieldToUpdate(RaddRegistryEntity registryEntity, UpdateRegistryRequest request) {
        if(StringUtils.isNotBlank(request.getDescription())) {
            registryEntity.setDescription(request.getDescription());
        }
        if(StringUtils.isNotBlank(request.getOpeningTime())) {
            registryEntity.setOpeningTime(request.getOpeningTime());
        }
        if(StringUtils.isNotBlank(request.getPhoneNumber())) {
            registryEntity.setPhoneNumber(request.getPhoneNumber());
        }
        return registryEntity;
    }

    public Mono<RaddRegistryEntity> deleteRegistry(String xPagopaPnCxId, String registryId, String endDate) {
        log.info("deleteRegistry called with xPagopaPnCxId: {}, registryId: {}, endDate: {}", xPagopaPnCxId, registryId, endDate);
        return raddRegistryDAO.find(registryId, xPagopaPnCxId)
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.REGISTRY_NOT_FOUND,HttpStatus.NOT_FOUND)))
                .flatMap(registryEntity -> updateRegistryEntityIfValidDate(registryEntity, endDate, registryId, xPagopaPnCxId))
                .doOnNext(raddRegistryEntity -> log.info("Registry with id: {} and cap: {} updated successfully", registryId, raddRegistryEntity.getZipCode()))
                .flatMap(raddRegistryEntity -> raddAltCapCheckerProducer.sendCapCheckerEvent(raddRegistryEntity.getZipCode())
                        .thenReturn(raddRegistryEntity))
                .doOnError(throwable -> log.error("Error during delete registry request for registryId: [{}] and cxId: [{}]", registryId, xPagopaPnCxId, throwable));
    }

    private Mono<RaddRegistryEntity> updateRegistryEntityIfValidDate(RaddRegistryEntity registryEntity, String date, String registryId, String xPagopaPnCxId) {
        LocalDate localDate = LocalDate.parse(date);
        Instant instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        if (isValidDate(instant)) {
            log.info("Updating registry with id: {} and cxId: {}", registryId, xPagopaPnCxId);
            registryEntity.setEndValidity(instant);
            return raddRegistryDAO.updateRegistryEntity(registryEntity);
        } else {
            log.error("not enough notice time for cancellation date: {}", instant);
            return Mono.error(new RaddGenericException(ExceptionTypeEnum.DATE_NOTICE_ERROR,HttpStatus.BAD_REQUEST));
        }
    }

    private boolean isValidDate(Instant endDate) {
        try {
            if(pnRaddFsuConfig.getRegistryDefaultEndValidity() != 0) {
                Instant minimumCancellationTime = Instant.now().plus(pnRaddFsuConfig.getRegistryDefaultEndValidity(), ChronoUnit.DAYS);
                return endDate.isAfter(minimumCancellationTime);
            }
            return true;
        } catch (DateTimeParseException e) {
            throw new RaddGenericException(ExceptionTypeEnum.DATE_INVALID_ERROR,HttpStatus.BAD_REQUEST);
        }
    }
}
