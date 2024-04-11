package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {RegistryController.class})
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = {RegistryController.class})
class RegistryControllerTest {
    @Autowired
    private RegistryController registryController;


    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private RegistryService registryService;
    public static final String PN_PAGOPA_CX_ID = "x-pagopa-pn-cx-id";
    public static final String PN_PAGOPA_CX_TYPE = "x-pagopa-pn-cx-type";
    public static final String PN_PAGOPA_UID = "uid";

    /**
     * Method under test: {@link RegistryController#uploadRegistryRequests(CxTypeAuthFleet, String, String, Mono, ServerWebExchange)}
     */
    @Test
    void testUploadRegistryRequests3() {
        when(registryService.uploadRegistryRequests(Mockito.<String>any(), Mockito.<Mono<RegistryUploadRequest>>any()))
                .thenReturn(mock(Mono.class));
        registryController.uploadRegistryRequests(CxTypeAuthFleet.PA, "42", "1234", null, null);
        verify(registryService).uploadRegistryRequests(Mockito.<String>any(), Mockito.<Mono<RegistryUploadRequest>>any());
    }

    /**
     * Method under test: {@link RegistryController#uploadRegistryRequests(CxTypeAuthFleet, String, String, Mono, ServerWebExchange)}
     */
    @Test
    void testUploadRegistryRequests4() {
        when(registryService.uploadRegistryRequests(Mockito.<String>any(), Mockito.<Mono<RegistryUploadRequest>>any()))
                .thenReturn(mock(Mono.class));
        registryController.uploadRegistryRequests(CxTypeAuthFleet.PF, "42", "1234", null, null);
        verify(registryService).uploadRegistryRequests(Mockito.<String>any(), Mockito.<Mono<RegistryUploadRequest>>any());
    }

    /**
     * Method under test: {@link RegistryController#uploadRegistryRequests(CxTypeAuthFleet, String, String, Mono, ServerWebExchange)}
     */
    @Test
    void testUploadRegistryRequests5() {
        when(registryService.uploadRegistryRequests(Mockito.<String>any(), Mockito.<Mono<RegistryUploadRequest>>any()))
                .thenReturn(mock(Mono.class));
        registryController.uploadRegistryRequests(CxTypeAuthFleet.PG, "42", "1234", null, null);
        verify(registryService).uploadRegistryRequests(Mockito.<String>any(), Mockito.<Mono<RegistryUploadRequest>>any());
    }

    /**
     * Method under test: {@link RegistryController#uploadRegistryRequests(CxTypeAuthFleet, String, String, Mono, ServerWebExchange)}
     */
    @Test
    void testUploadRegistryRequests6() {
        when(registryService.uploadRegistryRequests(Mockito.<String>any(), Mockito.<Mono<RegistryUploadRequest>>any()))
                .thenReturn(mock(Mono.class));
        registryController.uploadRegistryRequests(CxTypeAuthFleet.RADD, "42", "1234", null, null);
        verify(registryService).uploadRegistryRequests(Mockito.<String>any(), Mockito.<Mono<RegistryUploadRequest>>any());
    }

    @Test
    void documentUploadTest() {
        RegistryUploadResponse response = new RegistryUploadResponse();
        RegistryUploadRequest req = new RegistryUploadRequest();

        String path = "/radd-net/api/v1/registry/import/upload";
        Mockito.when(registryService.uploadRegistryRequests(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(response));
        webTestClient.post()
                .uri(path)
                .header(PN_PAGOPA_UID, "myUid")
                .header( PN_PAGOPA_CX_ID, "cxId")
                .header( PN_PAGOPA_CX_TYPE, "PA")
                .body(Mono.just(req), RegistryUploadRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }
    @Test
    void testVerifyRequest() {
        // Arrange
        String xPagopaPnCxId = "cxId";
        String requestId = "requestId";
        VerifyRequestResponse expectedResponse = new VerifyRequestResponse();
        when(registryService.verifyRegistriesImportRequest(xPagopaPnCxId, requestId))
                .thenReturn(Mono.just(expectedResponse));

        // Act
        Mono<ResponseEntity<VerifyRequestResponse>> result = registryController.verifyRequest(CxTypeAuthFleet.PA, xPagopaPnCxId, "uid", requestId, null);

        // Assert
        ResponseEntity<VerifyRequestResponse> responseEntity = result.block();
        assert responseEntity != null;
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(registryService).verifyRegistriesImportRequest(xPagopaPnCxId, requestId);
    }

    @Test
    void testRetrieveRequestItems() {
        // Arrange
        String xPagopaPnCxId = "cxId";
        String requestId = "requestId";
        RequestResponse expectedResponse = new RequestResponse();
        when(registryService.retrieveRequestItems(any(), any(), any(), any()))
                .thenReturn(Mono.just(expectedResponse));

        // Act
        Mono<ResponseEntity<RequestResponse>> result = registryController.retrieveRequestItems(CxTypeAuthFleet.PA, xPagopaPnCxId, "uid", requestId, 10, "lastKey", null);

        // Assert
        ResponseEntity<RequestResponse> responseEntity = result.block();
        assert responseEntity != null;
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertEquals(expectedResponse, responseEntity.getBody());
        verify(registryService).retrieveRequestItems(xPagopaPnCxId, requestId, 10, "lastKey");
    }
}

