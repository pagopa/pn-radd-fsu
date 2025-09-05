package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.*;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.CreateRegistryRequestV2;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.GetRegistryResponseV2;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.UpdateRegistryRequestV2;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.mapper.AddressMapper;
import it.pagopa.pn.radd.mapper.NormalizedAddressMapper;
import it.pagopa.pn.radd.mapper.RaddRegistryMapper;
import it.pagopa.pn.radd.mapper.RaddRegistryPageMapper;
import it.pagopa.pn.radd.middleware.db.RaddRegistryV2DAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntityV2;
import it.pagopa.pn.radd.pojo.RaddRegistryPage;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.geoplaces.model.AddressComponentMatchScores;
import software.amazon.awssdk.services.geoplaces.model.MatchScoreDetails;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.radd.utils.DateUtils.convertDateToInstantAtStartOfDay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {RegistrySelfServiceV2.class})
@CustomLog
class RegistrySelfServiceV2Test {

    @Mock
    private RaddRegistryV2DAO raddRegistryDAO;
    @Mock
    private AwsGeoService awsGeoService;
    @Mock
    private RegistrySelfServiceV2 registrySelfServiceV2;

    private static final String PATTERN_FORMAT = "yyyy-MM-dd";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_FORMAT).withZone(ZoneId.systemDefault());

    public static final String PN_PAGOPA_CX_ID = "x-pagopa-pn-cx-id";
    public static final String PN_PAGOPA_UID = "uid";
    public static final Integer LIMIT = 10;
    public static final String LAST_KEY = "lastKey";
    public static final String PARTNER_ID = "partnerId";
    private static final String LOCATION_ID = "locationId";

    private static final String OPENING_TIME_OK = """
            Lun 09:00-13:00, 14:00-18:00; mar 09:30-12:30; MER 09:00-18:00
            """;

    @BeforeEach
    void setUp() {
        RaddRegistryMapper raddRegistryMapper = new RaddRegistryMapper(new NormalizedAddressMapper(), new AddressMapper());
        registrySelfServiceV2 = new RegistrySelfServiceV2(
                raddRegistryDAO,
                awsGeoService,
                raddRegistryMapper,
                new RaddRegistryPageMapper(raddRegistryMapper)
        );
    }

    private RaddRegistryPage raddRegistryPage() {

        RaddRegistryEntityV2 registry = new RaddRegistryEntityV2();
        registry.setPartnerId(PARTNER_ID);

        List<RaddRegistryEntityV2> listRegistry = new ArrayList<>();
        listRegistry.add(registry);

        RaddRegistryPage page = new RaddRegistryPage();
        page.setItems(listRegistry);
        page.setLastKey(LAST_KEY);

        return page;
    }

    private CreateRegistryRequestV2 createValidRegistryRequest() {
        CreateRegistryRequestV2 request = new CreateRegistryRequestV2();

        AddressV2 address = new AddressV2();
        address.setAddressRow("Via Roma 123");
        address.setCap("00100");
        address.setCity("Roma");
        address.setProvince("RM");
        address.setCountry("Italia");
        request.setAddress(address);

        Instant now = Instant.now();
        formatter.format(now);
        request.setStartValidity(formatter.format(now));
        request.setEndValidity(formatter.format(now.plus(1, ChronoUnit.DAYS)));
        request.setDescription("Sportello Test");
        request.setPhoneNumbers(List.of("+390123456789"));
        request.setExternalCodes(List.of("EXT1"));
        request.setEmail("mail@esempio.it");
        request.setOpeningTime("Lun-Ven 08:00-12:00, 14:00-18:00; Sab 09:00-12:00; Dom 10:00-11:00");
        request.setAppointmentRequired(true);
        request.setWebsite("https://test.it");
        request.setPartnerType("CAF");
        return request;
    }

    private AwsGeoService.CoordinatesResult buildCoordinatesResult() {
        AwsGeoService.CoordinatesResult coordinatesResult = new AwsGeoService.CoordinatesResult();
        coordinatesResult.setAwsAddressRow("Via Roma 123");
        coordinatesResult.setAwsSubRegion("Roma");
        coordinatesResult.setAwsPostalCode("00100");
        coordinatesResult.setAwsLocality("RM");
        coordinatesResult.setAwsCountry("Italia");
        AddressComponentMatchScores addressComponents = AddressComponentMatchScores.builder()
                .addressNumber(1.0)
                .locality(1.0)
                .subRegion(1.0)
                .postalCode(1.0)
                .country(1.0)
                .build();
        coordinatesResult.setAwsMatchScore(MatchScoreDetails.builder()
                .overall(1.0)
                .components(builder -> builder.address(addressComponents))
                .build());
        coordinatesResult.setAwsLatitude("12.34567");
        coordinatesResult.setAwsLongitude("100.00000");
        return coordinatesResult;
    }

    @Test
    void addRegistry() {
        CreateRegistryRequestV2 request = createValidRegistryRequest();
        RaddRegistryEntityV2 entity = new RaddRegistryEntityV2();

        when(raddRegistryDAO.findByPartnerId(PARTNER_ID)).thenReturn(Flux.empty());
        when(raddRegistryDAO.putItemIfAbsent(any())).thenReturn(Mono.just(entity));
        when(awsGeoService.getCoordinatesForAddress(any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(buildCoordinatesResult()));

        Mono<RegistryV2> result = registrySelfServiceV2.addRegistry(PARTNER_ID, LOCATION_ID, PN_PAGOPA_UID, request);

        StepVerifier.create(result)
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();
    }

    @Test
    void addRegistry_InvalidIntervalDates() {
        CreateRegistryRequestV2 request = createValidRegistryRequest();
        request.setEndValidity(formatter.format(convertDateToInstantAtStartOfDay(request.getStartValidity()).minus(1, ChronoUnit.DAYS)));

        RaddGenericException ex = Assertions.assertThrows(RaddGenericException.class, () -> registrySelfServiceV2.addRegistry(PARTNER_ID, LOCATION_ID, PN_PAGOPA_UID, request));
        assertEquals(ExceptionTypeEnum.DATE_INTERVAL_ERROR, ex.getExceptionType());
    }

    @Test
    void addRegistry_InvalidDateFormat() {
        CreateRegistryRequestV2 request = createValidRegistryRequest();
        request.setStartValidity("10/02/2022");

        RaddGenericException ex = Assertions.assertThrows(RaddGenericException.class, () -> registrySelfServiceV2.addRegistry(PARTNER_ID, LOCATION_ID, PN_PAGOPA_UID, request));
        assertEquals(ExceptionTypeEnum.DATE_VALIDATION_ERROR, ex.getExceptionType());
    }

    @Test
    void addRegistry_StartValidityInThePast() {
        CreateRegistryRequestV2 request = createValidRegistryRequest();
        request.setStartValidity("2022-10-21");

        RaddGenericException ex = Assertions.assertThrows(RaddGenericException.class, () -> registrySelfServiceV2.addRegistry(PARTNER_ID, LOCATION_ID, PN_PAGOPA_UID, request));
        assertEquals(ExceptionTypeEnum.START_VALIDITY_IN_THE_PAST, ex.getExceptionType());
    }

    @Test
    void addRegistry_DuplicatedExternalCode() {
        CreateRegistryRequestV2 request = createValidRegistryRequest();
        RaddRegistryEntityV2 entity = new RaddRegistryEntityV2();
        entity.setLocationId(UUID.randomUUID().toString());
        entity.setExternalCodes(List.of("EXT1", "EXT2", "EXT3"));
        when(raddRegistryDAO.findByPartnerId(PARTNER_ID)).thenReturn(Flux.just(entity));

        request.setExternalCodes(List.of("EXT1"));
        StepVerifier.create(registrySelfServiceV2.addRegistry(PARTNER_ID, LOCATION_ID, PN_PAGOPA_UID, request))
                .expectErrorMatches(throwable -> throwable instanceof RaddGenericException &&
                        ((RaddGenericException) throwable).getExceptionType() == ExceptionTypeEnum.DUPLICATE_EXT_CODE)
                .verify();
    }

    @Test
    void testRetrieveRegistries_success() {

        RaddRegistryPage page = raddRegistryPage();

        when(raddRegistryDAO.findPaginatedByPartnerId(PARTNER_ID, LIMIT, LAST_KEY))
                .thenReturn(Mono.just(page));

        Mono<GetRegistryResponseV2> result = registrySelfServiceV2.retrieveRegistries(PARTNER_ID, LIMIT, LAST_KEY);

        StepVerifier.create(result)
                    .assertNext(Assertions::assertNotNull)
                    .verifyComplete();
    }

    private UpdateRegistryRequestV2 updateRegistryRequestV2() {
        UpdateRegistryRequestV2 request = new UpdateRegistryRequestV2();

        Instant now = Instant.now();
        formatter.format(now);
        request.setEndValidity(formatter.format(now.plus(1, ChronoUnit.DAYS)));
        request.setDescription("description");
        request.setPhoneNumbers(List.of("+390123456789"));
        request.setExternalCodes(List.of("EXT0"));
        request.setEmail("mail@esempio.it");
        request.setOpeningTime(OPENING_TIME_OK);
        request.setAppointmentRequired(true);
        request.setWebsite("https://test.it");
        return request;
    }

    @Test
    void updateRegistry() {
        UpdateRegistryRequestV2 request = updateRegistryRequestV2();

        RaddRegistryEntityV2 entity = new RaddRegistryEntityV2();
        Instant now = Instant.now();
        entity.setStartValidity(now.minus(1, ChronoUnit.DAYS));
        entity.setPartnerId(PARTNER_ID);
        entity.setLocationId(LOCATION_ID);

        when(raddRegistryDAO.findByPartnerId(PARTNER_ID)).thenReturn(Flux.empty());
        when(raddRegistryDAO.find(PARTNER_ID, LOCATION_ID)).thenReturn(Mono.just(entity));
        when(raddRegistryDAO.updateRegistryEntity(entity)).thenReturn(Mono.just(entity));

        StepVerifier.create(registrySelfServiceV2.updateRegistry(PARTNER_ID, LOCATION_ID, PN_PAGOPA_UID, request))
                    .expectNextMatches(raddRegistryEntity -> entity.getDescription().equalsIgnoreCase(request.getDescription())
                                                             && entity.getEmail().equalsIgnoreCase(request.getEmail()))
                    .verifyComplete();
    }

    @Test
    void updateRegistry_NotFound() {
        when(raddRegistryDAO.find(PARTNER_ID, LOCATION_ID)).thenReturn(Mono.empty());

        StepVerifier.create(registrySelfServiceV2.updateRegistry(PARTNER_ID, LOCATION_ID, PN_PAGOPA_UID, new UpdateRegistryRequestV2()))
                    .verifyErrorMessage(ExceptionTypeEnum.RADD_REGISTRY_NOT_FOUND.getMessage());
    }

    @Test
    void updateRegistry_DuplicatedExternalCode() {
        UpdateRegistryRequestV2 request = updateRegistryRequestV2();

        RaddRegistryEntityV2 entity = new RaddRegistryEntityV2();
        entity.setLocationId(UUID.randomUUID().toString());
        entity.setExternalCodes(List.of("EXT1", "EXT2", "EXT3"));

        when(raddRegistryDAO.findByPartnerId(PARTNER_ID)).thenReturn(Flux.just(entity));
        when(raddRegistryDAO.find(PARTNER_ID, LOCATION_ID)).thenReturn(Mono.just(entity));

        request.setExternalCodes(List.of("EXT1"));
        StepVerifier.create(registrySelfServiceV2.updateRegistry(PARTNER_ID, LOCATION_ID, PN_PAGOPA_UID, request))
                    .expectErrorMatches(throwable -> throwable instanceof RaddGenericException &&
                                                     ((RaddGenericException) throwable).getExceptionType() == ExceptionTypeEnum.DUPLICATE_EXT_CODE)
                    .verify();
    }

    @Test
    void shouldDeleteRegistrySuccessfully() {
        // Given
        String partnerId = "partnerTest";
        String locationId = "locationTest";

        RaddRegistryEntityV2 entity = new RaddRegistryEntityV2();
        entity.setPartnerId(partnerId);
        entity.setLocationId(locationId);


        when(raddRegistryDAO.delete(partnerId, locationId)).thenReturn(Mono.just(entity));

        Mono<RaddRegistryEntityV2> result = registrySelfServiceV2.deleteRegistry(partnerId, locationId);

        StepVerifier.create(result)
                    .expectNextMatches(deleted -> deleted.getPartnerId().equals(partnerId) && deleted.getLocationId().equals(locationId))
                    .verifyComplete();
    }


    @Test
    void shouldFailDeleteWhenRegistryNotFound() {
        // Given
        String partnerId = "partnerTest";
        String locationId = "locationTest";

        when(raddRegistryDAO.delete(partnerId, locationId)).thenReturn(Mono.empty());

        Mono<RaddRegistryEntityV2> result = registrySelfServiceV2.deleteRegistry(partnerId, locationId);

        StepVerifier.create(result).expectErrorMatches(
                throwable -> throwable instanceof RaddGenericException &&
                        ((RaddGenericException) throwable).getExceptionType() == ExceptionTypeEnum.RADD_REGISTRY_NOT_FOUND
        ).verify();
    }

}