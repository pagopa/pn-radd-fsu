package it.pagopa.pn.radd.middleware.queue.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.exception.CorrelationIdNotFoundException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.PnRaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.PnRaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.ImportStatus;
import it.pagopa.pn.radd.pojo.OriginalRequest;
import it.pagopa.pn.radd.pojo.PnAddressManagerRequestDTO;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
@AllArgsConstructor
@Slf4j
public class AddressManagerRequestHandler {
    private final PnRaddRegistryRequestDAO pnRaddRegistryRequestDAO;
    private final PnRaddRegistryDAO pnRaddRegistryDAO;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ERROR_DUPLICATE = "Rifiutato in quanto duplicato";
    private static final String ERROR_IMPORT = "Rifiutato in quanto errore di import";


    private Mono<Void> processMessage(List<PnAddressManagerRequestDTO.ResultItem> resultItems, String correlationId) {
        // 2
        return pnRaddRegistryRequestDAO.findWithStatus(correlationId, ImportStatus.PENDING)  // Ottengo tutti gli elementi da processare
                .switchIfEmpty(Flux.error(new CorrelationIdNotFoundException()))
                .flatMap(richiesteSediRaddItem -> getRelativeItemFromMessage(resultItems, richiesteSediRaddItem.getPk())
                        .flatMap(item -> {
                            String error = item.getError();
                            if (error != null && !error.isEmpty()) {
                                log.warn("Id {} error not empty", item.getError());
                                return pnRaddRegistryRequestDAO.updateStatusAndError(richiesteSediRaddItem, ImportStatus.REJECTED, ERROR_IMPORT);
                            }
                            return handleRegistryUpdate(richiesteSediRaddItem, item).then();
                        }))
                .doOnError(
                        CorrelationIdNotFoundException.class,
                        exception -> log.warn("correlationId {} not found or not in PENDING", correlationId)
                )
                .then();
    }

    @NotNull
    private Flux<RaddRegistryRequestEntity> handleRegistryUpdate(RaddRegistryRequestEntity richiesteSediRaddItem, PnAddressManagerRequestDTO.ResultItem resultItem) {
        UUID registryId = UUID.nameUUIDFromBytes(resultItem.getNormalizedAddress().toString().getBytes());
        // 4
        return pnRaddRegistryDAO.find(registryId.toString(), richiesteSediRaddItem.getCxId())
                .switchIfEmpty(createNewRegistryEntity(richiesteSediRaddItem, resultItem))
                .flatMap(entity -> updateRegistryEntity(richiesteSediRaddItem, entity))
                .onErrorResume(throwable -> {
                    if (throwable instanceof RaddGenericException ex && ERROR_DUPLICATE.equals(ex.getMessage())) {
                        return pnRaddRegistryRequestDAO.updateStatusAndError(
                                richiesteSediRaddItem,
                                ImportStatus.REJECTED,
                                ERROR_DUPLICATE
                        );
                    }
                    return Mono.error(throwable);
                });
    }

    @NotNull
    private Mono<RaddRegistryRequestEntity> updateRegistryEntity(RaddRegistryRequestEntity richiesteSediRaddItem, RaddRegistryEntity raddRegistryEntity) {
            if (StringUtils.equals(raddRegistryEntity.getRequestId(), richiesteSediRaddItem.getRequestId())) {
                return pnRaddRegistryRequestDAO.updateStatusAndError(richiesteSediRaddItem, ImportStatus.REJECTED, ERROR_DUPLICATE);
            } else {
                return mergeNewRegistryEntity(raddRegistryEntity, richiesteSediRaddItem)
                        .flatMap(updatedEntity -> pnRaddRegistryDAO.updateRegistryEntity(updatedEntity)
                                .flatMap(unused -> pnRaddRegistryRequestDAO.updateRichiesteSediRaddStatus(richiesteSediRaddItem, RegistryRequestStatus.ACCEPTED)));
            }
    }

    @NotNull
    private Mono<RaddRegistryEntity> createNewRegistryEntity(RaddRegistryRequestEntity richiesteSediRaddItem, PnAddressManagerRequestDTO.ResultItem resultItem) {
        return constructAnagraficheRaddItem(resultItem.getNormalizedAddress(), richiesteSediRaddItem.getCxId(), richiesteSediRaddItem)
                .flatMap(item -> this.pnRaddRegistryDAO.createNewRegistryEntity(item)
                            .onErrorResume(ConditionalCheckFailedException.class, ex -> Mono.error(new RaddGenericException(ERROR_DUPLICATE))));

    }

    public Mono<RaddRegistryEntity> mergeNewRegistryEntity(RaddRegistryEntity raddRegistryEntity, RaddRegistryRequestEntity raddRegistryRequestEntity) {
        return Mono.fromCallable(() -> {
            OriginalRequest originalRequest = objectMapper.readValue(raddRegistryRequestEntity.getOriginalRequest(), OriginalRequest.class);

            return RaddRegistryEntity.builder()
                    .registryId(raddRegistryEntity.getRegistryId())
                    .cxId(raddRegistryEntity.getCxId())
                    .normalizedAddress(raddRegistryEntity.getNormalizedAddress())
                    .description(raddRegistryEntity.getDescription())
                    .phoneNumber(raddRegistryEntity.getPhoneNumber())
                    .geoLocation(raddRegistryEntity.getGeoLocation())
                    // Metadata from originalRequest
                    .description(originalRequest.getDescription())
                    .phoneNumber(originalRequest.getPhoneNumber())
                    .geoLocation(originalRequest.getGeoLocation())
                    .zipCode(raddRegistryRequestEntity.getZipCode())
                    .openingTime(originalRequest.getOpeningTime())
                    .startValidity(originalRequest.getStartValidity())
                    .endValidity(originalRequest.getEndValidity())
                    .build();
        });
    }
    
    private Mono<RaddRegistryEntity> constructAnagraficheRaddItem(PnAddressManagerRequestDTO.NormalizedAddress normalizedAddress, String id, RaddRegistryRequestEntity registryRequest) {
        return Mono.fromCallable(() -> {
            String normalizedAddressString = objectMapper.writeValueAsString(normalizedAddress);
            OriginalRequest originalRequest = objectMapper.readValue(registryRequest.getOriginalRequest(), OriginalRequest.class);

            return RaddRegistryEntity.builder()
                    .registryId(registryRequest.getRegistryId())
                    .cxId(id)
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

    private Mono<PnAddressManagerRequestDTO.ResultItem> getRelativeItemFromMessage(List<PnAddressManagerRequestDTO.ResultItem> resultItems, String id) {
        Optional<PnAddressManagerRequestDTO.ResultItem> resultItemOptional = resultItems.stream().filter(item -> StringUtils.equals(item.getId(), id)).findFirst();
        if (resultItemOptional.isEmpty()) {
            log.warn("Item with id {} not found or not in event list", id);
            return Mono.empty();
        }
        return Mono.just(resultItemOptional.get());
    }

    public Mono<Void> handleMessage(PnAddressManagerRequestDTO message) {
        return processMessage(message.getBody().getResultItems(), message.getBody().getCorrelationId());
    }

}
