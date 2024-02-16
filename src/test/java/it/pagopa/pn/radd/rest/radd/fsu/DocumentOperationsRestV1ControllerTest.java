package it.pagopa.pn.radd.rest.radd.fsu;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DocumentUploadRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DocumentUploadResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.ResponseStatus;
import it.pagopa.pn.radd.services.radd.fsu.v1.DocumentOperationsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ContextConfiguration(classes = {DocumentOperationsRestV1Controller.class})
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = {DocumentOperationsRestV1Controller.class})
class DocumentOperationsRestV1ControllerTest {
    @Autowired
    private DocumentOperationsRestV1Controller documentOperationsRestV1Controller;

    @MockBean
    private DocumentOperationsService documentOperationsService;

    @Autowired
    WebTestClient webTestClient;

    public static final String PN_PAGOPA_CX_ID = "x-pagopa-pn-cx-id";
    public static final String PN_PAGOPA_CX_TYPE = "x-pagopa-pn-cx-type";
    public static final String PN_PAGOPA_UID = "uid";

    @Test
    void documentDownloadTest() {
        byte[] response = new byte[0];

        String path = "/radd-net/api/v1/download/{operationType}/{operationId}".replace("{operationType}", "ACT")
                .replace("{operationId}", "42");
        Mockito.when(documentOperationsService
                .documentDownload(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())
        ).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .header(PN_PAGOPA_UID, "myUid")
                .header(PN_PAGOPA_CX_ID, "cxId")
                .header(PN_PAGOPA_CX_TYPE, "PA")
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Method under test: {@link DocumentOperationsRestV1Controller#documentDownload(String, String, CxTypeAuthFleet, String, String, ServerWebExchange)}
     */
    @Test
    void testDocumentDownload3() {
        when(documentOperationsService.documentDownload(Mockito.<String>any(), Mockito.<String>any(),
                Mockito.<CxTypeAuthFleet>any(), Mockito.<String>any(), Mockito.anyString())).thenReturn(mock(Mono.class));
        documentOperationsRestV1Controller.documentDownload("Operation Type", "42", CxTypeAuthFleet.PA, "42", "attach", null);
        verify(documentOperationsService).documentDownload(Mockito.<String>any(), Mockito.<String>any(),
                Mockito.<CxTypeAuthFleet>any(), Mockito.<String>any(), Mockito.anyString());
    }

    /**
     * Method under test: {@link DocumentOperationsRestV1Controller#documentDownload(String, String, CxTypeAuthFleet, String, String, ServerWebExchange)}
     */
    @Test
    void testDocumentDownload4() {
        when(documentOperationsService.documentDownload(Mockito.<String>any(), Mockito.<String>any(),
                Mockito.<CxTypeAuthFleet>any(), Mockito.<String>any(), Mockito.anyString())).thenReturn(mock(Mono.class));
        documentOperationsRestV1Controller.documentDownload("Operation Type", "42", CxTypeAuthFleet.PF, "42", "attach", null);
        verify(documentOperationsService).documentDownload(Mockito.<String>any(), Mockito.<String>any(),
                Mockito.<CxTypeAuthFleet>any(), Mockito.<String>any(), Mockito.anyString());
    }

    /**
     * Method under test: {@link DocumentOperationsRestV1Controller#documentDownload(String, String, CxTypeAuthFleet, String, String, ServerWebExchange)}
     */
    @Test
    void testDocumentDownload5() {
        when(documentOperationsService.documentDownload(Mockito.<String>any(), Mockito.<String>any(),
                Mockito.<CxTypeAuthFleet>any(), Mockito.<String>any(), Mockito.anyString())).thenReturn(mock(Mono.class));
        documentOperationsRestV1Controller.documentDownload("Operation Type", "42", CxTypeAuthFleet.PG, "42", "attach", null);
        verify(documentOperationsService).documentDownload(Mockito.<String>any(), Mockito.<String>any(),
                Mockito.<CxTypeAuthFleet>any(), Mockito.<String>any(), Mockito.anyString());
    }

    @Test
    void documentUploadTest() {
        DocumentUploadResponse response = new DocumentUploadResponse();
        ResponseStatus status = new ResponseStatus();
        status.setMessage("OK");
        response.setStatus(status);
        DocumentUploadRequest req = new DocumentUploadRequest();

        String path = "/radd-net/api/v1/documents/upload";
        Mockito.when(documentOperationsService.createFile( Mockito.anyString(), Mockito.any() ))
                .thenReturn(Mono.just(response));
        webTestClient.post()
                .uri(path)
                .header(PN_PAGOPA_UID, "myUid")
                .header( PN_PAGOPA_CX_ID, "cxId")
                .header( PN_PAGOPA_CX_TYPE, "PA")
                .body(Mono.just(req), DocumentUploadRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }
}

