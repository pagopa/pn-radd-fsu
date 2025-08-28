package it.pagopa.pn.radd.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.PnAttachmentsConfigEventItem;
import it.pagopa.pn.api.dto.events.PnAttachmentsConfigEventPayload;
import it.pagopa.pn.api.dto.events.PnEvaluatedZipCodeEvent;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.AnalogAddressDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.NormalizeItemsRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.NormalizeRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RequestResponse;
import it.pagopa.pn.radd.config.CachedSecretsManagerConsumer;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.entities.NormalizedAddressEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.queue.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.pojo.*;
import it.pagopa.pn.radd.services.radd.fsu.v1.SecretService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import static it.pagopa.pn.radd.utils.RaddRegistryUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaddRegistryUtilsTest {
    @InjectMocks
    private RaddRegistryUtils raddRegistryUtils;

    @Mock
    private ObjectMapperUtil objectMapperUtil;

    @Mock
    private PnRaddFsuConfig pnRaddFsuConfig;

    @Mock
    private SecretService secretService;

    /**
     * Method under test:
     * {@link RaddRegistryUtils#mergeNewRegistryEntity(RaddRegistryEntity, RaddRegistryRequestEntity)}
     */
    @Test
    void testMergeNewRegistryEntity() {
        // Arrange
        RaddRegistryEntity preExistingRegistryEntity = mock(RaddRegistryEntity.class);
        doNothing().when(preExistingRegistryEntity).setCxId(Mockito.any());
        doNothing().when(preExistingRegistryEntity).setDescription(Mockito.any());
        doNothing().when(preExistingRegistryEntity).setEndValidity(Mockito.any());
        doNothing().when(preExistingRegistryEntity).setGeoLocation(Mockito.any());
        doNothing().when(preExistingRegistryEntity).setNormalizedAddress(Mockito.any());
        doNothing().when(preExistingRegistryEntity).setOpeningTime(Mockito.any());
        doNothing().when(preExistingRegistryEntity).setPhoneNumber(Mockito.any());
        doNothing().when(preExistingRegistryEntity).setRegistryId(Mockito.any());
        doNothing().when(preExistingRegistryEntity).setRequestId(Mockito.any());
        doNothing().when(preExistingRegistryEntity).setStartValidity(Mockito.any());
        doNothing().when(preExistingRegistryEntity).setZipCode(Mockito.any());
        preExistingRegistryEntity.setCxId("42");
        preExistingRegistryEntity.setDescription("The characteristics of someone or something");
        preExistingRegistryEntity
                .setEndValidity(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        preExistingRegistryEntity.setGeoLocation("Geo Location");
        NormalizedAddressEntity addressEntity = new NormalizedAddressEntity();
        addressEntity.setCountry("country");
        addressEntity.setProvince("pr");
        addressEntity.setCity("city");
        addressEntity.setCap("cap");
        preExistingRegistryEntity.setNormalizedAddress(addressEntity);
        preExistingRegistryEntity.setOpeningTime("Opening Time");
        preExistingRegistryEntity.setPhoneNumber("6625550144");
        preExistingRegistryEntity.setRegistryId("42");
        preExistingRegistryEntity.setRequestId("42");
        preExistingRegistryEntity
                .setStartValidity(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        preExistingRegistryEntity.setZipCode("21654");

        RaddRegistryRequestEntity newRegistryRequestEntity = new RaddRegistryRequestEntity();
        newRegistryRequestEntity.setCorrelationId("42");
        newRegistryRequestEntity.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        newRegistryRequestEntity.setCxId("42");
        newRegistryRequestEntity.setError("An error occurred");
        newRegistryRequestEntity.setOriginalRequest("Original Request");
        newRegistryRequestEntity.setPk("Pk");
        newRegistryRequestEntity.setRegistryId("42");
        newRegistryRequestEntity.setRequestId("42");
        newRegistryRequestEntity.setStatus("Status");
        newRegistryRequestEntity.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        newRegistryRequestEntity.setZipCode("21654");

        // Act
        raddRegistryUtils.mergeNewRegistryEntity(preExistingRegistryEntity, newRegistryRequestEntity);

        // Assert
        verify(preExistingRegistryEntity).setCxId(Mockito.any());
        verify(preExistingRegistryEntity).setDescription(Mockito.any());
        verify(preExistingRegistryEntity).setEndValidity(Mockito.any());
        verify(preExistingRegistryEntity).setGeoLocation(Mockito.any());
        verify(preExistingRegistryEntity).setNormalizedAddress(Mockito.any());
        verify(preExistingRegistryEntity).setOpeningTime(Mockito.any());
        verify(preExistingRegistryEntity).setPhoneNumber(Mockito.any());
        verify(preExistingRegistryEntity).setRegistryId(Mockito.any());
        verify(preExistingRegistryEntity).setRequestId(Mockito.any());
        verify(preExistingRegistryEntity).setStartValidity(Mockito.any());
        verify(preExistingRegistryEntity).setZipCode(Mockito.any());
    }


    @Test
    void testConstructRaddRegistryEntity() {
        // Arrange
        PnAddressManagerEvent.NormalizedAddress normalizedAddress = mock(PnAddressManagerEvent.NormalizedAddress.class);
        doNothing().when(normalizedAddress).setAddressRow(Mockito.any());
        doNothing().when(normalizedAddress).setAddressRow2(Mockito.any());
        doNothing().when(normalizedAddress).setCap(Mockito.any());
        doNothing().when(normalizedAddress).setCity(Mockito.any());
        doNothing().when(normalizedAddress).setCity2(Mockito.any());
        doNothing().when(normalizedAddress).setCountry(Mockito.any());
        doNothing().when(normalizedAddress).setNameRow2(Mockito.any());
        doNothing().when(normalizedAddress).setPr(Mockito.any());
        normalizedAddress.setAddressRow("42 Main St");
        normalizedAddress.setAddressRow2("42 Main St");
        normalizedAddress.setCap("Cap");
        normalizedAddress.setCity("Oxford");
        normalizedAddress.setCity2("Oxford");
        normalizedAddress.setCountry("GB");
        normalizedAddress.setNameRow2("Name Row2");
        normalizedAddress.setPr("Pr");

        RaddRegistryRequestEntity registryRequest = new RaddRegistryRequestEntity();
        registryRequest.setCorrelationId("42");
        registryRequest.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        registryRequest.setCxId("42");
        registryRequest.setError("An error occurred");
        registryRequest.setOriginalRequest("{}");
        registryRequest.setPk("Pk");
        registryRequest.setRegistryId("42");
        registryRequest.setRequestId("42");
        registryRequest.setStatus("Status");
        registryRequest.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        registryRequest.setZipCode("21654");

        when(objectMapperUtil.toObject(any(), any())).thenReturn(new RaddRegistryOriginalRequest());

        // Act
        raddRegistryUtils.constructRaddRegistryEntity("registryId", normalizedAddress, registryRequest);

        // Assert
        verify(normalizedAddress).setAddressRow(Mockito.any());
        verify(normalizedAddress).setAddressRow2(Mockito.any());
        verify(normalizedAddress).setCap(Mockito.any());
        verify(normalizedAddress).setCity(Mockito.any());
        verify(normalizedAddress).setCity2(Mockito.any());
        verify(normalizedAddress).setCountry(Mockito.any());
        verify(normalizedAddress).setNameRow2(Mockito.any());
        verify(normalizedAddress).setPr(Mockito.any());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getRelativeItemFromAddressManagerEvent(List, String)}
     */
    @Test
    void testGetRelativeItemFromAddressManagerEvent() {
        // Arrange
        PnAddressManagerEvent.ResultItem resultItem = mock(PnAddressManagerEvent.ResultItem.class);
        when(resultItem.getId()).thenReturn("42");

        ArrayList<PnAddressManagerEvent.ResultItem> resultItems = new ArrayList<>();
        resultItems.add(resultItem);

        // Act
        raddRegistryUtils.getRelativeItemFromAddressManagerEvent(resultItems, "42");

        // Assert
        verify(resultItem).getId();
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getRelativeItemFromAddressManagerEvent(List, String)}
     */
    @Test
    void testGetRelativeItemFromAddressManagerEvent2() {
        // Arrange
        PnAddressManagerEvent.ResultItem resultItem = mock(PnAddressManagerEvent.ResultItem.class);
        when(resultItem.getId()).thenReturn("foo");

        ArrayList<PnAddressManagerEvent.ResultItem> resultItems = new ArrayList<>();
        resultItems.add(resultItem);

        // Act
        raddRegistryUtils.getRelativeItemFromAddressManagerEvent(resultItems, "42");

        // Assert
        verify(resultItem).getId();
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getRelativeItemFromAddressManagerEvent(List, String)}
     */
    @Test
    void testGetRelativeItemFromAddressManagerEvent3() {
        // Arrange
        PnAddressManagerEvent.ResultItem resultItem = mock(PnAddressManagerEvent.ResultItem.class);
        when(resultItem.getId()).thenReturn("42");

        ArrayList<PnAddressManagerEvent.ResultItem> resultItems = new ArrayList<>();
        resultItems.add(resultItem);

        // Act
        raddRegistryUtils.getRelativeItemFromAddressManagerEvent(resultItems, "Id");

        // Assert
        verify(resultItem).getId();
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getPnRaddRegistryImportEntity(String, RegistryUploadRequest, FileCreationResponseDto, String)}
     */
    @Test
    void testGetPnRaddRegistryImportEntity() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Long.longValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getRegistryImportUploadFileTtl()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.getPnRaddRegistryImportEntity(RaddRegistryUtils.java:110)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        pnRaddFsuConfig.setRegistryImportUploadFileTtl(1L);
        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(new ObjectMapper());
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(mock(SecretsManagerClient.class))));
        RegistryUploadRequest request = new RegistryUploadRequest();

        // Act
        RaddRegistryImportEntity actualPnRaddRegistryImportEntity = raddRegistryUtils.getPnRaddRegistryImportEntity("42",
                request, new FileCreationResponseDto(), "42");

        // Assert
        assertEquals("42", actualPnRaddRegistryImportEntity.getCxId());
        assertEquals("42", actualPnRaddRegistryImportEntity.getRequestId());
        assertEquals("TO_PROCESS", actualPnRaddRegistryImportEntity.getStatus());
        assertEquals("{\"defaultEndValidity\":0,\"deleteRole\":null}", actualPnRaddRegistryImportEntity.getConfig());
        assertNull(actualPnRaddRegistryImportEntity.getChecksum());
        assertNull(actualPnRaddRegistryImportEntity.getFileKey());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getPnRaddRegistryImportEntity(String, RegistryUploadRequest, FileCreationResponseDto, String)}
     */
    @Test
    void testGetPnRaddRegistryImportEntity2() throws JsonProcessingException {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Long.longValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getRegistryImportUploadFileTtl()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.getPnRaddRegistryImportEntity(RaddRegistryUtils.java:110)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("42");
        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(objectMapper);

        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        pnRaddFsuConfig.setRegistryImportUploadFileTtl(1L);
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(mock(SecretsManagerClient.class))));
        RegistryUploadRequest request = new RegistryUploadRequest();

        // Act
        RaddRegistryImportEntity actualPnRaddRegistryImportEntity = raddRegistryUtils.getPnRaddRegistryImportEntity("42",
                request, new FileCreationResponseDto(), "42");

        // Assert
        verify(objectMapper).writeValueAsString(Mockito.any());
        assertEquals("42", actualPnRaddRegistryImportEntity.getConfig());
        assertEquals("42", actualPnRaddRegistryImportEntity.getCxId());
        assertEquals("42", actualPnRaddRegistryImportEntity.getRequestId());
        assertEquals("TO_PROCESS", actualPnRaddRegistryImportEntity.getStatus());
        assertNull(actualPnRaddRegistryImportEntity.getChecksum());
        assertNull(actualPnRaddRegistryImportEntity.getFileKey());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getPnRaddRegistryImportEntity(String, RegistryUploadRequest, FileCreationResponseDto, String)}
     */
    @Test
    void testGetPnRaddRegistryImportEntity3() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Long.longValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getRegistryImportUploadFileTtl()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.getPnRaddRegistryImportEntity(RaddRegistryUtils.java:110)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        ObjectMapperUtil objectMapperUtil = mock(ObjectMapperUtil.class);
        when(objectMapperUtil.toJson(Mockito.any())).thenReturn("Json");

        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        pnRaddFsuConfig.setRegistryImportUploadFileTtl(1L);
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(mock(SecretsManagerClient.class))));
        RegistryUploadRequest request = new RegistryUploadRequest();

        // Act
        RaddRegistryImportEntity actualPnRaddRegistryImportEntity = raddRegistryUtils.getPnRaddRegistryImportEntity("42",
                request, new FileCreationResponseDto(), "42");

        // Assert
        verify(objectMapperUtil).toJson(Mockito.any());
        assertEquals("42", actualPnRaddRegistryImportEntity.getCxId());
        assertEquals("42", actualPnRaddRegistryImportEntity.getRequestId());
        assertEquals("Json", actualPnRaddRegistryImportEntity.getConfig());
        assertEquals("TO_PROCESS", actualPnRaddRegistryImportEntity.getStatus());
        assertNull(actualPnRaddRegistryImportEntity.getChecksum());
        assertNull(actualPnRaddRegistryImportEntity.getFileKey());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#getFileCreationRequestDto()}
     */
    @Test
    void testGetFileCreationRequestDto() {
        // Arrange and Act
        FileCreationRequestDto actualFileCreationRequestDto = raddRegistryUtils.getFileCreationRequestDto();

        // Assert
        assertEquals(Const.CONTENT_TYPE_TEXT_CSV, actualFileCreationRequestDto.getContentType());
        assertEquals(Const.SAVED, actualFileCreationRequestDto.getStatus());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getRequestAddressFromOriginalRequest(List)}
     */
    @Test
    void testGetRequestAddressFromOriginalRequest() {
        // Arrange, Act and Assert
        assertTrue(raddRegistryUtils.getRequestAddressFromOriginalRequest(new ArrayList<>()).isEmpty());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getRequestAddressFromOriginalRequest(List)}
     */
    @Test
    void testGetRequestAddressFromOriginalRequest2() {
        // Arrange
        AddressManagerRequestAddress addressManagerRequestAddress = new AddressManagerRequestAddress();
        addressManagerRequestAddress.setAddressRow("42 Main St");
        addressManagerRequestAddress.setCap("Cap");
        addressManagerRequestAddress.setCity("Oxford");
        addressManagerRequestAddress.setCountry("GB");
        addressManagerRequestAddress.setId("42");
        addressManagerRequestAddress.setPr("Pr");
        when(objectMapperUtil.toObject(Mockito.any(), Mockito.<Class<AddressManagerRequestAddress>>any()))
                .thenReturn(addressManagerRequestAddress);

        RaddRegistryRequestEntity raddRegistryRequestEntity = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity.setCorrelationId("42");
        raddRegistryRequestEntity.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setCxId("42");
        raddRegistryRequestEntity.setError("An error occurred");
        raddRegistryRequestEntity.setOriginalRequest("Original Request");
        raddRegistryRequestEntity.setPk("Pk");
        raddRegistryRequestEntity.setRegistryId("42");
        raddRegistryRequestEntity.setRequestId("42");
        raddRegistryRequestEntity.setStatus("Status");
        raddRegistryRequestEntity.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setZipCode("21654");

        ArrayList<RaddRegistryRequestEntity> entities = new ArrayList<>();
        entities.add(raddRegistryRequestEntity);

        // Act
        List<AddressManagerRequestAddress> actualRequestAddressFromOriginalRequest = raddRegistryUtils
                .getRequestAddressFromOriginalRequest(entities);

        // Assert
        verify(objectMapperUtil).toObject(Mockito.any(), Mockito.<Class<AddressManagerRequestAddress>>any());
        assertEquals(1, actualRequestAddressFromOriginalRequest.size());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getRequestAddressFromOriginalRequest(List)}
     */
    @Test
    void testGetRequestAddressFromOriginalRequest3() {
        // Arrange
        AddressManagerRequestAddress addressManagerRequestAddress = new AddressManagerRequestAddress();
        addressManagerRequestAddress.setAddressRow("42 Main St");
        addressManagerRequestAddress.setCap("Cap");
        addressManagerRequestAddress.setCity("Oxford");
        addressManagerRequestAddress.setCountry("GB");
        addressManagerRequestAddress.setId("42");
        addressManagerRequestAddress.setPr("Pr");
        when(objectMapperUtil.toObject(Mockito.any(), Mockito.<Class<AddressManagerRequestAddress>>any()))
                .thenReturn(addressManagerRequestAddress);

        RaddRegistryRequestEntity raddRegistryRequestEntity = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity.setCorrelationId("42");
        raddRegistryRequestEntity.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setCxId("42");
        raddRegistryRequestEntity.setError("An error occurred");
        raddRegistryRequestEntity.setOriginalRequest("Original Request");
        raddRegistryRequestEntity.setPk("Pk");
        raddRegistryRequestEntity.setRegistryId("42");
        raddRegistryRequestEntity.setRequestId("42");
        raddRegistryRequestEntity.setStatus("Status");
        raddRegistryRequestEntity.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setZipCode("21654");

        RaddRegistryRequestEntity raddRegistryRequestEntity2 = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity2.setCorrelationId("Correlation Id");
        raddRegistryRequestEntity2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity2.setCxId("Cx Id");
        raddRegistryRequestEntity2.setError("Error");
        raddRegistryRequestEntity2.setOriginalRequest("it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity");
        raddRegistryRequestEntity2.setPk("it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity");
        raddRegistryRequestEntity2.setRegistryId("Registry Id");
        raddRegistryRequestEntity2.setRequestId("Request Id");
        raddRegistryRequestEntity2.setStatus("it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity");
        raddRegistryRequestEntity2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity2.setZipCode("OX1 1PT");

        ArrayList<RaddRegistryRequestEntity> entities = new ArrayList<>();
        entities.add(raddRegistryRequestEntity2);
        entities.add(raddRegistryRequestEntity);

        // Act
        List<AddressManagerRequestAddress> actualRequestAddressFromOriginalRequest = raddRegistryUtils
                .getRequestAddressFromOriginalRequest(entities);

        // Assert
        verify(objectMapperUtil, atLeast(1)).toObject(Mockito.any(),
                Mockito.<Class<AddressManagerRequestAddress>>any());
        assertEquals(2, actualRequestAddressFromOriginalRequest.size());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getRequestAddressFromOriginalRequest(List)}
     */
    @Test
    void testGetRequestAddressFromOriginalRequest4() {
        // Arrange
        when(objectMapperUtil.toObject(Mockito.any(), Mockito.<Class<AddressManagerRequestAddress>>any()))
                .thenThrow(new RuntimeException("foo"));

        RaddRegistryRequestEntity raddRegistryRequestEntity = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity.setCorrelationId("42");
        raddRegistryRequestEntity.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setCxId("42");
        raddRegistryRequestEntity.setError("An error occurred");
        raddRegistryRequestEntity.setOriginalRequest("Original Request");
        raddRegistryRequestEntity.setPk("Pk");
        raddRegistryRequestEntity.setRegistryId("42");
        raddRegistryRequestEntity.setRequestId("42");
        raddRegistryRequestEntity.setStatus("Status");
        raddRegistryRequestEntity.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setZipCode("21654");

        ArrayList<RaddRegistryRequestEntity> entities = new ArrayList<>();
        entities.add(raddRegistryRequestEntity);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> raddRegistryUtils.getRequestAddressFromOriginalRequest(entities));
        verify(objectMapperUtil).toObject(Mockito.any(), Mockito.<Class<AddressManagerRequestAddress>>any());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getNormalizeRequestDtoFromAddressManagerRequest(AddressManagerRequest)}
     */
    @Test
    void testGetNormalizeRequestDtoFromAddressManagerRequest() {
        // Arrange
        AddressManagerRequest request = new AddressManagerRequest();
        request.setAddresses(new ArrayList<>());
        request.setCorrelationId("42");

        // Act
        NormalizeItemsRequestDto actualNormalizeRequestDtoFromAddressManagerRequest = raddRegistryUtils
                .getNormalizeRequestDtoFromAddressManagerRequest(request);

        // Assert
        assertEquals("42", actualNormalizeRequestDtoFromAddressManagerRequest.getCorrelationId());
        assertTrue(actualNormalizeRequestDtoFromAddressManagerRequest.getRequestItems().isEmpty());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getNormalizeRequestDtoFromAddressManagerRequest(AddressManagerRequest)}
     */
    @Test
    void testGetNormalizeRequestDtoFromAddressManagerRequest2() {
        // Arrange
        AddressManagerRequest request = mock(AddressManagerRequest.class);
        when(request.getCorrelationId()).thenReturn("42");
        when(request.getAddresses()).thenReturn(new ArrayList<>());
        doNothing().when(request).setAddresses(Mockito.any());
        doNothing().when(request).setCorrelationId(Mockito.any());
        request.setAddresses(new ArrayList<>());
        request.setCorrelationId("42");

        // Act
        NormalizeItemsRequestDto actualNormalizeRequestDtoFromAddressManagerRequest = raddRegistryUtils
                .getNormalizeRequestDtoFromAddressManagerRequest(request);

        // Assert
        verify(request).getAddresses();
        verify(request).getCorrelationId();
        verify(request).setAddresses(Mockito.any());
        verify(request).setCorrelationId(Mockito.any());
        assertEquals("42", actualNormalizeRequestDtoFromAddressManagerRequest.getCorrelationId());
        assertTrue(actualNormalizeRequestDtoFromAddressManagerRequest.getRequestItems().isEmpty());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getNormalizeRequestDtoFromAddressManagerRequest(AddressManagerRequest)}
     */
    @Test
    void testGetNormalizeRequestDtoFromAddressManagerRequest3() {
        // Arrange
        AddressManagerRequestAddress addressManagerRequestAddress = new AddressManagerRequestAddress();
        addressManagerRequestAddress.setAddressRow("42 Main St");
        addressManagerRequestAddress.setCap("Cap");
        addressManagerRequestAddress.setCity("Oxford");
        addressManagerRequestAddress.setCountry("GB");
        addressManagerRequestAddress.setId("42");
        addressManagerRequestAddress.setPr("Pr");

        ArrayList<AddressManagerRequestAddress> addressManagerRequestAddressList = new ArrayList<>();
        addressManagerRequestAddressList.add(addressManagerRequestAddress);
        AddressManagerRequest request = mock(AddressManagerRequest.class);
        when(request.getCorrelationId()).thenReturn("42");
        when(request.getAddresses()).thenReturn(addressManagerRequestAddressList);
        doNothing().when(request).setAddresses(Mockito.any());
        doNothing().when(request).setCorrelationId(Mockito.any());
        request.setAddresses(new ArrayList<>());
        request.setCorrelationId("42");

        // Act
        NormalizeItemsRequestDto actualNormalizeRequestDtoFromAddressManagerRequest = raddRegistryUtils
                .getNormalizeRequestDtoFromAddressManagerRequest(request);

        // Assert
        verify(request).getAddresses();
        verify(request).getCorrelationId();
        verify(request).setAddresses(Mockito.any());
        verify(request).setCorrelationId(Mockito.any());
        List<NormalizeRequestDto> requestItems = actualNormalizeRequestDtoFromAddressManagerRequest.getRequestItems();
        assertEquals(1, requestItems.size());
        NormalizeRequestDto getResult = requestItems.get(0);
        AnalogAddressDto address = getResult.getAddress();
        assertEquals("42 Main St", address.getAddressRow());
        assertEquals("42", actualNormalizeRequestDtoFromAddressManagerRequest.getCorrelationId());
        assertEquals("42", getResult.getId());
        assertEquals("Cap", address.getCap());
        assertEquals("GB", address.getCountry());
        assertEquals("Oxford", address.getCity());
        assertEquals("Pr", address.getPr());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#getNormalizeRequestDtoFromAddressManagerRequest(AddressManagerRequest)}
     */
    @Test
    void testGetNormalizeRequestDtoFromAddressManagerRequest4() {
        // Arrange
        AddressManagerRequestAddress addressManagerRequestAddress = new AddressManagerRequestAddress();
        addressManagerRequestAddress.setAddressRow("42 Main St");
        addressManagerRequestAddress.setCap("Cap");
        addressManagerRequestAddress.setCity("Oxford");
        addressManagerRequestAddress.setCountry("GB");
        addressManagerRequestAddress.setId("42");
        addressManagerRequestAddress.setPr("Pr");

        AddressManagerRequestAddress addressManagerRequestAddress2 = new AddressManagerRequestAddress();
        addressManagerRequestAddress2.setAddressRow("17 High St");
        addressManagerRequestAddress2.setCap("it.pagopa.pn.radd.pojo.AddressManagerRequestAddress");
        addressManagerRequestAddress2.setCity("London");
        addressManagerRequestAddress2.setCountry("GBR");
        addressManagerRequestAddress2.setId("Id");
        addressManagerRequestAddress2.setPr("it.pagopa.pn.radd.pojo.AddressManagerRequestAddress");

        ArrayList<AddressManagerRequestAddress> addressManagerRequestAddressList = new ArrayList<>();
        addressManagerRequestAddressList.add(addressManagerRequestAddress2);
        addressManagerRequestAddressList.add(addressManagerRequestAddress);
        AddressManagerRequest request = mock(AddressManagerRequest.class);
        when(request.getCorrelationId()).thenReturn("42");
        when(request.getAddresses()).thenReturn(addressManagerRequestAddressList);
        doNothing().when(request).setAddresses(Mockito.any());
        doNothing().when(request).setCorrelationId(Mockito.any());
        request.setAddresses(new ArrayList<>());
        request.setCorrelationId("42");

        // Act
        NormalizeItemsRequestDto actualNormalizeRequestDtoFromAddressManagerRequest = raddRegistryUtils
                .getNormalizeRequestDtoFromAddressManagerRequest(request);

        // Assert
        verify(request).getAddresses();
        verify(request).getCorrelationId();
        verify(request).setAddresses(Mockito.any());
        verify(request).setCorrelationId(Mockito.any());
        List<NormalizeRequestDto> requestItems = actualNormalizeRequestDtoFromAddressManagerRequest.getRequestItems();
        assertEquals(2, requestItems.size());
        NormalizeRequestDto getResult = requestItems.get(0);
        AnalogAddressDto address = getResult.getAddress();
        assertEquals("17 High St", address.getAddressRow());
        NormalizeRequestDto getResult2 = requestItems.get(1);
        AnalogAddressDto address2 = getResult2.getAddress();
        assertEquals("42 Main St", address2.getAddressRow());
        assertEquals("42", actualNormalizeRequestDtoFromAddressManagerRequest.getCorrelationId());
        assertEquals("42", getResult2.getId());
        assertEquals("Cap", address2.getCap());
        assertEquals("GB", address2.getCountry());
        assertEquals("GBR", address.getCountry());
        assertEquals("Id", getResult.getId());
        assertEquals("London", address.getCity());
        assertEquals("Oxford", address2.getCity());
        assertEquals("Pr", address2.getPr());
        assertEquals("it.pagopa.pn.radd.pojo.AddressManagerRequestAddress", address.getCap());
        assertEquals("it.pagopa.pn.radd.pojo.AddressManagerRequestAddress", address.getPr());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#retrieveAddressManagerApiKey()}
     */
    @Test
    void testRetrieveAddressManagerApiKey() {
        // Arrange
        when(secretService.getSecret(Mockito.any())).thenReturn("Secret");

        // Act
        String actualRetrieveAddressManagerApiKeyResult = raddRegistryUtils.retrieveAddressManagerApiKey();

        // Assert
        verify(secretService).getSecret(Mockito.any());
        assertEquals("Secret", actualRetrieveAddressManagerApiKeyResult);
    }

    /**
     * Method under test: {@link RaddRegistryUtils#retrieveAddressManagerApiKey()}
     */
    @Test
    void testRetrieveAddressManagerApiKey2() {
        // Arrange
        when(secretService.getSecret(Mockito.any())).thenThrow(new RuntimeException("foo"));

        // Act and Assert
        assertThrows(RuntimeException.class, () -> raddRegistryUtils.retrieveAddressManagerApiKey());
        verify(secretService).getSecret(Mockito.any());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#mapToEventMessage(Set, String)}
     */
    @Test
    void testMapToEventMessage() {
        // Arrange, Act and Assert
        PnAttachmentsConfigEventPayload detail = raddRegistryUtils.mapToEventMessage(new HashSet<>(), "21654").getDetail();
        assertEquals("21654", detail.getConfigKey());
        assertNull(detail.getConfigType());
        assertTrue(detail.getConfigs().isEmpty());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#mapToEventMessage(Set, String)}
     */
    @Test
    void testMapToEventMessage2() {
        // Arrange
        Set<TimeInterval> timeIntervals = new HashSet<>();
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        timeIntervals
                .add(new TimeInterval(start, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));

        // Act and Assert
        PnAttachmentsConfigEventPayload detail = raddRegistryUtils.mapToEventMessage(timeIntervals, "21654").getDetail();
        assertEquals("21654", detail.getConfigKey());
        assertNull(detail.getConfigType());
        List<PnAttachmentsConfigEventItem> configs = detail.getConfigs();
        assertEquals(1, configs.size());
        PnAttachmentsConfigEventItem getResult = configs.get(0);
        Instant expectedStartValidity = getResult.getEndValidity();
        assertSame(expectedStartValidity, getResult.getStartValidity());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#mapToEventMessage(Set, String)}
     */
    @Test
    void testMapToEventMessage3() {
        // Arrange
        Set<TimeInterval> timeIntervals = new HashSet<>();
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        timeIntervals
                .add(new TimeInterval(start, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        Instant start2 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        timeIntervals
                .add(new TimeInterval(start2, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));

        // Act and Assert
        PnAttachmentsConfigEventPayload detail = raddRegistryUtils.mapToEventMessage(timeIntervals, "21654").getDetail();
        assertEquals("21654", detail.getConfigKey());
        assertNull(detail.getConfigType());
        List<PnAttachmentsConfigEventItem> configs = detail.getConfigs();
        assertEquals(1, configs.size());
        PnAttachmentsConfigEventItem getResult = configs.get(0);
        Instant startValidity = getResult.getStartValidity();
        assertSame(startValidity, getResult.getEndValidity());
        assertSame(startValidity, configs.get(0).getEndValidity());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#mapToEventMessage(Set, String)}
     */
    @Test
    void testMapToEventMessage4() {
        // Arrange
        TimeInterval timeInterval = mock(TimeInterval.class);
        when(timeInterval.getEnd()).thenReturn(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        when(timeInterval.getStart())
                .thenReturn(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        Set<TimeInterval> timeIntervals = new HashSet<>();
        timeIntervals.add(timeInterval);

        // Act
        PnEvaluatedZipCodeEvent actualMapToEventMessageResult = raddRegistryUtils.mapToEventMessage(timeIntervals, "21654");

        // Assert
        verify(timeInterval, atLeast(1)).getEnd();
        verify(timeInterval).getStart();
        PnAttachmentsConfigEventPayload detail = actualMapToEventMessageResult.getDetail();
        assertEquals("21654", detail.getConfigKey());
        assertNull(detail.getConfigType());
        List<PnAttachmentsConfigEventItem> configs = detail.getConfigs();
        assertEquals(1, configs.size());
        PnAttachmentsConfigEventItem getResult = configs.get(0);
        Instant expectedStartValidity = getResult.getEndValidity();
        assertSame(expectedStartValidity, getResult.getStartValidity());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#getOfficeIntervals(List)}
     */
    @Test
    void testGetOfficeIntervals() {
        // Arrange, Act and Assert
        assertTrue(raddRegistryUtils.getOfficeIntervals(new ArrayList<>()).isEmpty());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#getOfficeIntervals(List)}
     */
    @Test
    void testGetOfficeIntervals2() {
        // Arrange
        RaddRegistryEntity raddRegistryEntity = new RaddRegistryEntity();
        raddRegistryEntity.setCxId("42");
        raddRegistryEntity.setDescription("The characteristics of someone or something");
        raddRegistryEntity.setEndValidity(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryEntity.setGeoLocation("Geo Location");
        NormalizedAddressEntity addressEntity = new NormalizedAddressEntity();
        addressEntity.setCountry("country");
        addressEntity.setProvince("pr");
        addressEntity.setCity("city");
        addressEntity.setCap("cap");
        raddRegistryEntity.setNormalizedAddress(addressEntity);
        raddRegistryEntity.setOpeningTime("Opening Time");
        raddRegistryEntity.setPhoneNumber("6625550144");
        raddRegistryEntity.setRegistryId("42");
        raddRegistryEntity.setRequestId("42");
        raddRegistryEntity.setStartValidity(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryEntity.setZipCode("21654");

        ArrayList<RaddRegistryEntity> raddRegistryEntities = new ArrayList<>();
        raddRegistryEntities.add(raddRegistryEntity);

        // Act
        List<TimeInterval> actualOfficeIntervals = raddRegistryUtils.getOfficeIntervals(raddRegistryEntities);

        // Assert
        assertEquals(1, actualOfficeIntervals.size());
        TimeInterval getResult = actualOfficeIntervals.get(0);
        Instant expectedStart = getResult.getEnd();
        assertSame(expectedStart, getResult.getStart());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#getOfficeIntervals(List)}
     */
    @Test
    void testGetOfficeIntervals3() {
        // Arrange
        RaddRegistryEntity raddRegistryEntity = new RaddRegistryEntity();
        raddRegistryEntity.setCxId("42");
        raddRegistryEntity.setDescription("The characteristics of someone or something");
        raddRegistryEntity.setEndValidity(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryEntity.setGeoLocation("Geo Location");
        NormalizedAddressEntity addressEntity = new NormalizedAddressEntity();
        addressEntity.setCountry("country");
        addressEntity.setProvince("pr");
        addressEntity.setCity("city");
        addressEntity.setCap("cap");
        raddRegistryEntity.setNormalizedAddress(addressEntity);
        raddRegistryEntity.setOpeningTime("Opening Time");
        raddRegistryEntity.setPhoneNumber("6625550144");
        raddRegistryEntity.setRegistryId("42");
        raddRegistryEntity.setRequestId("42");
        raddRegistryEntity.setStartValidity(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryEntity.setZipCode("21654");

        RaddRegistryEntity raddRegistryEntity2 = new RaddRegistryEntity();
        raddRegistryEntity2.setCxId("Cx Id");
        raddRegistryEntity2.setDescription("Description");
        raddRegistryEntity2.setEndValidity(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryEntity2.setGeoLocation("it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity");
        NormalizedAddressEntity addressEntity2 = new NormalizedAddressEntity();
        addressEntity.setCountry("country2");
        addressEntity.setProvince("pr2");
        addressEntity.setCity("city2");
        addressEntity.setCap("cap2");
        raddRegistryEntity2.setNormalizedAddress(addressEntity2);
        raddRegistryEntity2.setOpeningTime("it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity");
        raddRegistryEntity2.setPhoneNumber("8605550118");
        raddRegistryEntity2.setRegistryId("Registry Id");
        raddRegistryEntity2.setRequestId("Request Id");
        raddRegistryEntity2.setStartValidity(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryEntity2.setZipCode("OX1 1PT");

        ArrayList<RaddRegistryEntity> raddRegistryEntities = new ArrayList<>();
        raddRegistryEntities.add(raddRegistryEntity2);
        raddRegistryEntities.add(raddRegistryEntity);

        // Act
        List<TimeInterval> actualOfficeIntervals = raddRegistryUtils.getOfficeIntervals(raddRegistryEntities);

        // Assert
        assertEquals(2, actualOfficeIntervals.size());
        TimeInterval getResult = actualOfficeIntervals.get(1);
        Instant start = getResult.getStart();
        assertSame(start, actualOfficeIntervals.get(0).getEnd());
        assertSame(start, getResult.getEnd());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#getOfficeIntervals(List)}
     */
    @Test
    void testGetOfficeIntervals4() {
        // Arrange
        RaddRegistryEntity raddRegistryEntity = mock(RaddRegistryEntity.class);
        when(raddRegistryEntity.getEndValidity())
                .thenReturn(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        when(raddRegistryEntity.getStartValidity())
                .thenReturn(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        doNothing().when(raddRegistryEntity).setCxId(Mockito.any());
        doNothing().when(raddRegistryEntity).setDescription(Mockito.any());
        doNothing().when(raddRegistryEntity).setEndValidity(Mockito.any());
        doNothing().when(raddRegistryEntity).setGeoLocation(Mockito.any());
        doNothing().when(raddRegistryEntity).setNormalizedAddress(Mockito.any());
        doNothing().when(raddRegistryEntity).setOpeningTime(Mockito.any());
        doNothing().when(raddRegistryEntity).setPhoneNumber(Mockito.any());
        doNothing().when(raddRegistryEntity).setRegistryId(Mockito.any());
        doNothing().when(raddRegistryEntity).setRequestId(Mockito.any());
        doNothing().when(raddRegistryEntity).setStartValidity(Mockito.any());
        doNothing().when(raddRegistryEntity).setZipCode(Mockito.any());
        raddRegistryEntity.setCxId("42");
        raddRegistryEntity.setDescription("The characteristics of someone or something");
        raddRegistryEntity.setEndValidity(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryEntity.setGeoLocation("Geo Location");
        NormalizedAddressEntity addressEntity = new NormalizedAddressEntity();
        addressEntity.setCountry("country");
        addressEntity.setProvince("pr");
        addressEntity.setCity("city");
        addressEntity.setCap("cap");
        raddRegistryEntity.setNormalizedAddress(addressEntity);
        raddRegistryEntity.setOpeningTime("Opening Time");
        raddRegistryEntity.setPhoneNumber("6625550144");
        raddRegistryEntity.setRegistryId("42");
        raddRegistryEntity.setRequestId("42");
        raddRegistryEntity.setStartValidity(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryEntity.setZipCode("21654");

        ArrayList<RaddRegistryEntity> raddRegistryEntities = new ArrayList<>();
        raddRegistryEntities.add(raddRegistryEntity);

        // Act
        List<TimeInterval> actualOfficeIntervals = raddRegistryUtils.getOfficeIntervals(raddRegistryEntities);

        // Assert
        verify(raddRegistryEntity, atLeast(1)).getEndValidity();
        verify(raddRegistryEntity).getStartValidity();
        verify(raddRegistryEntity).setCxId(Mockito.any());
        verify(raddRegistryEntity).setDescription(Mockito.any());
        verify(raddRegistryEntity).setEndValidity(Mockito.any());
        verify(raddRegistryEntity).setGeoLocation(Mockito.any());
        verify(raddRegistryEntity).setNormalizedAddress(Mockito.any());
        verify(raddRegistryEntity).setOpeningTime(Mockito.any());
        verify(raddRegistryEntity).setPhoneNumber(Mockito.any());
        verify(raddRegistryEntity).setRegistryId(Mockito.any());
        verify(raddRegistryEntity).setRequestId(Mockito.any());
        verify(raddRegistryEntity).setStartValidity(Mockito.any());
        verify(raddRegistryEntity).setZipCode(Mockito.any());
        assertEquals(1, actualOfficeIntervals.size());
        TimeInterval getResult = actualOfficeIntervals.get(0);
        Instant expectedStart = getResult.getEnd();
        assertSame(expectedStart, getResult.getStart());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#findActiveIntervals(List)}
     */
    @Test
    void testFindActiveIntervals() {
        // Arrange
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        pnRaddFsuConfig.setEvaluatedZipCodeConfigNumber(10);
        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(new ObjectMapper());
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(mock(SecretsManagerClient.class))));

        // Act and Assert
        assertTrue(raddRegistryUtils.findActiveIntervals(new ArrayList<>()).isEmpty());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#findActiveIntervals(List)}
     */
    @Test
    void testFindActiveIntervals2() {
        // Arrange
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        pnRaddFsuConfig.setEvaluatedZipCodeConfigNumber(1);
        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(new ObjectMapper());
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(mock(SecretsManagerClient.class))));

        ArrayList<TimeInterval> timeIntervals = new ArrayList<>();
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        timeIntervals
                .add(new TimeInterval(start, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));

        // Act
        Set<TimeInterval> actualFindActiveIntervalsResult = raddRegistryUtils.findActiveIntervals(timeIntervals);

        // Assert
        assertEquals(1, actualFindActiveIntervalsResult.size());
        TimeInterval getResult = actualFindActiveIntervalsResult.iterator().next();
        Instant expectedStart = getResult.getStart();
        Instant startOfToday = LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        assertEquals(expectedStart, startOfToday);
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#combinations(TimeInterval[], List, Set, int, int)}
     */
    @Test
    void testCombinations() {
        // Arrange
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();

        ArrayList<TimeInterval> current = new ArrayList<>();
        Instant start2 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        current.add(new TimeInterval(start2, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        Instant start3 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        current.add(new TimeInterval(start3, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        Instant start4 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        current.add(new TimeInterval(start4, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        HashSet<Set<TimeInterval>> accumulator = new HashSet<>();

        // Act
        combinations(
                new TimeInterval[]{
                        new TimeInterval(start, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant())},
                current, accumulator, 3, 2);

        // Assert
        assertEquals(1, accumulator.size());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#combinations(TimeInterval[], List, Set, int, int)}
     */
    @Test
    void testCombinations2() {
        // Arrange
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        TimeInterval timeInterval = new TimeInterval(start,
                LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        Instant start2 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        TimeInterval timeInterval2 = new TimeInterval(start2,
                LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        Instant start3 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();

        ArrayList<TimeInterval> current = new ArrayList<>();
        Instant start4 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        current.add(new TimeInterval(start4, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        Instant start5 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        current.add(new TimeInterval(start5, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        HashSet<Set<TimeInterval>> accumulator = new HashSet<>();

        // Act
        combinations(
                new TimeInterval[]{timeInterval, timeInterval2,
                        new TimeInterval(start3, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant())},
                current, accumulator, 3, 2);

        // Assert
        assertEquals(1, accumulator.size());
        assertEquals(2, current.size());
    }

    /**
     * Method under test:
     * {@link RaddRegistryUtils#combinations(TimeInterval[], List, Set, int, int)}
     */
    @Test
    void testCombinations3() {
        // Arrange
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();

        ArrayList<TimeInterval> current = new ArrayList<>();
        Instant start2 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        current.add(new TimeInterval(start2, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        Instant start3 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        current.add(new TimeInterval(start3, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        Instant start4 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        current.add(new TimeInterval(start4, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));

        HashSet<Set<TimeInterval>> accumulator = new HashSet<>();
        accumulator.add(new HashSet<>());

        // Act
        combinations(
                new TimeInterval[]{
                        new TimeInterval(start, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant())},
                current, accumulator, 3, 2);

        // Assert
        assertEquals(2, accumulator.size());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#findIntersection(List)}
     */
    @Test
    void testFindIntersection() {
        // Arrange
        ArrayList<TimeInterval> intervals = new ArrayList<>();
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        intervals.add(new TimeInterval(start, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));

        // Act
        TimeInterval actualFindIntersectionResult = findIntersection(intervals);

        // Assert
        Instant expectedStart = actualFindIntersectionResult.getEnd();
        assertSame(expectedStart, actualFindIntersectionResult.getStart());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#findIntersection(List)}
     */
    @Test
    void testFindIntersection2() {
        // Arrange
        ArrayList<TimeInterval> intervals = new ArrayList<>();
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        intervals.add(new TimeInterval(start, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        Instant start2 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        intervals.add(new TimeInterval(start2, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));

        // Act
        TimeInterval actualFindIntersectionResult = findIntersection(intervals);

        // Assert
        Instant expectedStart = actualFindIntersectionResult.getEnd();
        assertSame(expectedStart, actualFindIntersectionResult.getStart());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#findIntersection(List)}
     */
    @Test
    void testFindIntersection3() {
        // Arrange
        TimeInterval timeInterval = mock(TimeInterval.class);
        when(timeInterval.getEnd()).thenReturn(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        when(timeInterval.getStart())
                .thenReturn(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        ArrayList<TimeInterval> intervals = new ArrayList<>();
        intervals.add(timeInterval);

        // Act
        TimeInterval actualFindIntersectionResult = findIntersection(intervals);

        // Assert
        verify(timeInterval).getEnd();
        verify(timeInterval).getStart();
        Instant expectedStart = actualFindIntersectionResult.getEnd();
        assertSame(expectedStart, actualFindIntersectionResult.getStart());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#findIntersection(List)}
     */
    @Test
    void testFindIntersection4() {
        // Arrange
        TimeInterval timeInterval = mock(TimeInterval.class);
        when(timeInterval.getStart()).thenThrow(new RuntimeException("foo"));

        ArrayList<TimeInterval> intervals = new ArrayList<>();
        Instant start = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        intervals.add(new TimeInterval(start, LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        intervals.add(timeInterval);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> findIntersection(intervals));
        verify(timeInterval).getStart();
    }

    /**
     * Method under test: {@link RaddRegistryUtils#findIntersection(List)}
     */
    @Test
    void testFindIntersection5() {
        // Arrange
        TimeInterval timeInterval = mock(TimeInterval.class);
        when(timeInterval.getEnd()).thenReturn(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        when(timeInterval.getStart())
                .thenReturn(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        TimeInterval timeInterval2 = mock(TimeInterval.class);
        when(timeInterval2.getEnd()).thenReturn(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        when(timeInterval2.getStart())
                .thenReturn(LocalDate.ofYearDay(1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        ArrayList<TimeInterval> intervals = new ArrayList<>();
        intervals.add(timeInterval2);
        intervals.add(timeInterval);

        // Act
        TimeInterval actualFindIntersectionResult = findIntersection(intervals);

        // Assert
        verify(timeInterval2).getEnd();
        verify(timeInterval, atLeast(1)).getEnd();
        verify(timeInterval2).getStart();
        verify(timeInterval, atLeast(1)).getStart();
        Instant expectedStart = actualFindIntersectionResult.getEnd();
        assertSame(expectedStart, actualFindIntersectionResult.getStart());
    }

    @Test
    void findIntervalsTest() {
        List<TimeInterval> timeIntervals = new ArrayList<>();

        timeIntervals.add(new TimeInterval(Instant.parse("2100-05-01T00:00:00Z"), Instant.parse("2100-05-08T00:00:00Z")));
        timeIntervals.add(new TimeInterval(Instant.parse("2100-05-06T00:00:00Z"), Instant.parse("2100-05-12T00:00:00Z"))); // Intersects with first interval
        timeIntervals.add(new TimeInterval(Instant.parse("2100-05-15T00:00:00Z"), Instant.parse("2100-05-22T00:00:00Z")));
        timeIntervals.add(new TimeInterval(Instant.parse("2100-05-18T00:00:00Z"), Instant.parse("2100-05-25T00:00:00Z"))); // Intersects with third interval
        TimeInterval[] timeIntervalArray = timeIntervals.toArray(new TimeInterval[0]);

        Set<Set<TimeInterval>> result = new HashSet<>();
        combinations(timeIntervalArray, new ArrayList<>(), result, 1, 0);

        List<TimeInterval> activeIntervals = new ArrayList<>();

        for (Set<TimeInterval> intervalSet : result) {
            TimeInterval timeInterval = findIntersection(intervalSet.stream().toList());
            if (timeInterval != null) {
                activeIntervals.add(timeInterval);
            }
        }


        Instant instant1Start = Instant.parse("2100-05-15T00:00:00Z");
        Instant instant1End = Instant.parse("2100-05-25T00:00:00Z");
        Instant instant2Start = Instant.parse("2100-05-01T00:00:00Z");
        Instant instant2End = Instant.parse("2100-05-12T00:00:00Z");

        // Create TimeInterval objects from the Instant objects
        TimeInterval interval1 = new TimeInterval(instant1Start, instant1End);
        TimeInterval interval2 = new TimeInterval(instant2Start, instant2End);

        Set<TimeInterval> verify = Set.of(interval1, interval2);
        TimeInterval[] timeIntervals1 = new TimeInterval[0];

        Assertions.assertEquals(verify, mergeIntervals(activeIntervals.toArray(timeIntervals1)));
    }

    @Test
    void shouldMapToRequestResponseWhenResultsPageIsNotNull() {
        // Given
        ResultPaginationDto<RaddRegistryRequestEntity, String> resultPaginationDto = new ResultPaginationDto<>();
        List<RaddRegistryRequestEntity> resultsPage = new ArrayList<>();
        RaddRegistryRequestEntity raddRegistryRequestEntity = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity.setRegistryId("testRegistryId");
        raddRegistryRequestEntity.setRequestId("testRequestId");
        raddRegistryRequestEntity.setError("testError");
        raddRegistryRequestEntity.setCreatedAt(Instant.now());
        raddRegistryRequestEntity.setUpdatedAt(Instant.now());
        raddRegistryRequestEntity.setStatus("testStatus");
        raddRegistryRequestEntity.setOriginalRequest("{\"addressRow\": \"testAddressRow\", \"cap\": \"testCap\", \"city\": \"testCity\", \"pr\": \"testPr\", \"country\": \"testCountry\", \"startValidity\": \"2024-01-01T00:00:00.000Z\"}");
        resultsPage.add(raddRegistryRequestEntity);
        resultPaginationDto.setResultsPage(resultsPage);
        resultPaginationDto.setNextPagesKey(Collections.emptyList());
        resultPaginationDto.setMoreResult(true);

        RaddRegistryOriginalRequest originalRequest = new RaddRegistryOriginalRequest();
        originalRequest.setAddressRow("testAddressRow");
        originalRequest.setCap("testCap");
        originalRequest.setCity("testCity");
        originalRequest.setPr("testPr");

        when(objectMapperUtil.toObject(Mockito.anyString(), Mockito.any())).thenReturn(originalRequest);
        // When
        RequestResponse result = raddRegistryUtils.mapToRequestResponse(resultPaginationDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("testRegistryId", result.getItems().get(0).getRegistryId());
        assertEquals("testRequestId", result.getItems().get(0).getRequestId());
        assertEquals("testError", result.getItems().get(0).getError());
        assertEquals("testStatus", result.getItems().get(0).getStatus());
        assertEquals("testAddressRow", result.getItems().get(0).getOriginalRequest().getOriginalAddress().getAddressRow());
        assertEquals(0, result.getNextPagesKey().size());
        assertTrue(result.getMoreResult());
    }

    @Test
    void shouldMapToRequestResponseWhenResultsPageIsNull() {
        // Given
        ResultPaginationDto<RaddRegistryRequestEntity, String> resultPaginationDto = new ResultPaginationDto<>();
        resultPaginationDto.setResultsPage(null);
        resultPaginationDto.setNextPagesKey(Collections.emptyList());
        resultPaginationDto.setMoreResult(true);

        // When
        RequestResponse result = raddRegistryUtils.mapToRequestResponse(resultPaginationDto);

        // Then
        assertNotNull(result);
        assertNull(result.getItems());
        assertEquals(0, result.getNextPagesKey().size());
        assertTrue(result.getMoreResult());
    }
}
