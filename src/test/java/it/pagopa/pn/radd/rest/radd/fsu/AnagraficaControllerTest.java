package it.pagopa.pn.radd.rest.radd.fsu;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadResponse;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntity;
import it.pagopa.pn.radd.services.radd.fsu.v1.AnagraficaService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;

@ContextConfiguration(classes = {AnagraficaController.class})
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = {AnagraficaController.class})
class AnagraficaControllerTest {
    @Autowired
    private AnagraficaController anagraficaController;

    @MockBean
    private AnagraficaService anagraficaService;

    @Autowired
    WebTestClient webTestClient;

    public static final String PN_PAGOPA_CX_ID = "x-pagopa-pn-cx-id";
    public static final String PN_PAGOPA_CX_TYPE = "x-pagopa-pn-cx-type";
    public static final String PN_PAGOPA_UID = "uid";

    @Test
    void testDeleteRegistry() {
        RaddRegistryEntity response = getRaddRegistryEntity();
        String registryId = "testRegistryId";
        String endDate = "2025-12-31T23:59:59Z";

        String path = "/radd-alt/api/v1/registry/{registryId}/dismiss?endDate={endDate}";
        Mockito.when(anagraficaService.deleteRegistry(any(), any(), any()))
                .thenReturn(Mono.just(response));
        webTestClient.patch()
                .uri(path,registryId,endDate)
                .header(PN_PAGOPA_CX_TYPE, CxTypeAuthFleet.PA.toString())
                .header(PN_PAGOPA_CX_ID, "x-pagopa-pn-cx-id")
                .header(PN_PAGOPA_UID, "uid")
                .exchange()
                .expectStatus().isNoContent();

    }
    private static @NotNull RaddRegistryEntity getRaddRegistryEntity() {
        RaddRegistryEntity registryEntity = new RaddRegistryEntity();
        registryEntity.setRegistryId("testRegistryId");
        registryEntity.setCxId("testCxId");
        registryEntity.setRequestId("testRequestId");
        registryEntity.setNormalizedAddress("testNormalizedAddress");
        registryEntity.setDescription("testDescription");
        registryEntity.setPhoneNumber("testPhoneNumber");
        registryEntity.setGeoLocation("testGeoLocation");
        registryEntity.setZipCode("00100");
        registryEntity.setOpeningTime("testOpeningTime");
        registryEntity.setStartValidity(Instant.parse("2020-04-06T12:00:00Z"));
        registryEntity.setEndValidity(Instant.parse("2025-10-06T12:00:00Z"));
        return registryEntity;
    }
}
