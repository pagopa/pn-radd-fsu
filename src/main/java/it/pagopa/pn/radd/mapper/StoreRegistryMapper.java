package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.RegistryV2;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.StoreRegistry;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntityV2;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static it.pagopa.pn.radd.utils.OpeningHoursParser.parseOpeningHours;
import static it.pagopa.pn.radd.utils.OpeningHoursParser.serializeOpeningHours;

@Component
@RequiredArgsConstructor
@CustomLog
public class StoreRegistryMapper extends AbstractRegistryMapper {

    private final RaddRegistryMapper raddRegistryMapper;
    private final NormalizedAddressMapper normalizedAddressMapper;
    private final AddressMapper addressMapper;

    public StoreRegistry toDto(RaddRegistryEntityV2 entity) {
        if (entity == null) {
            return null;
        }
        RegistryV2 registryDto = raddRegistryMapper.toDto(entity);

        Map<String, String> parseOpeningTime = parseOpeningHours(registryDto.getOpeningTime());
        Object openingTime = (parseOpeningTime.isEmpty()) ? registryDto.getOpeningTime() : parseOpeningTime;

        StoreRegistry storeRegistry = new StoreRegistry();
        storeRegistry.setPartnerId(registryDto.getPartnerId());
        storeRegistry.setLocationId(registryDto.getLocationId());
        storeRegistry.setExternalCodes(registryDto.getExternalCodes());
        storeRegistry.setPhoneNumbers(registryDto.getPhoneNumbers());
        storeRegistry.setEmail(registryDto.getEmail());
        storeRegistry.setAppointmentRequired(registryDto.getAppointmentRequired());
        storeRegistry.setWebsite(registryDto.getWebsite());
        storeRegistry.setPartnerType(registryDto.getPartnerType());
        storeRegistry.setCreationTimestamp(registryDto.getCreationTimestamp());
        storeRegistry.setUpdateTimestamp(registryDto.getUpdateTimestamp());
        storeRegistry.setDescription(registryDto.getDescription());
        storeRegistry.setOpeningTime(openingTime);
        storeRegistry.setStartValidity(registryDto.getStartValidity());
        storeRegistry.setEndValidity(registryDto.getEndValidity());
        storeRegistry.setNormalizedAddress(normalizedAddressMapper.toDto(entity.getNormalizedAddress()));
        storeRegistry.setAddress(addressMapper.toDto(entity.getAddress()));

        return storeRegistry;
    }

    public RaddRegistryEntityV2 toEntity(StoreRegistry storeRegistry) {
        if (storeRegistry == null) {
            return null;
        }
        RaddRegistryEntityV2 entity = new RaddRegistryEntityV2();

        String openingTime;
        try {
            openingTime = serializeOpeningHours((Map<String, String>) storeRegistry.getOpeningTime());
        } catch (Exception e){
            openingTime = storeRegistry.getOpeningTime().toString();
        }

        entity.setPartnerId(storeRegistry.getPartnerId());
        entity.setLocationId(storeRegistry.getLocationId());
        entity.setExternalCodes(storeRegistry.getExternalCodes());
        entity.setPhoneNumbers(storeRegistry.getPhoneNumbers());
        entity.setEmail(storeRegistry.getEmail());
        entity.setAppointmentRequired(storeRegistry.getAppointmentRequired());
        entity.setWebsite(storeRegistry.getWebsite());
        entity.setPartnerType(storeRegistry.getPartnerType());
        entity.setCreationTimestamp(toInstant(storeRegistry.getCreationTimestamp()));
        entity.setUpdateTimestamp(toInstant(storeRegistry.getUpdateTimestamp()));
        entity.setDescription(storeRegistry.getDescription());
        entity.setOpeningTime(openingTime);
        entity.setStartValidity(parseDateString(storeRegistry.getStartValidity()));
        entity.setEndValidity(parseDateString(storeRegistry.getEndValidity()));
        entity.setNormalizedAddress(normalizedAddressMapper.toEntity(storeRegistry.getNormalizedAddress()));
        entity.setAddress(addressMapper.toEntity(storeRegistry.getAddress()));

        return entity;
    }

}