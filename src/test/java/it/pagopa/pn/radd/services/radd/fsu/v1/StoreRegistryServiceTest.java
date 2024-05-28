package it.pagopa.pn.radd.services.radd.fsu.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.entities.NormalizedAddressEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import it.pagopa.pn.radd.utils.RaddRegistryUtils;
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
public class StoreRegistryServiceTest {

    @Mock
    RaddRegistryDAO raddRegistryDAO;
    @Mock
    private SecretService secretService;
    @Mock
    private PnRaddFsuConfig pnRaddFsuConfig;

    @InjectMocks
    private StoreRegistryService storeRegistryService;

    @BeforeEach
    void setUp() {
        storeRegistryService = new StoreRegistryService(raddRegistryDAO, new RaddRegistryUtils(new ObjectMapperUtil(new ObjectMapper()), pnRaddFsuConfig, secretService));
    }


    @Test
    void retrieveStoreRegistries() {
        RaddRegistryEntity registryEntity = getRegistryEntity();
        List<RaddRegistryEntity> registryEntities = List.of(registryEntity);
        Map<String, AttributeValue> lastEvaluetadKey = Map.of("cxId", AttributeValue.builder()
                        .s("testCxId")
                        .build(),
                "registryId", AttributeValue.builder()
                        .s("testRegistryId")
                        .build());

        when(raddRegistryDAO.scanRegistries(any(), any())).thenReturn(Mono.just(Page.create(registryEntities, lastEvaluetadKey)));

        StepVerifier.create(storeRegistryService.retrieveStoreRegistries(1000, "lastKey"))
                .expectNextMatches(storeRegistriesResponse -> storeRegistriesResponse.getRegistries().size() == 1 &&
                        storeRegistriesResponse.getRegistries().get(0).getGeoLocation().getLongitude().equals("52.241") &&
                        storeRegistriesResponse.getRegistries().get(0).getAddress().getCity().equals("city") &&
                        StringUtils.isNotEmpty(storeRegistriesResponse.getLastKey()))
                .verifyComplete();
    }

    private RaddRegistryEntity getRegistryEntity() {
        RaddRegistryEntity registryEntity = new RaddRegistryEntity();
        registryEntity.setDescription("testDescription");
        registryEntity.setPhoneNumber("testPhoneNumber");
        registryEntity.setExternalCode("testExternalCode");
        registryEntity.setCapacity("testCapacity");
        registryEntity.setGeoLocation("{\"latitude\": \"41.210\", \"longitude\": \"52.241\"}");
        registryEntity.setOpeningTime("testOpeningTime");
        NormalizedAddressEntity addressEntity = new NormalizedAddressEntity();
        addressEntity.setCountry("country");
        addressEntity.setPr("pr");
        addressEntity.setCity("city");
        addressEntity.setCap("cap");
        registryEntity.setNormalizedAddress(addressEntity);
        return registryEntity;
    }
}
