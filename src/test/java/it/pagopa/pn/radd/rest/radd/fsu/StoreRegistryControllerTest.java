package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.StoreRegistriesResponse;
import it.pagopa.pn.radd.config.RestExceptionHandler;
import it.pagopa.pn.radd.services.radd.fsu.v1.StoreRegistryService;
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
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {StoreRegistryController.class, RestExceptionHandler.class})
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = {StoreRegistryController.class})
public class StoreRegistryControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private StoreRegistryService storeRegistryService;

    @Test
    void retrieveStoreRegistries() {
        String path = "/radd-net-private/api/v1/store";
        StoreRegistriesResponse response = new StoreRegistriesResponse();
        when(storeRegistryService.retrieveStoreRegistries(any(), any())).thenReturn(Mono.just(response));
        webTestClient.get()
                .uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void retrieveStoreRegistriesInvalidLimitParameter() {
        String path = "/radd-net-private/api/v1/store?limit=1001";
        StoreRegistriesResponse response = new StoreRegistriesResponse();
        when(storeRegistryService.retrieveStoreRegistries(any(), any())).thenReturn(Mono.just(response));
        webTestClient.get()
                .uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
