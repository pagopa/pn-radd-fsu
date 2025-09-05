package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.NormalizedAddress;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.NormalizedAddressAllOfBiasPoint;
import it.pagopa.pn.radd.middleware.db.entities.BiasPointEntity;
import it.pagopa.pn.radd.middleware.db.entities.NormalizedAddressEntity;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@CustomLog
public class NormalizedAddressMapper {

    public NormalizedAddressEntity toEntity(NormalizedAddress dto) {
        if (dto == null) {
            return null;
        }
        NormalizedAddressEntity entity = new NormalizedAddressEntity();
        entity.setAddressRow(dto.getAddressRow());
        entity.setCap(dto.getCap());
        entity.setCity(dto.getCity());
        entity.setProvince(dto.getProvince());
        entity.setCountry(dto.getCountry());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setBiasPoint(toEntity(dto.getBiasPoint()));
        return entity;
    }

    public NormalizedAddress toDto(NormalizedAddressEntity entity) {
        if (entity == null) {
            return null;
        }
        NormalizedAddress dto = new NormalizedAddress();
        dto.setAddressRow(entity.getAddressRow());
        dto.setCap(entity.getCap());
        dto.setCity(entity.getCity());
        dto.setProvince(entity.getProvince());
        dto.setCountry(entity.getCountry());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setBiasPoint(toDto(entity.getBiasPoint()));
        return dto;
    }

    private BiasPointEntity toEntity(NormalizedAddressAllOfBiasPoint biasPoint) {
        if (biasPoint == null) {
            return null;
        }
        BiasPointEntity biasPointEntity = new BiasPointEntity();
        biasPointEntity.setAddressNumber(biasPoint.getAddressNumber());
        biasPointEntity.setCountry(biasPoint.getCountry());
        biasPointEntity.setLocality(biasPoint.getLocality());
        biasPointEntity.setPostalCode(biasPoint.getPostalCode());
        biasPointEntity.setSubRegion(biasPoint.getSubRegion());
        biasPointEntity.setOverall(biasPoint.getOverall());
        return biasPointEntity;
    }

    private NormalizedAddressAllOfBiasPoint toDto(BiasPointEntity biasPointEntity) {
        if (biasPointEntity == null) {
            return null;
        }
        NormalizedAddressAllOfBiasPoint biasPoint = new NormalizedAddressAllOfBiasPoint();
        biasPoint.setAddressNumber(biasPointEntity.getAddressNumber());
        biasPoint.setCountry(biasPointEntity.getCountry());
        biasPoint.setLocality(biasPointEntity.getLocality());
        biasPoint.setPostalCode(biasPointEntity.getPostalCode());
        biasPoint.setSubRegion(biasPointEntity.getSubRegion());
        biasPoint.setOverall(biasPointEntity.getOverall());
        return biasPoint;
    }
}
