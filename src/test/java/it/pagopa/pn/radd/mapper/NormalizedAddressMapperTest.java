package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.NormalizedAddress;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.NormalizedAddressAllOfBiasPoint;
import it.pagopa.pn.radd.middleware.db.entities.BiasPointEntity;
import it.pagopa.pn.radd.middleware.db.entities.NormalizedAddressEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NormalizedAddressMapperTest {

    private NormalizedAddressMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NormalizedAddressMapper();
    }

    @Test
    void testToEntity_fullDto() {
        NormalizedAddress dto = new NormalizedAddress();
        dto.setAddressRow("Via Roma 1");
        dto.setCap("00100");
        dto.setCity("Roma");
        dto.setProvince("RM");
        dto.setCountry("Italia");
        dto.setLatitude("41");
        dto.setLongitude("12");

        NormalizedAddressAllOfBiasPoint biasPoint = new NormalizedAddressAllOfBiasPoint();
        biasPoint.setAddressNumber(BigDecimal.valueOf(1));
        biasPoint.setCountry(BigDecimal.valueOf(2));
        biasPoint.setLocality(BigDecimal.valueOf(3));
        biasPoint.setPostalCode(BigDecimal.valueOf(4));
        biasPoint.setSubRegion(BigDecimal.valueOf(5));
        biasPoint.setOverall(BigDecimal.valueOf(6));

        dto.setBiasPoint(biasPoint);

        NormalizedAddressEntity entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("Via Roma 1", entity.getAddressRow());
        assertEquals("00100", entity.getCap());
        assertEquals("Roma", entity.getCity());
        assertEquals("RM", entity.getProvince());
        assertEquals("Italia", entity.getCountry());
        assertEquals("41", entity.getLatitude());
        assertEquals("12", entity.getLongitude());

        BiasPointEntity biasEntity = entity.getBiasPoint();
        assertNotNull(biasEntity);
        assertEquals(BigDecimal.valueOf(1), biasEntity.getAddressNumber());
        assertEquals(BigDecimal.valueOf(2), biasEntity.getCountry());
        assertEquals(BigDecimal.valueOf(3), biasEntity.getLocality());
        assertEquals(BigDecimal.valueOf(4), biasEntity.getPostalCode());
        assertEquals(BigDecimal.valueOf(5), biasEntity.getSubRegion());
        assertEquals(BigDecimal.valueOf(6), biasEntity.getOverall());
    }

    @Test
    void testToDto_fullEntity() {
        NormalizedAddressEntity entity = new NormalizedAddressEntity();
        entity.setAddressRow("Via Milano 2");
        entity.setCap("20100");
        entity.setCity("Milano");
        entity.setProvince("MI");
        entity.setCountry("Italia");
        entity.setLatitude("45");
        entity.setLongitude("9");

        BiasPointEntity biasPoint = new BiasPointEntity();
        biasPoint.setAddressNumber(BigDecimal.valueOf(1));
        biasPoint.setCountry(BigDecimal.valueOf(2));
        biasPoint.setLocality(BigDecimal.valueOf(3));
        biasPoint.setPostalCode(BigDecimal.valueOf(4));
        biasPoint.setSubRegion(BigDecimal.valueOf(5));
        biasPoint.setOverall(BigDecimal.valueOf(6));

        entity.setBiasPoint(biasPoint);

        NormalizedAddress dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals("Via Milano 2", dto.getAddressRow());
        assertEquals("20100", dto.getCap());
        assertEquals("Milano", dto.getCity());
        assertEquals("MI", dto.getProvince());
        assertEquals("Italia", dto.getCountry());
        assertEquals("45", dto.getLatitude());
        assertEquals("9", dto.getLongitude());

        NormalizedAddressAllOfBiasPoint dtoBias = dto.getBiasPoint();
        assertNotNull(dtoBias);
        assertEquals(BigDecimal.valueOf(1), dtoBias.getAddressNumber());
        assertEquals(BigDecimal.valueOf(2), dtoBias.getCountry());
        assertEquals(BigDecimal.valueOf(3), dtoBias.getLocality());
        assertEquals(BigDecimal.valueOf(4), dtoBias.getPostalCode());
        assertEquals(BigDecimal.valueOf(5), dtoBias.getSubRegion());
        assertEquals(BigDecimal.valueOf(6), dtoBias.getOverall());
    }

    @Test
    void testToEntity_nullInput() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void testToDto_nullInput() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void testToEntity_nullBiasPoint() {
        NormalizedAddress dto = new NormalizedAddress();
        dto.setAddressRow("Via Test");
        dto.setBiasPoint(null);

        NormalizedAddressEntity entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertNull(entity.getBiasPoint());
    }

    @Test
    void testToDto_nullBiasPoint() {
        NormalizedAddressEntity entity = new NormalizedAddressEntity();
        entity.setAddressRow("Via Test");
        entity.setBiasPoint(null);

        NormalizedAddress dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertNull(dto.getBiasPoint());
    }

}