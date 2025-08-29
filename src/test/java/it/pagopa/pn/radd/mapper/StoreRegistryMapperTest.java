package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryV2;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.StoreRegistry;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntityV2;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.NormalizedAddress;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.AddressV2;
import it.pagopa.pn.radd.middleware.db.entities.NormalizedAddressEntity;
import it.pagopa.pn.radd.middleware.db.entities.AddressEntity;
import it.pagopa.pn.radd.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreRegistryMapperTest {

    private StoreRegistryMapper mapper;

    private RaddRegistryMapper raddRegistryMapper;
    private NormalizedAddressMapper normalizedAddressMapper;
    private AddressMapper addressMapper;

    @BeforeEach
    void setUp() {
        raddRegistryMapper = mock(RaddRegistryMapper.class);
        normalizedAddressMapper = mock(NormalizedAddressMapper.class);
        addressMapper = mock(AddressMapper.class);

        mapper = new StoreRegistryMapper(raddRegistryMapper, normalizedAddressMapper, addressMapper);
    }

    @Test
    void testToDto_withValidEntity_shouldMapCorrectly() {
        RaddRegistryEntityV2 entity = new RaddRegistryEntityV2();
        entity.setNormalizedAddress(new NormalizedAddressEntity());
        entity.setAddress(new AddressEntity());

        RegistryV2 registryDto = new RegistryV2();
        registryDto.setPartnerId("PARTNER_1");
        registryDto.setLocationId("LOC_1");
        registryDto.setOpeningTime("MO:09-12");

        when(raddRegistryMapper.toDto(entity)).thenReturn(registryDto);
        when(normalizedAddressMapper.toDto(any())).thenReturn(new NormalizedAddress());
        when(addressMapper.toDto(any())).thenReturn(new AddressV2());

        StoreRegistry result = mapper.toDto(entity);

        assertNotNull(result);
        assertEquals("PARTNER_1", result.getPartnerId());
        assertEquals("LOC_1", result.getLocationId());
        verify(raddRegistryMapper).toDto(entity);
    }

    @Test
    void testToDto_withNullEntity_shouldReturnNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void testToEntity_withValidDto_shouldMapCorrectly() {
        StoreRegistry dto = new StoreRegistry();
        dto.setPartnerId("PARTNER_2");
        dto.setLocationId("LOC_2");
        dto.setOpeningTime(Map.of("MO", "08-13"));
        dto.setCreationTimestamp(DateUtils.parseDateString("2025-08-20T12:00:00Z"));
        dto.setUpdateTimestamp(DateUtils.parseDateString("2025-08-25T14:00:00Z"));
        dto.setStartValidity("2025-08-01");
        dto.setEndValidity("2025-12-31");
        dto.setNormalizedAddress(new NormalizedAddress());
        dto.setAddress(new AddressV2());

        when(normalizedAddressMapper.toEntity(any())).thenReturn(new NormalizedAddressEntity());
        when(addressMapper.toEntity(any())).thenReturn(new AddressEntity());

        RaddRegistryEntityV2 entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("PARTNER_2", entity.getPartnerId());
        assertEquals("LOC_2", entity.getLocationId());
        assertEquals("08-13", ((Map<String, String>) dto.getOpeningTime()).get("MO"));
        assertEquals(Instant.parse("2025-08-20T12:00:00Z"), entity.getCreationTimestamp());
        assertEquals(Instant.parse("2025-08-25T14:00:00Z"), entity.getUpdateTimestamp());
        assertNotNull(entity.getNormalizedAddress());
        assertNotNull(entity.getAddress());
    }

    @Test
    void testToEntity_withNull_shouldReturnNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void testToEntity_withInvalidOpeningTime_shouldFallbackToString() {
        StoreRegistry dto = new StoreRegistry();
        dto.setOpeningTime("some-invalid-string");

        RaddRegistryEntityV2 entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("some-invalid-string", entity.getOpeningTime());
    }
}