package it.pagopa.pn.radd.services.radd.fsu.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.pagopa.pn.commons.exceptions.PnInternalException;
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
import it.pagopa.pn.radd.pojo.PnLastEvaluatedKey;
import it.pagopa.pn.radd.pojo.RaddRegistryOriginalRequest;
import it.pagopa.pn.radd.middleware.queue.producer.RaddAltCapCheckerProducer;
import it.pagopa.pn.radd.utils.RaddRegistryUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static it.pagopa.pn.radd.pojo.PnLastEvaluatedKey.ERROR_CODE_PN_RADD_ALT_UNSUPPORTED_LAST_EVALUATED_KEY;
import static it.pagopa.pn.radd.utils.Const.REQUEST_ID_PREFIX;

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

    public Mono<CreateRegistryResponse> addRegistry(String xPagopaPnCxId, CreateRegistryRequest request) {
        RaddRegistryRequestEntity raddRegistryRequestEntity = createRaddRegistryRequestEntity(request, xPagopaPnCxId);
        return registryRequestDAO.createEntity(raddRegistryRequestEntity)
                .flatMap(this::sendStartEvent)
                .map(this::createRegistryResponse);
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

    private RaddRegistryRequestEntity createRaddRegistryRequestEntity(CreateRegistryRequest createRegistryRequest, String cxId) {
        String requestId = REQUEST_ID_PREFIX + UUID.randomUUID();
        RaddRegistryOriginalRequest originalRequest = raddRegistryRequestEntityMapper.retrieveOriginalRequest(createRegistryRequest);
        return raddRegistryRequestEntityMapper.retrieveRaddRegistryRequestEntity(cxId, requestId, originalRequest);
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

    public Mono<RegistriesResponse> registryListing(String xPagopaPnCxId, Integer limit, String lastKey, String cap, String city, String pr, String externalCode) {
        log.info("start registryListing for xPagopaPnCxId={} and limit: [{}] and lastKey: [{}] and cap: [{}] and city: [{}] and pr: [{}] and externalCode: [{}].", xPagopaPnCxId, limit, lastKey, cap, city, pr, externalCode);
        return raddRegistryDAO.findAll(xPagopaPnCxId, limit, cap, city, pr, externalCode, lastKey)
                .map(raddRegistryUtils::mapRegistryEntityToRegistry);
    }

}
