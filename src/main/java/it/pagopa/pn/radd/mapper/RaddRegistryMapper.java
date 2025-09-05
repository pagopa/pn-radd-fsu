package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.RegistryV2;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntityV2;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@CustomLog
public class RaddRegistryMapper extends AbstractRegistryMapper {

    private final NormalizedAddressMapper normalizedAddressMapper;
    private final AddressMapper addressMapper;

    public RegistryV2 toDto(RaddRegistryEntityV2 entity) {
        if (entity == null) {
            return null;
        }

        RegistryV2 dto = new RegistryV2();
        dto.setPartnerId(entity.getPartnerId());
        dto.setLocationId(entity.getLocationId());
        dto.setExternalCodes(entity.getExternalCodes());
        dto.setPhoneNumbers(entity.getPhoneNumbers());
        dto.setEmail(entity.getEmail());
        dto.setAppointmentRequired(entity.getAppointmentRequired());
        dto.setWebsite(entity.getWebsite());
        dto.setPartnerType(entity.getPartnerType());
        dto.setCreationTimestamp(toDate(entity.getCreationTimestamp()));
        dto.setUpdateTimestamp(toDate(entity.getUpdateTimestamp()));
        dto.setDescription(entity.getDescription());
        dto.setOpeningTime(entity.getOpeningTime());
        dto.setStartValidity(toStringDate(entity.getStartValidity()));
        dto.setEndValidity(toStringDate(entity.getEndValidity()));
        dto.setAddress(addressMapper.toDto(entity.getAddress()));
        dto.setNormalizedAddress(normalizedAddressMapper.toDto(entity.getNormalizedAddress()));

        return dto;
    }

    public RaddRegistryEntityV2 toEntity(RegistryV2 dto) {
        if (dto == null) {
            return null;
        }

        RaddRegistryEntityV2 entity = new RaddRegistryEntityV2();
        entity.setPartnerId(dto.getPartnerId());
        entity.setLocationId(dto.getLocationId());
        entity.setExternalCodes(dto.getExternalCodes());
        entity.setPhoneNumbers(dto.getPhoneNumbers());
        entity.setEmail(dto.getEmail());
        entity.setAppointmentRequired(dto.getAppointmentRequired());
        entity.setWebsite(dto.getWebsite());
        entity.setPartnerType(dto.getPartnerType());
        entity.setCreationTimestamp(toInstant(dto.getCreationTimestamp()));
        entity.setUpdateTimestamp(toInstant(dto.getUpdateTimestamp()));
        entity.setDescription(dto.getDescription());
        entity.setOpeningTime(dto.getOpeningTime());
        entity.setStartValidity(parseDateString(dto.getStartValidity()));
        entity.setEndValidity(parseDateString(dto.getEndValidity()));
        entity.setAddress(addressMapper.toEntity(dto.getAddress()));
        entity.setNormalizedAddress(normalizedAddressMapper.toEntity(dto.getNormalizedAddress()));

        return entity;
    }

}
