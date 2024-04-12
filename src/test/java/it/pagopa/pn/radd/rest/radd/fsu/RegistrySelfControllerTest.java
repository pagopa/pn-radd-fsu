package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistrySelfService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RegistrySelfController.class})
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = {RegistryController.class})
class RegistrySelfControllerTest {

    @MockBean
    private RegistrySelfService registrySelfService;

    @Autowired
    WebTestClient webTestClient;

    public static final String PN_PAGOPA_CX_ID = "x-pagopa-pn-cx-id";
    public static final String PN_PAGOPA_CX_TYPE = "x-pagopa-pn-cx-type";
    public static final String PN_PAGOPA_UID = "uid";
    public static final String LIMIT = "limit";
    public static final String LASTKEY = "lastKey";
    public static final String CAP = "cap";
    public static final String CITY = "city";
    public static final String PR = "pr";
    public static final String EXTERNALCODE = "externalCode";

    @Test
    void updateRegistry() {
        String path = "/radd-net/api/v1/registry/{registryId}";

        UpdateRegistryRequest request = new UpdateRegistryRequest();
        request.setPhoneNumber("phoneNumber");
        request.setDescription("description");
        request.setOpeningTime("openingTime");
        when(registrySelfService.updateRegistry(any(), any(), any())).thenReturn(Mono.just(mock(RaddRegistryEntity.class)));

        webTestClient.patch()
                .uri(path, "registryId")
                .header(PN_PAGOPA_UID, "myUid")
                .header( PN_PAGOPA_CX_ID, "cxId")
                .header( PN_PAGOPA_CX_TYPE, "PA")
                .body(Mono.just(request), UpdateRegistryRequest.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void createReqistry() {
        String path = "/radd-net/api/v1/registry";

        CreateRegistryRequest createRegistryRequest = new CreateRegistryRequest();
        Address address = new Address();
        address.setAddressRow("addressRow");
        address.setCap("00100");
        address.setCity("city");
        address.setCountry("country");
        address.setPr("province");
        createRegistryRequest.setAddress(address);
        CreateRegistryResponse createRegistryResponse = new CreateRegistryResponse();
        when(registrySelfService.addRegistry(any(), any())).thenReturn(Mono.just(createRegistryResponse));

        webTestClient.post()
                .uri(path )
                .header(PN_PAGOPA_UID, "myUid")
                .header( PN_PAGOPA_CX_ID, "cxId")
                .header( PN_PAGOPA_CX_TYPE, "PA")
                .body(Mono.just(createRegistryRequest), CreateRegistryRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void retrieveRegistries() {
        String path = "/radd-net/api/v1/registry";
        RegistriesResponse response = new RegistriesResponse();
        when(registrySelfService.registryListing(any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(response));
        webTestClient.get()
                .uri(path)
                .header( PN_PAGOPA_CX_TYPE, CxTypeAuthFleet.RADD.getValue())
                .header( PN_PAGOPA_CX_ID, "xpagopapncxid")
                .header( PN_PAGOPA_UID, "uid")
                .header( LIMIT, "10")
                .header( LASTKEY, "lastKey")
                .header( CAP, "cap")
                .header( CITY, "city")
                .header( PR, "pr")
                .header( EXTERNALCODE, "externalCode")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }
}