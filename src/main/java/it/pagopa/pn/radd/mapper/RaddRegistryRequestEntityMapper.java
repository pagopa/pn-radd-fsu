package it.pagopa.pn.radd.mapper;

import com.amazonaws.util.CollectionUtils;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CreateRegistryRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.GeoLocation;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.RaddRegistryOriginalRequest;
import it.pagopa.pn.radd.pojo.RaddRegistryRequest;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import it.pagopa.pn.radd.pojo.WrappedRaddRegistryOriginalRequest;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.radd.utils.Const.MISSING_ADDRESS_REQUIRED_FIELD;
import static it.pagopa.pn.radd.utils.DateUtils.convertDateToInstantAtStartOfDay;

@RequiredArgsConstructor
@Component
public class RaddRegistryRequestEntityMapper {

    private final ObjectMapperUtil objectMapperUtil;

    public RaddRegistryOriginalRequest retrieveOriginalRequest(CreateRegistryRequest request) {
        RaddRegistryOriginalRequest originalRequest = new RaddRegistryOriginalRequest();
        if (request.getAddress() != null) {
            originalRequest.setAddressRow(request.getAddress().getAddressRow());
            originalRequest.setCap(request.getAddress().getCap());
            originalRequest.setCity(request.getAddress().getCity());
            originalRequest.setPr(request.getAddress().getPr());
            originalRequest.setCountry(request.getAddress().getCountry());
        }
        if (request.getStartValidity() != null) {
            Instant instant = convertDateToInstantAtStartOfDay(request.getStartValidity());

            originalRequest.setStartValidity(instant.toString());
        } else {
            originalRequest.setStartValidity(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toString());
        }

        if (request.getEndValidity() != null) {
            Instant instant = convertDateToInstantAtStartOfDay(request.getEndValidity());
            originalRequest.setEndValidity(instant.toString());
        }

        originalRequest.setOpeningTime(request.getOpeningTime());
        originalRequest.setDescription(request.getDescription());
        if (request.getGeoLocation() != null) {
            originalRequest.setGeoLocation(objectMapperUtil.toJson(request.getGeoLocation()));
        }
        originalRequest.setPhoneNumber(request.getPhoneNumber());
        originalRequest.setExternalCode(request.getExternalCode());
        originalRequest.setCapacity(request.getCapacity());

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

    public WrappedRaddRegistryOriginalRequest retrieveOriginalRequest(RaddRegistryRequest request) {
        List<String> errors = new ArrayList<>();
        WrappedRaddRegistryOriginalRequest wrappedRaddRegistryOriginalRequest = new WrappedRaddRegistryOriginalRequest();
        RaddRegistryOriginalRequest originalRequest = new RaddRegistryOriginalRequest();
        originalRequest.setAddressRow(request.getVia());
        originalRequest.setCap(request.getCap());
        originalRequest.setCity(request.getCitta());
        originalRequest.setPr(request.getProvincia());
        originalRequest.setCountry(request.getPaese());

        if (StringUtils.isNotBlank(request.getDataInizioValidita())) {
            try {
                LocalDate date = LocalDate.parse(request.getDataInizioValidita());
                originalRequest.setStartValidity(date.atStartOfDay().toInstant(ZoneOffset.UTC).toString());
            } catch (DateTimeParseException exception) {
                errors.add("Formato non valido per data inizio validità");
            }
        } else {
            originalRequest.setStartValidity(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toString());
        }

        if (StringUtils.isNotBlank(request.getDataFineValidita())) {
            try {
                LocalDate date = LocalDate.parse(request.getDataFineValidita());
                originalRequest.setEndValidity(date.atStartOfDay().toInstant(ZoneOffset.UTC).toString());
            } catch (DateTimeParseException exception) {
                errors.add("Formato non valido per data fine validità");
            }
        }

        originalRequest.setOpeningTime(request.getOrariApertura());
        originalRequest.setDescription(request.getDescrizione());

        if (StringUtils.isNotBlank(request.getCoordinateGeoReferenziali())) {
            originalRequest.setGeoLocation(objectMapperUtil.toJson(retrieveGeoLocationObject(request.getCoordinateGeoReferenziali())));
        }

        originalRequest.setPhoneNumber(request.getTelefono());
        originalRequest.setExternalCode(request.getExternalCode());
        originalRequest.setCapacity(request.getCapacita());

        if (!CollectionUtils.isNullOrEmpty(errors)) {
            wrappedRaddRegistryOriginalRequest.setErrors(errors);
        }
        wrappedRaddRegistryOriginalRequest.setRequest(originalRequest);
        return wrappedRaddRegistryOriginalRequest;
    }

    private GeoLocation retrieveGeoLocationObject(String coordinateGeoReferenziali) {
        String[] coordinates = coordinateGeoReferenziali.split(",");
        if (coordinates.length != 2) {
            return null;
        }
        GeoLocation geoLocation = new GeoLocation();
        geoLocation.setLatitude(coordinates[0]);
        geoLocation.setLongitude(coordinates[1]);
        return geoLocation;
    }

    public List<RaddRegistryRequestEntity> retrieveRaddRegistryRequestEntity(List<RaddRegistryRequest> raddRegistryRequests, RaddRegistryImportEntity importEntity) {
        List<RaddRegistryRequestEntity> entities = raddRegistryRequests.stream().map(raddRegistryRequest -> {
            WrappedRaddRegistryOriginalRequest originalRequest = retrieveOriginalRequest(raddRegistryRequest);

            String originalRequestString = objectMapperUtil.toJson(originalRequest);

            RaddRegistryRequestEntity requestEntity = new RaddRegistryRequestEntity();
            requestEntity.setPk(buildPk(importEntity, originalRequestString));
            requestEntity.setCxId(importEntity.getCxId());
            requestEntity.setRequestId(importEntity.getRequestId());
            requestEntity.setCreatedAt(Instant.now());
            requestEntity.setUpdatedAt(Instant.now());
            requestEntity.setOriginalRequest(originalRequestString);

            checkRequiredFieldsAndUpdateError(originalRequest);
            if (CollectionUtils.isNullOrEmpty(originalRequest.getErrors())) {
                requestEntity.setStatus(RegistryRequestStatus.REJECTED.name());
                requestEntity.setError(String.join(", ", originalRequest.getErrors()));
            } else {
                requestEntity.setStatus(RegistryRequestStatus.NOT_WORKED.name());
            }
            return requestEntity;
        }).toList();
        log.info("Retrieved {} raddRegistryRequestEntities from {} CSV rows.", entities.size(), raddRegistryRequests.size());
        return entities;
    }

    private void checkRequiredFieldsAndUpdateError(WrappedRaddRegistryOriginalRequest originalRequest) {
        if (checkAddressFields(originalRequest)) {
            originalRequest.getErrors().add(MISSING_ADDRESS_REQUIRED_FIELD);
        }
    }

    private static boolean checkAddressFields(WrappedRaddRegistryOriginalRequest originalRequest) {
        return StringUtils.isBlank(originalRequest.getRequest().getAddressRow())
                || StringUtils.isBlank(originalRequest.getRequest().getCap())
                || StringUtils.isBlank(originalRequest.getRequest().getCity())
                || StringUtils.isBlank(originalRequest.getRequest().getPr());
    }

    private static String buildPk(RaddRegistryImportEntity importEntity, String originalRequest) {
        UUID index = UUID.nameUUIDFromBytes(originalRequest.getBytes(StandardCharsets.UTF_8));
        return importEntity.getCxId() + "#" + importEntity.getRequestId() + "#" + index;
    }
}
