package it.pagopa.pn.radd.services.radd.fsu.v1;

import io.micrometer.core.instrument.util.StringUtils;
import it.pagopa.pn.radd.mapper.AddressMapper;
import it.pagopa.pn.radd.mapper.NormalizedAddressMapper;
import it.pagopa.pn.radd.mapper.RaddRegistryMapper;
import it.pagopa.pn.radd.mapper.StoreRegistryMapper;
import it.pagopa.pn.radd.middleware.db.RaddRegistryV2DAO;
import it.pagopa.pn.radd.middleware.db.entities.AddressEntity;
import it.pagopa.pn.radd.middleware.db.entities.NormalizedAddressEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntityV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {StoreRegistryService.class})
class StoreRegistryServiceTest {

    @Mock
    RaddRegistryV2DAO raddRegistryDAO;

    @InjectMocks
    private StoreRegistryService storeRegistryService;

    @BeforeEach
    void setUp() {
        NormalizedAddressMapper normalizedAddressMapper = new NormalizedAddressMapper();
        AddressMapper addressMapper = new AddressMapper();
        storeRegistryService = new StoreRegistryService(raddRegistryDAO, new StoreRegistryMapper(new RaddRegistryMapper(normalizedAddressMapper, addressMapper), normalizedAddressMapper, addressMapper));
    }

    private RaddRegistryEntityV2 getRegistryEntity() {
        RaddRegistryEntityV2 registryEntity = new RaddRegistryEntityV2();
        registryEntity.setPartnerId("partnerId");
        registryEntity.setLocationId("locationId");
        registryEntity.setDescription("testDescription");
        registryEntity.setOpeningTime("testOpeningTime");

        NormalizedAddressEntity normalizedAddressEntity = new NormalizedAddressEntity();
        normalizedAddressEntity.setCountry("country");
        normalizedAddressEntity.setProvince("pr");
        normalizedAddressEntity.setCity("city");
        normalizedAddressEntity.setCap("cap");
        registryEntity.setNormalizedAddress(normalizedAddressEntity);

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setCountry("country");
        addressEntity.setProvince("pr");
        addressEntity.setCity("city");
        addressEntity.setCap("cap");
        addressEntity.setAddressRow("addressRow");
        registryEntity.setAddress(addressEntity);

        return registryEntity;
    }

    @Test
    void retrieveStoreRegistries() {
        RaddRegistryEntityV2 registryEntity = getRegistryEntity();
        List<RaddRegistryEntityV2> registryEntities = List.of(registryEntity);
        Map<String, AttributeValue> lastEvaluetadKey = Map.of("partnerId", AttributeValue.builder()
                                                                                         .s("partnerId")
                                                                                         .build(),
                                                              "locationId", AttributeValue.builder()
                                                                                          .s("locationId")
                                                                                          .build());

        when(raddRegistryDAO.scanRegistries(any(), any())).thenReturn(Mono.just(Page.create(registryEntities, lastEvaluetadKey)));

        StepVerifier.create(storeRegistryService.retrieveStoreRegistries(1000, "lastKey"))
                    .expectNextMatches(storeRegistriesResponse -> storeRegistriesResponse.getRegistries().size() == 1 &&
                                                                  StringUtils.isNotEmpty(storeRegistriesResponse.getLastKey()))
                    .verifyComplete();
    }

}