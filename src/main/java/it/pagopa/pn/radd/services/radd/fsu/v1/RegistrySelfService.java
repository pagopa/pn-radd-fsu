package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.UpdateRegistryRequest;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryResponse;
import it.pagopa.pn.radd.mapper.RaddRegistryRequestEntityMapper;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.queue.producer.CorrelationIdEventsProducer;
import it.pagopa.pn.radd.pojo.RaddRegistryOriginalRequest;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static it.pagopa.pn.radd.utils.Const.REQUEST_ID_PREFIX;

@Service
@RequiredArgsConstructor
@CustomLog
public class RegistrySelfService {

    private final RaddRegistryDAO raddRegistryDAO;
    private final RaddRegistryRequestEntityMapper raddRegistryRequestEntityMapper;
    private final CorrelationIdEventsProducer correlationIdEventsProducer;


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

}
