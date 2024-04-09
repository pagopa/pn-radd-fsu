package it.pagopa.pn.radd.services.radd.fsu.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.UpdateRegistryRequest;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.pojo.PnLastEvaluatedKey;
import it.pagopa.pn.radd.pojo.ResultPaginationDto;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import it.pagopa.pn.radd.utils.RaddRegistryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {RegistrySelfService.class})
class RegistrySelfServiceTest {

    @Mock
    private RaddRegistryDAO raddRegistryDAO;
    @Mock
    private RaddRegistryRequestDAO raddRegistryRequestDAO;
    @Mock
    private PnRaddFsuConfig pnRaddFsuConfig;
    @Mock
    private SecretService secretService;
    private RaddRegistryUtils raddRegistryUtils;
    private RegistrySelfService registrySelfService;

    @BeforeEach
    void setUp() {
        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(new ObjectMapper());
        RaddRegistryUtils utils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig, secretService);
        registrySelfService = new RegistrySelfService(raddRegistryDAO, raddRegistryRequestDAO, utils);
    }

    @Test
    void updateRegistryNotFound() {
        UpdateRegistryRequest updateRegistryRequest = new UpdateRegistryRequest();
        when(raddRegistryDAO.find("registryId", "cxId")).thenReturn(Mono.empty());
        StepVerifier.create(registrySelfService.updateRegistry("registryId", "cxId", updateRegistryRequest))
                .verifyErrorMessage("Punto di ritiro SEND non trovato");
    }

    @Test
    void updateRegistry() {
        UpdateRegistryRequest updateRegistryRequest = new UpdateRegistryRequest();
        updateRegistryRequest.setDescription("description");
        updateRegistryRequest.setOpeningTime("openingTime");
        updateRegistryRequest.setPhoneNumber("phoneNumber");
        RaddRegistryEntity entity = new RaddRegistryEntity();
        entity.setRegistryId("registryId");
        when(raddRegistryDAO.find("registryId", "cxId")).thenReturn(Mono.just(entity));
        when(raddRegistryDAO.updateRegistryEntity(entity)).thenReturn(Mono.just(entity));
        StepVerifier.create(registrySelfService.updateRegistry("registryId", "cxId", updateRegistryRequest))
                .expectNextMatches(raddRegistryEntity -> entity.getDescription().equalsIgnoreCase("description")
                        && entity.getOpeningTime().equalsIgnoreCase("openingTime")
                        && entity.getPhoneNumber().equalsIgnoreCase("phoneNumber"))
                .verifyComplete();
    }

    @Test
    void registryListing() {
        ResultPaginationDto<RaddRegistryEntity, PnLastEvaluatedKey> paginator = new ResultPaginationDto<RaddRegistryEntity, PnLastEvaluatedKey>().toBuilder().build();
        paginator.setResultsPage(List.of());
        PnLastEvaluatedKey lastEvaluatedKeyToSerialize = new PnLastEvaluatedKey();
        lastEvaluatedKeyToSerialize.setExternalLastEvaluatedKey( "SenderId##creationMonth" );
        lastEvaluatedKeyToSerialize.setInternalLastEvaluatedKey(
                Map.of( "KEY", AttributeValue.builder()
                        .s( "VALUE" )
                        .build() )  );
        String serializedLEK = lastEvaluatedKeyToSerialize.serializeInternalLastEvaluatedKey();
        when(raddRegistryRequestDAO.findAll(eq("cxId"), eq(1),eq("cap"), eq("city"), eq("pr"), eq("externalCode"), any())).thenReturn(Mono.just(paginator));
        StepVerifier.create(registrySelfService.registryListing("cxId", 1, serializedLEK,"cap", "city", "pr", "externalCode"))
                .expectNextMatches(registriesResponse -> Boolean.FALSE.equals(registriesResponse.getMoreResult()))
                .verifyComplete();
    }
}