package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.AddressV2;
import it.pagopa.pn.radd.middleware.db.entities.AddressEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressMapperTest {

    private AddressMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AddressMapper();
    }

    @Test
    void testToEntity_fromDto_success() {
        AddressV2 dto = new AddressV2();
        dto.setAddressRow("Via Venezia 10");
        dto.setCap("30100");
        dto.setCity("Venezia");
        dto.setProvince("VE");
        dto.setCountry("Italia");

        AddressEntity entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("Via Venezia 10", entity.getAddressRow());
        assertEquals("30100", entity.getCap());
        assertEquals("Venezia", entity.getCity());
        assertEquals("VE", entity.getProvince());
        assertEquals("Italia", entity.getCountry());
    }

    @Test
    void testToDto_fromEntity_success() {
        AddressEntity entity = new AddressEntity();
        entity.setAddressRow("Corso Italia 5");
        entity.setCap("20100");
        entity.setCity("Milano");
        entity.setProvince("MI");
        entity.setCountry("Italia");

        AddressV2 dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals("Corso Italia 5", dto.getAddressRow());
        assertEquals("20100", dto.getCap());
        assertEquals("Milano", dto.getCity());
        assertEquals("MI", dto.getProvince());
        assertEquals("Italia", dto.getCountry());
    }

    @Test
    void testToEntity_withNullInput_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void testToDto_withNullInput_returnsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void testToEntity_withEmptyDto() {
        AddressV2 dto = new AddressV2();
        AddressEntity entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertNull(entity.getAddressRow());
        assertNull(entity.getCap());
        assertNull(entity.getCity());
        assertNull(entity.getProvince());
        assertNull(entity.getCountry());
    }

    @Test
    void testToDto_withEmptyEntity() {
        AddressEntity entity = new AddressEntity();
        AddressV2 dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertNull(dto.getAddressRow());
        assertNull(dto.getCap());
        assertNull(dto.getCity());
        assertNull(dto.getProvince());
        assertNull(dto.getCountry());
    }
}