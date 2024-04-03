package it.pagopa.pn.radd.services.radd.fsu.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.PnRaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.PnRaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.queue.consumer.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.pojo.ImportStatus;
import it.pagopa.pn.radd.pojo.OriginalRequest;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static it.pagopa.pn.radd.utils.Const.ERROR_DUPLICATE;

@Component
@CustomLog
@RequiredArgsConstructor
public class RegistryService {
    private final PnRaddRegistryRequestDAO pnRaddRegistryRequestDAO;
    private final PnRaddRegistryDAO pnRaddRegistryDAO;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<Void> handleMessage(PnAddressManagerEvent message) {
        return processMessage(message.getPayload().getResultItems(), message.getPayload().getCorrelationId());
    }
    private Mono<Void> processMessage(List<PnAddressManagerEvent.ResultItem> resultItems, String correlationId) {
        return pnRaddRegistryRequestDAO.findByCorrelationIdWithStatus(correlationId, ImportStatus.PENDING)
                .switchIfEmpty(Flux.error(new RaddGenericException("No pending items found for correlationId " + correlationId)))
                .flatMap(raddRegistryRequest -> processAddressForRegistryRequest(resultItems, raddRegistryRequest))
                .doOnError(
                        RaddGenericException.class,
                        exception -> log.warn("correlationId {} not found or not in PENDING", correlationId)
                )
                .then();
    }

    private Mono<RaddRegistryRequestEntity> processAddressForRegistryRequest(List<PnAddressManagerEvent.ResultItem> resultItems, RaddRegistryRequestEntity raddRegistryRequest) {
        return getRelativeItemFromAddressManagerEvent(resultItems, raddRegistryRequest.getPk())
                .flatMap(item -> {
                    String error = item.getError();
                    if (StringUtils.isNotBlank(error)) {
                        log.warn("Id {} error not empty", item.getError());
                        return pnRaddRegistryRequestDAO.updateStatusAndError(raddRegistryRequest, ImportStatus.REJECTED, error);
                    }
                    return handleRegistryUpdate(raddRegistryRequest, item);
                });
    }

    private Mono<RaddRegistryRequestEntity> handleRegistryUpdate(RaddRegistryRequestEntity raddRegistryRequestEntity, PnAddressManagerEvent.ResultItem resultItem) {
        UUID registryId = UUID.nameUUIDFromBytes(resultItem.getNormalizedAddress().toString().getBytes());

        return pnRaddRegistryDAO.find(registryId.toString(), raddRegistryRequestEntity.getCxId())
                .flatMap(entity -> updateRegistryRequestEntity(raddRegistryRequestEntity, entity))
                .switchIfEmpty(createNewRegistryEntity(raddRegistryRequestEntity, resultItem))
                .onErrorResume(throwable -> {
                    if (throwable instanceof RaddGenericException ex && ERROR_DUPLICATE.equals(ex.getMessage())) {
                        return pnRaddRegistryRequestDAO.updateStatusAndError(
                                raddRegistryRequestEntity,
                                ImportStatus.REJECTED,
                                ERROR_DUPLICATE
                        );
                    }
                    return Mono.error(throwable);
                });
    }

    private Mono<RaddRegistryRequestEntity> updateRegistryRequestEntity(RaddRegistryRequestEntity newRegistryRequestEntity, RaddRegistryEntity preExistingRegistryEntity) {
        if (StringUtils.equals(preExistingRegistryEntity.getRequestId(), newRegistryRequestEntity.getRequestId())) {
            return pnRaddRegistryRequestDAO.updateStatusAndError(newRegistryRequestEntity, ImportStatus.REJECTED, ERROR_DUPLICATE);
        } else {
            return mergeNewRegistryEntity(preExistingRegistryEntity, newRegistryRequestEntity)
                    .flatMap(updatedEntity -> pnRaddRegistryDAO.updateRegistryEntity(updatedEntity)
                            .flatMap(unused -> pnRaddRegistryRequestDAO.updateRegistryRequestStatus(newRegistryRequestEntity, RegistryRequestStatus.ACCEPTED)));
        }
    }

    private Mono<RaddRegistryRequestEntity> createNewRegistryEntity(RaddRegistryRequestEntity raddRegistryRequestEntity, PnAddressManagerEvent.ResultItem resultItem) {
        return constructRaddRegistryEntity(resultItem.getNormalizedAddress(), raddRegistryRequestEntity)
                .flatMap(item -> this.pnRaddRegistryDAO.putItemIfAbsent(item)
                        .onErrorResume(ConditionalCheckFailedException.class, ex -> Mono.error(new RaddGenericException(ERROR_DUPLICATE))))
                .flatMap(unused -> pnRaddRegistryRequestDAO.updateRegistryRequestStatus(raddRegistryRequestEntity, RegistryRequestStatus.ACCEPTED));

    }

    public Mono<RaddRegistryEntity> mergeNewRegistryEntity(RaddRegistryEntity preExistingRegistryEntity, RaddRegistryRequestEntity newRegistryRequestEntity) {
        return Mono.fromCallable(() -> {
            OriginalRequest originalRequest = objectMapper.readValue(newRegistryRequestEntity.getOriginalRequest(), OriginalRequest.class);

            return getRaddRegistryEntity(preExistingRegistryEntity, newRegistryRequestEntity, originalRequest);
        });
    }

    private static RaddRegistryEntity getRaddRegistryEntity(RaddRegistryEntity preExistingRegistryEntity, RaddRegistryRequestEntity newRegistryRequestEntity, OriginalRequest originalRequest) {
        RaddRegistryEntity registryEntity = new RaddRegistryEntity();

        registryEntity.setRegistryId(preExistingRegistryEntity.getRegistryId());
        registryEntity.setCxId(preExistingRegistryEntity.getCxId());
        registryEntity.setNormalizedAddress(preExistingRegistryEntity.getNormalizedAddress());
        registryEntity.setRequestId(newRegistryRequestEntity.getRequestId());
        // Metadata from originalRequest
        registryEntity.setDescription(originalRequest.getDescription());
        registryEntity.setPhoneNumber(originalRequest.getPhoneNumber());
        registryEntity.setGeoLocation(originalRequest.getGeoLocation());
        registryEntity.setZipCode(newRegistryRequestEntity.getZipCode());
        registryEntity.setOpeningTime(originalRequest.getOpeningTime());
        registryEntity.setStartValidity(originalRequest.getStartValidity());
        registryEntity.setEndValidity(originalRequest.getEndValidity());

        return registryEntity;
    }

    private Mono<RaddRegistryEntity> constructRaddRegistryEntity(PnAddressManagerEvent.NormalizedAddress normalizedAddress, RaddRegistryRequestEntity registryRequest) {
        return Mono.fromCallable(() -> {
            String normalizedAddressString = objectMapper.writeValueAsString(normalizedAddress);
            OriginalRequest originalRequest = objectMapper.readValue(registryRequest.getOriginalRequest(), OriginalRequest.class);

            return getRaddRegistryEntity(normalizedAddress, registryRequest, normalizedAddressString, originalRequest);
        });
    }

    private static RaddRegistryEntity getRaddRegistryEntity(PnAddressManagerEvent.NormalizedAddress normalizedAddress, RaddRegistryRequestEntity registryRequest, String normalizedAddressString, OriginalRequest originalRequest) {
        RaddRegistryEntity registryEntity = new RaddRegistryEntity();

        registryEntity.setRegistryId(registryRequest.getRegistryId());
        registryEntity.setCxId(registryRequest.getCxId());
        registryEntity.setNormalizedAddress(normalizedAddressString);
        registryEntity.setRequestId(registryRequest.getRequestId());
        // Metadata from originalRequest
        registryEntity.setDescription(originalRequest.getDescription());
        registryEntity.setPhoneNumber(originalRequest.getPhoneNumber());
        registryEntity.setGeoLocation(originalRequest.getGeoLocation());
        registryEntity.setZipCode(normalizedAddress.getCap());
        registryEntity.setOpeningTime(originalRequest.getOpeningTime());
        registryEntity.setStartValidity(originalRequest.getStartValidity());
        registryEntity.setEndValidity(originalRequest.getEndValidity());

        return registryEntity;
    }

    private Mono<PnAddressManagerEvent.ResultItem> getRelativeItemFromAddressManagerEvent(List<PnAddressManagerEvent.ResultItem> resultItems, String id) {
        Optional<PnAddressManagerEvent.ResultItem> resultItemOptional = resultItems.stream().filter(item -> StringUtils.equals(item.getId(), id)).findFirst();
        if (resultItemOptional.isEmpty()) {
            log.warn("Item with id {} not found or not in event list", id);
            return Mono.empty();
        }
        return Mono.just(resultItemOptional.get());
    }
}
