package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.services.radd.fsu.v1.DocumentUploadService;
import it.pagopa.pn.radd.services.radd.fsu.v1.OperationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {OperationPrivateRestV1Controller.class})
class OperationPrivateRestV1ControllerTest {

    public static final String PN_PAGOPA_CX_ID = "x-pagopa-pn-cx-id";
    public static final String PN_PAGOPA_CX_TYPE = "x-pagopa-pn-cx-type";
    public static final String PN_PAGOPA_UID = "uid";

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private OperationService operationService;

    @Test
    void getTransactionTest() {
        OperationResponse response = new OperationResponse();
        response.setElement(new OperationDetailResponse());

        String path = "/radd-fsu-private/api/v1/operations/by-id/{operationId}"
                        .replace("{operationId}", "1200");
        Mockito.when(operationService
                .getTransaction(Mockito.anyString()))
                .thenReturn(Mono.just(response));
        webTestClient.get()
                .uri(path)
                .header(PN_PAGOPA_UID, "myUid")
                .header( PN_PAGOPA_CX_ID, "cxId")
                .header( PN_PAGOPA_CX_TYPE, "PA")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getPracticesTest() {
        OperationsResponse response = new OperationsResponse();
        response.setResult(true);

        String path = "/radd-fsu-private/api/v1/operations/by-iun/{iun}"
                .replace("{iun}", "iun-123");
        Mockito.when(operationService
                .getPracticesId(Mockito.anyString()))
                .thenReturn(Mono.just(response));
        webTestClient.get()
                .uri(path)
                .header(PN_PAGOPA_UID, "myUid")
                .header( PN_PAGOPA_CX_ID, "cxId")
                .header( PN_PAGOPA_CX_TYPE, "PA")
                .exchange()
                .expectStatus().isOk();
    }

}