package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistriesResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.UpdateRegistryRequest;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.mapper.RaddRegistryRequestEntityMapper;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.queue.producer.CorrelationIdEventsProducer;
import it.pagopa.pn.radd.middleware.queue.producer.RaddAltCapCheckerProducer;
import it.pagopa.pn.radd.pojo.RaddRegistryOriginalRequest;
import it.pagopa.pn.radd.utils.RaddRegistryUtils;
import it.pagopa.pn.radd.utils.Utils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static it.pagopa.pn.radd.utils.Const.*;
import static it.pagopa.pn.radd.utils.DateUtils.convertDateToInstantAtStartOfDay;
import static it.pagopa.pn.radd.utils.DateUtils.getStartOfDayToday;

@Service
@RequiredArgsConstructor
@CustomLog
public class RegistrySelfService {

    private final RaddRegistryDAO raddRegistryDAO;
    private final RaddRegistryRequestDAO registryRequestDAO;
    private final RaddRegistryRequestEntityMapper raddRegistryRequestEntityMapper;
    private final CorrelationIdEventsProducer correlationIdEventsProducer;
    private final RaddAltCapCheckerProducer raddAltCapCheckerProducer;
    private final RaddRegistryUtils raddRegistryUtils;
    private final PnRaddFsuConfig pnRaddFsuConfig;

    public Mono<RaddRegistryEntity> updateRegistry(String registryId, String xPagopaPnCxId, UpdateRegistryRequest request) {
        log.info("start updateRegistry for registryId [{}] and cxId [{}]", registryId, xPagopaPnCxId);
        checkUpdateRegistryRequest(request);
        return raddRegistryDAO.find(registryId, xPagopaPnCxId)
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.RADD_REGISTRY_NOT_FOUND, HttpStatus.NOT_FOUND)))
                .flatMap(registryEntity -> raddRegistryDAO.updateRegistryEntity(mapFieldToUpdate(registryEntity, request)))
                .doOnError(throwable -> log.error("Error during update registry request for registryId: [{}] and cxId: [{}]", registryId, xPagopaPnCxId, throwable));
    }

    private void checkUpdateRegistryRequest(UpdateRegistryRequest request) {
        Utils.matchRegex(REGEX_PHONENUMBER, request.getPhoneNumber(), ExceptionTypeEnum.PHONE_NUMBER_ERROR);
        Utils.matchRegex(REGEX_OPENINGTIME, request.getOpeningTime(), ExceptionTypeEnum.OPENING_TIME_ERROR);
    }

    private RaddRegistryEntity mapFieldToUpdate(RaddRegistryEntity registryEntity, UpdateRegistryRequest request) {
        if (StringUtils.isNotBlank(request.getDescription())) {
            registryEntity.setDescription(request.getDescription());
        }
        if (StringUtils.isNotBlank(request.getOpeningTime())) {
            registryEntity.setOpeningTime(request.getOpeningTime());
        }
        if (StringUtils.isNotBlank(request.getPhoneNumber())) {
            registryEntity.setPhoneNumber(request.getPhoneNumber());
        }
        return registryEntity;
    }

    public Mono<CreateRegistryResponse> addRegistry(String xPagopaPnCxId, CreateRegistryRequest request) {
        checkCreateRegistryRequest(request);
        RaddRegistryRequestEntity raddRegistryRequestEntity = createRaddRegistryRequestEntity(request, xPagopaPnCxId);
        log.info("Creating registry request entity for cxId: {} and requestId: {}", xPagopaPnCxId, raddRegistryRequestEntity.getRequestId());
        return registryRequestDAO.createEntity(raddRegistryRequestEntity)
                .flatMap(entity -> {
                    log.info("Registry request entity created successfully for cxId: {} and requestId: {}", xPagopaPnCxId, entity.getRequestId());
                    return sendStartEvent(entity);
                })
                .map(response -> {
                    log.info("Start event sent successfully for cxId: {} and requestId: {}", xPagopaPnCxId, response.getRequestId());
                    return createRegistryResponse(response);
                });
    }

    private void checkCreateRegistryRequest(CreateRegistryRequest request) {
        Utils.matchRegex(REGEX_PHONENUMBER, request.getPhoneNumber(), ExceptionTypeEnum.PHONE_NUMBER_ERROR);
        if (request.getGeoLocation() != null) {
            Utils.matchRegex(REGEX_GEOLOCATION, request.getGeoLocation().getLatitude(), ExceptionTypeEnum.GEOLOCATION_ERROR);
            Utils.matchRegex(REGEX_GEOLOCATION, request.getGeoLocation().getLongitude(), ExceptionTypeEnum.GEOLOCATION_ERROR);
        }
        Utils.matchRegex(REGEX_OPENINGTIME, request.getOpeningTime(), ExceptionTypeEnum.OPENING_TIME_ERROR);
        Utils.matchRegex(REGEX_CAPACITY, request.getCapacity(), ExceptionTypeEnum.CAPACITY_ERROR);

        verifyDates(request.getStartValidity(), request.getEndValidity());
    }

    private void verifyDates(String startValidity, String endValidity) {
        try {
            Instant startValidityInstant = startValidity != null ? convertDateToInstantAtStartOfDay(startValidity) : getStartOfDayToday();

            if (endValidity != null) {
                Instant endValidityInstant = convertDateToInstantAtStartOfDay(endValidity);
                if (endValidityInstant.isBefore(startValidityInstant)) {
                    throw new RaddGenericException(ExceptionTypeEnum.DATE_INTERVAL_ERROR, HttpStatus.BAD_REQUEST);
                }
            }
        } catch (DateTimeParseException e) {
            throw new RaddGenericException(ExceptionTypeEnum.DATE_INVALID_ERROR, HttpStatus.BAD_REQUEST);
        }
    }


    private Mono<RaddRegistryRequestEntity> sendStartEvent(RaddRegistryRequestEntity entity) {
        return Mono.fromRunnable(() -> correlationIdEventsProducer.sendCorrelationIdEvent(entity.getCorrelationId()))
                .thenReturn(entity);
    }

    private CreateRegistryResponse createRegistryResponse(RaddRegistryRequestEntity entity) {
        CreateRegistryResponse response = new CreateRegistryResponse();
        response.setRequestId(entity.getRequestId());
        return response;
    }

    private RaddRegistryRequestEntity createRaddRegistryRequestEntity(CreateRegistryRequest
                                                                              createRegistryRequest, String cxId) {
        String requestId = REQUEST_ID_PREFIX + UUID.randomUUID();
        RaddRegistryOriginalRequest originalRequest = raddRegistryRequestEntityMapper.retrieveOriginalRequest(createRegistryRequest);
        return raddRegistryRequestEntityMapper.retrieveRaddRegistryRequestEntity(cxId, requestId, originalRequest);
    }

    public Mono<RaddRegistryEntity> deleteRegistry(String xPagopaPnCxId, String registryId, String endDate) {
        log.info("deleteRegistry called with xPagopaPnCxId: {}, registryId: {}, endDate: {}", xPagopaPnCxId, registryId, endDate);
        return raddRegistryDAO.find(registryId, xPagopaPnCxId)
                .switchIfEmpty(Mono.error(new RaddGenericException(ExceptionTypeEnum.REGISTRY_NOT_FOUND, HttpStatus.NOT_FOUND)))
                .flatMap(registryEntity -> updateRegistryEntityIfValidDate(registryEntity, endDate, registryId, xPagopaPnCxId))
                .doOnNext(raddRegistryEntity -> log.info("Registry with id: {} and cap: {} updated successfully", registryId, raddRegistryEntity.getZipCode()))
                .flatMap(raddRegistryEntity -> raddAltCapCheckerProducer.sendCapCheckerEvent(raddRegistryEntity.getZipCode())
                        .thenReturn(raddRegistryEntity))
                .doOnError(throwable -> log.error("Error during delete registry request for registryId: [{}] and cxId: [{}]", registryId, xPagopaPnCxId, throwable));
    }

    private Mono<RaddRegistryEntity> updateRegistryEntityIfValidDate(RaddRegistryEntity registryEntity, String
            date, String registryId, String xPagopaPnCxId) {
            Instant instant = convertDateToInstantAtStartOfDay(date);
            if (isValidDate(instant)) {
                log.info("Updating registry with id: {} and cxId: {}", registryId, xPagopaPnCxId);
                registryEntity.setEndValidity(instant);
                return raddRegistryDAO.updateRegistryEntity(registryEntity);
            } else {
                log.error("not enough notice time for cancellation date: {}", instant);
                return Mono.error(new RaddGenericException(ExceptionTypeEnum.DATE_NOTICE_ERROR, HttpStatus.BAD_REQUEST));
            }
    }

    private boolean isValidDate(Instant endDate) {
        if (pnRaddFsuConfig.getRegistryDefaultEndValidity() != 0) {
            Instant minimumCancellationTime = Instant.now().plus(pnRaddFsuConfig.getRegistryDefaultEndValidity(), ChronoUnit.DAYS);
            return endDate.isAfter(minimumCancellationTime);
        }
        return true;
    }

    public Mono<RegistriesResponse> registryListing(String xPagopaPnCxId, Integer limit, String lastKey, String
            cap, String city, String pr, String externalCode) {
        log.info("start registryListing for xPagopaPnCxId={} and limit: [{}] and lastKey: [{}] and cap: [{}] and city: [{}] and pr: [{}] and externalCode: [{}].", xPagopaPnCxId, limit, lastKey, cap, city, pr, externalCode);
        return raddRegistryDAO.findByFilters(xPagopaPnCxId, limit, cap, city, pr, externalCode, lastKey)
                .map(raddRegistryUtils::mapRegistryEntityToRegistry);
    }

}
