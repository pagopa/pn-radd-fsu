package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.UpdateRegistryRequest;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@CustomLog
public class RegistryService {

    private final RaddRegistryDAO raddRegistryDAO;

    public RaddRegistryEntity updateRegistry(String registryId, UpdateRegistryRequest request) {
        log.info("start updateRegistry for registryId: {}", registryId);
        RaddRegistryEntity entity = new RaddRegistryEntity();
        entity.setRegistryId(registryId);
        entity.setDescription(request.getDescription());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setOpeningTime(request.getOpeningTime());
        return raddRegistryDAO.updateRegistryEntity(entity)
                .switchIfEmpty(Mono.error(new RaddGenericException(String.format("No registry found for registryId: [%s] ", registryId))))
                .doOnError(throwable -> log.error("Error during update registry request for registryId: [{}]", registryId, throwable))
                .block();
    }

}
