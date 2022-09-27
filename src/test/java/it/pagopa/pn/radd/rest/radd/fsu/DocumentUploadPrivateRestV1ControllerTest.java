package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.ResponseStatus;
import it.pagopa.pn.radd.services.radd.fsu.v1.DocumentUploadService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {DocumentUploadPrivateRestV1Controller.class})
class DocumentUploadPrivateRestV1ControllerTest {

    public static final String PN_PAGOPA_CX_ID = "x-pagopa-pn-cx-id";
    public static final String PN_PAGOPA_CX_TYPE = "x-pagopa-pn-cx-type";
    public static final String PN_PAGOPA_UID = "uid";

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private DocumentUploadService documentUploadService;

    @Test
    void documentUploadTest() {
        DocumentUploadResponse response = new DocumentUploadResponse();
        ResponseStatus status = new ResponseStatus();
        status.setMessage("OK");
        response.setStatus(status);
        DocumentUploadRequest req = new DocumentUploadRequest();
        req.setBundleId("bundleId");
        req.setContentType("application/json");

        String path = "/radd-private/api/v1/documents/upload";
        Mockito.when(documentUploadService.createFile( Mockito.anyString(), Mockito.any() ))
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