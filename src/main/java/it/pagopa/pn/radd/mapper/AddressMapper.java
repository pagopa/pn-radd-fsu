package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.AddressV2;
import it.pagopa.pn.radd.middleware.db.entities.AddressEntity;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@CustomLog
public class AddressMapper {

    public AddressEntity toEntity(AddressV2 dto) {
        if (dto == null) {
            return null;
        }
        AddressEntity entity = new AddressEntity();
        entity.setAddressRow(dto.getAddressRow());
        entity.setCap(dto.getCap());
        entity.setCity(dto.getCity());
        entity.setProvince(dto.getProvince());
        entity.setCountry(dto.getCountry());
        return entity;
    }

    public AddressV2 toDto(AddressEntity entity) {
        if (entity == null) {
            return null;
        }
        AddressV2 dto = new AddressV2();
        dto.setAddressRow(entity.getAddressRow());
        dto.setCap(entity.getCap());
        dto.setCity(entity.getCity());
        dto.setProvince(entity.getProvince());
        dto.setCountry(entity.getCountry());
        return dto;
    }

}
