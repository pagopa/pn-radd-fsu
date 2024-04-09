package it.pagopa.pn.radd.services.radd.fsu.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.pojo.PnLastEvaluatedKey;
import it.pagopa.pn.radd.pojo.ResultPaginationDto;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import it.pagopa.pn.radd.utils.RaddRegistryUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;

import static it.pagopa.pn.radd.pojo.PnLastEvaluatedKey.ERROR_CODE_PN_RADD_ALT_UNSUPPORTED_LAST_EVALUATED_KEY;

@Service
@RequiredArgsConstructor
@CustomLog
public class RegistrySelfService {

    private final RaddRegistryDAO raddRegistryDAO;
    private final RaddRegistryRequestDAO raddRegistryRequestDAO;
    private final RaddRegistryUtils raddRegistryUtils;

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

    public Mono<RegistriesResponse> registryListing(String xPagopaPnCxId, Integer limit, String lastKey, String cap, String city, String pr, String externalCode) {
        PnLastEvaluatedKey lastEvaluatedKey = null;
        if (lastKey != null) {
            try {
                lastEvaluatedKey = PnLastEvaluatedKey.deserializeInternalLastEvaluatedKey(lastKey);
            } catch (JsonProcessingException e) {
                throw new PnInternalException("Unable to deserialize lastEvaluatedKey",
                        ERROR_CODE_PN_RADD_ALT_UNSUPPORTED_LAST_EVALUATED_KEY,
                        e);
            }
        } else {
            log.debug("First page search");
        }

        log.info("start registryListing for xPagopaPnCxId={} and limit: [{}] and lastKey: [{}] and cap: [{}] and city: [{}] and pr: [{}] and externalCode: [{}].", xPagopaPnCxId, limit, lastKey, cap, city, pr, externalCode);
        return raddRegistryRequestDAO.findAll(xPagopaPnCxId, limit, cap, city, pr, externalCode, lastEvaluatedKey)
                .map(resultPaginationDto -> raddRegistryUtils.prepareRaddRegistrySelfResult(resultPaginationDto.getResultsPage(), resultPaginationDto.isMoreResult(), limit));
    }

}