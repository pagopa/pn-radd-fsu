package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.NormalizedAddress;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryV2;
import it.pagopa.pn.radd.middleware.db.entities.NormalizedAddressEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntityV2;
import it.pagopa.pn.radd.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RaddRegistryMapperTest {

    private RaddRegistryMapper mapper;
    private NormalizedAddressMapper normalizedAddressMapper;

    @BeforeEach
    void setUp() {
        normalizedAddressMapper = mock(NormalizedAddressMapper.class);
        mapper = new RaddRegistryMapper(normalizedAddressMapper);
    }

    @Test
    void testToDto_withValidEntity_shouldMapCorrectly() {
        RaddRegistryEntityV2 entity = new RaddRegistryEntityV2();
        entity.setPartnerId("partner-1");
        entity.setLocationId("loc-1");
        entity.setExternalCodes(List.of("EX", "001"));
        entity.setPhoneNumbers(List.of("1234567890"));
        entity.setEmail("test@example.com");
        entity.setAppointmentRequired(true);
        entity.setWebsite("https://example.com");
        entity.setPartnerType("TYPE1");
        entity.setCreationTimestamp(Instant.parse("2025-08-01T12:00:00Z"));
        entity.setUpdateTimestamp(Instant.parse("2025-08-20T18:00:00Z"));
        entity.setDescription("A registry entity");
        entity.setOpeningTime("MO:09-12");
        entity.setStartValidity(Instant.parse("2025-01-01T15:00:00Z"));
        entity.setEndValidity(Instant.parse("2025-02-01T15:00:00Z"));
        entity.setNormalizedAddress(new NormalizedAddressEntity());

        NormalizedAddress normalizedAddress = new NormalizedAddress();
        when(normalizedAddressMapper.toDto(any())).thenReturn(normalizedAddress);

        RegistryV2 dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals("partner-1", dto.getPartnerId());
        assertEquals("loc-1", dto.getLocationId());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("https://example.com", dto.getWebsite());
        assertEquals("TYPE1", dto.getPartnerType());
        assertEquals(DateUtils.parseDateString("2025-08-01T12:00:00Z"), dto.getCreationTimestamp());
        assertEquals(DateUtils.parseDateString("2025-08-20T18:00:00Z"), dto.getUpdateTimestamp());
        assertEquals("2025-01-01", dto.getStartValidity());
        assertEquals("2025-02-01", dto.getEndValidity());
        assertEquals(normalizedAddress, dto.getNormalizedAddress());
    }

    @Test
    void testToDto_withNullEntity_shouldReturnNull() {
        RegistryV2 dto = mapper.toDto(null);
        assertNull(dto);
    }

    @Test
    void testToEntity_withValidDto_shouldMapCorrectly() {
        RegistryV2 dto = new RegistryV2();
        dto.setPartnerId("partner-2");
        dto.setLocationId("loc-2");
        dto.setExternalCodes(List.of("EXT", "002"));
        dto.setPhoneNumbers(List.of("0987654321"));
        dto.setEmail("email@test.com");
        dto.setAppointmentRequired(false);
        dto.setWebsite("https://test.com");
        dto.setPartnerType("TYPE2");
        dto.setCreationTimestamp(DateUtils.parseDateString("2025-07-01T08:30:00Z"));
        dto.setUpdateTimestamp(DateUtils.parseDateString("2025-08-01T09:45:00Z"));
        dto.setDescription("Another registry");
        dto.setOpeningTime("TU:08-10");
        dto.setStartValidity("2025-07-01");
        dto.setEndValidity("2025-10-01");
        dto.setNormalizedAddress(new NormalizedAddress());

        NormalizedAddressEntity normalizedAddressEntity = new NormalizedAddressEntity();
        when(normalizedAddressMapper.toEntity(any())).thenReturn(normalizedAddressEntity);

        RaddRegistryEntityV2 entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("partner-2", entity.getPartnerId());
        assertEquals("loc-2", entity.getLocationId());
        assertEquals("email@test.com", entity.getEmail());
        assertEquals("https://test.com", entity.getWebsite());
        assertEquals("TYPE2", entity.getPartnerType());
        assertEquals(Instant.parse("2025-07-01T08:30:00Z"), entity.getCreationTimestamp());
        assertEquals(Instant.parse("2025-08-01T09:45:00Z"), entity.getUpdateTimestamp());
        assertEquals(DateUtils.convertDateToInstantAtStartOfDay("2025-07-01"), entity.getStartValidity());
        assertEquals(DateUtils.convertDateToInstantAtStartOfDay("2025-10-01"), entity.getEndValidity());
        assertEquals(normalizedAddressEntity, entity.getNormalizedAddress());
    }

    @Test
    void testToEntity_withNullDto_shouldReturnNull() {
        RaddRegistryEntityV2 entity = mapper.toEntity(null);
        assertNull(entity);
    }
}