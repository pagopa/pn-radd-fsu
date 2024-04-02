package it.pagopa.pn.radd.middleware.queue.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.PnRaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.PnRaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.ImportStatus;
import it.pagopa.pn.radd.pojo.OriginalRequest;
import it.pagopa.pn.radd.middleware.queue.consumer.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class AddressManagerRequestHandler {
    private final PnRaddRegistryRequestDAO pnRaddRegistryRequestDAO;
    private final PnRaddRegistryDAO pnRaddRegistryDAO;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ERROR_DUPLICATE = "Rifiutato in quanto duplicato";


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

    @NotNull
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

    @NotNull
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

    @NotNull
    private Mono<RaddRegistryRequestEntity> updateRegistryRequestEntity(RaddRegistryRequestEntity newRegistryRequestEntity, RaddRegistryEntity preExistingRegistryEntity) {
            if (StringUtils.equals(preExistingRegistryEntity.getRequestId(), newRegistryRequestEntity.getRequestId())) {
                return pnRaddRegistryRequestDAO.updateStatusAndError(newRegistryRequestEntity, ImportStatus.REJECTED, ERROR_DUPLICATE);
            } else {
                return mergeNewRegistryEntity(preExistingRegistryEntity, newRegistryRequestEntity)
                        .flatMap(updatedEntity -> pnRaddRegistryDAO.updateRegistryEntity(updatedEntity)
                                .flatMap(unused -> pnRaddRegistryRequestDAO.updateRichiesteSediRaddStatus(newRegistryRequestEntity, RegistryRequestStatus.ACCEPTED)));
            }
    }

    @NotNull
    private Mono<RaddRegistryRequestEntity> createNewRegistryEntity(RaddRegistryRequestEntity raddRegistryRequestEntity, PnAddressManagerEvent.ResultItem resultItem) {
        return constructRaddRegistryEntity(resultItem.getNormalizedAddress(), raddRegistryRequestEntity)
                .flatMap(item -> this.pnRaddRegistryDAO.putItemIfAbsent(item)
                            .onErrorResume(ConditionalCheckFailedException.class, ex -> Mono.error(new RaddGenericException(ERROR_DUPLICATE))))
                .flatMap(unused -> pnRaddRegistryRequestDAO.updateRichiesteSediRaddStatus(raddRegistryRequestEntity, RegistryRequestStatus.ACCEPTED));

    }

    public Mono<RaddRegistryEntity> mergeNewRegistryEntity(RaddRegistryEntity preExistingRegistryEntity, RaddRegistryRequestEntity newRegistryRequestEntity) {
        return Mono.fromCallable(() -> {
            OriginalRequest originalRequest = objectMapper.readValue(newRegistryRequestEntity.getOriginalRequest(), OriginalRequest.class);

            return RaddRegistryEntity.builder()
                    .registryId(preExistingRegistryEntity.getRegistryId())
                    .cxId(preExistingRegistryEntity.getCxId())
                    .normalizedAddress(preExistingRegistryEntity.getNormalizedAddress())
                    .requestId(newRegistryRequestEntity.getRequestId())
                    // Metadata from originalRequest
                    .description(originalRequest.getDescription())
                    .phoneNumber(originalRequest.getPhoneNumber())
                    .geoLocation(originalRequest.getGeoLocation())
                    .zipCode(newRegistryRequestEntity.getZipCode())
                    .openingTime(originalRequest.getOpeningTime())
                    .startValidity(originalRequest.getStartValidity())
                    .endValidity(originalRequest.getEndValidity())
                    .build();
        });
    }
    
    private Mono<RaddRegistryEntity> constructRaddRegistryEntity(PnAddressManagerEvent.NormalizedAddress normalizedAddress, RaddRegistryRequestEntity registryRequest) {
        return Mono.fromCallable(() -> {
            String normalizedAddressString = objectMapper.writeValueAsString(normalizedAddress);
            OriginalRequest originalRequest = objectMapper.readValue(registryRequest.getOriginalRequest(), OriginalRequest.class);

            return RaddRegistryEntity.builder()
                    .registryId(registryRequest.getRegistryId())
                    .cxId(registryRequest.getCxId())
                    .requestId(registryRequest.getRequestId())
                    .normalizedAddress(normalizedAddressString)
                    // Metadata from originalRequest
                    .description(originalRequest.getDescription())
                    .phoneNumber(originalRequest.getPhoneNumber())
                    .geoLocation(originalRequest.getGeoLocation())
                    .zipCode(normalizedAddress.getCap())
                    .openingTime(originalRequest.getOpeningTime())
                    .startValidity(originalRequest.getStartValidity())
                    .endValidity(originalRequest.getEndValidity())
                    .build();
        });
    }

    private Mono<PnAddressManagerEvent.ResultItem> getRelativeItemFromAddressManagerEvent(List<PnAddressManagerEvent.ResultItem> resultItems, String id) {
        Optional<PnAddressManagerEvent.ResultItem> resultItemOptional = resultItems.stream().filter(item -> StringUtils.equals(item.getId(), id)).findFirst();
        if (resultItemOptional.isEmpty()) {
            log.warn("Item with id {} not found or not in event list", id);
            return Mono.empty();
        }
        return Mono.just(resultItemOptional.get());
    }

    public Mono<Void> handleMessage(PnAddressManagerEvent message) {
        return processMessage(message.getPayload().getResultItems(), message.getPayload().getCorrelationId());
    }

}
