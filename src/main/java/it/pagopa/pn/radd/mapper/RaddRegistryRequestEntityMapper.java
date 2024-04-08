package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryRequest;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.RaddRegistryOriginalRequest;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class RaddRegistryRequestEntityMapper {

    private final ObjectMapperUtil objectMapperUtil;

    public RaddRegistryOriginalRequest retrieveOriginalRequest(CreateRegistryRequest request) {
        RaddRegistryOriginalRequest originalRequest = new RaddRegistryOriginalRequest();
        if(request.getAddress() != null) {
            originalRequest.setAddressRow(request.getAddress().getAddressRow());
            originalRequest.setCap(request.getAddress().getCap());
            originalRequest.setCity(request.getAddress().getCity());
            originalRequest.setPr(request.getAddress().getPr());
            originalRequest.setCountry(request.getAddress().getCountry());
        }
        if(StringUtils.isNotBlank(request.getStartValidity())) {
            LocalDate date = LocalDate.parse(request.getStartValidity());
            Instant instant = date.atStartOfDay().toInstant(ZoneOffset.UTC);
            originalRequest.setStartValidity(instant.toString());
        }else{
            originalRequest.setStartValidity(Instant.now().toString());
        }

        if(StringUtils.isNotBlank(request.getEndValidity())) {
            LocalDate date = LocalDate.parse(request.getStartValidity());
            Instant instant = date.atStartOfDay().toInstant(ZoneOffset.UTC);
            originalRequest.setEndValidity(instant.toString());
        }

        originalRequest.setOpeningTime(request.getOpeningTime());
        originalRequest.setDescription(request.getDescription());
        originalRequest.setGeoLocation(objectMapperUtil.toJson(request.getGeoLocation()));
        originalRequest.setPhoneNumber(request.getPhoneNumber());
        originalRequest.setExternalCode(request.getExternalCode());

        return originalRequest;
    }

    public RaddRegistryRequestEntity retrieveRaddRegistryRequestEntity(String cxId, String requestId, RaddRegistryOriginalRequest originalRequest) {

        String originalRequestString = objectMapperUtil.toJson(originalRequest);

        RaddRegistryRequestEntity requestEntity = new RaddRegistryRequestEntity();
        requestEntity.setPk(buildPk(cxId, requestId, originalRequestString));
        requestEntity.setCxId(cxId);
        requestEntity.setRequestId(requestId);
        requestEntity.setCorrelationId(requestId);
        requestEntity.setCreatedAt(Instant.now());
        requestEntity.setUpdatedAt(Instant.now());
        requestEntity.setOriginalRequest(originalRequestString);
        requestEntity.setStatus(RegistryRequestStatus.NOT_WORKED.name());
        return requestEntity;
    }
    private static String buildPk(String cxId, String requestId, String originalRequest) {
        UUID index = UUID.nameUUIDFromBytes(originalRequest.getBytes(StandardCharsets.UTF_8));
        return cxId + "#" + requestId + "#" + index;
    }
}
