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

@Service
@RequiredArgsConstructor
@CustomLog
public class RegistrySelfService {

    private final RaddRegistryDAO raddRegistryDAO;

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

}
