package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import it.pagopa.pn.radd.services.radd.fsu.v1.ActService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Date;

@WebFluxTest(controllers = {ActPrivateRestV1Controller.class})
class ActPrivateRestV1ControllerTest {

    public static final String PN_PAGOPA_CX_ID = "x-pagopa-pn-cx-id";
    public static final String PN_PAGOPA_CX_TYPE = "x-pagopa-pn-cx-type";
    public static final String PN_PAGOPA_UID = "uid";

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private ActService actService;

    @Test
    void actInquiryTest() {
        ActInquiryResponse response = new ActInquiryResponse();
        response.setResult(true);

        String path = "/radd-fsu-private/api/v1/act/inquiry";
        Mockito.when(actService
                .actInquiry(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())
                ).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                                .queryParam("uid", "123-456")
                                .queryParam("recipientTaxId", "MRASSS90A67H718I")
                                .queryParam("recipientType", "PF")
                                .queryParam("qrCode", "qrCode").build())
                .header(PN_PAGOPA_UID, "myUid")
                .header( PN_PAGOPA_CX_ID, "cxId")
                .header( PN_PAGOPA_CX_TYPE, "PA")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void completeActTransactionTest() {
        CompleteTransactionResponse response = new CompleteTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setMessage("OK");
        response.status(status);

        CompleteTransactionRequest req = new CompleteTransactionRequest();
        req.setOperationId("123");
        req.setOperationDate(new Date());

        String path = "/radd-fsu-private/api/v1/act/transaction/complete";
        Mockito.when(actService
                .completeTransaction(Mockito.anyString(), Mockito.any())
        ).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(path)
                .header(PN_PAGOPA_UID, "myUid")
                .header( PN_PAGOPA_CX_ID, "cxId")
                .header( PN_PAGOPA_CX_TYPE, "PA")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(req), CompleteTransactionRequest.class)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void startActTransactionTest() {
        StartTransactionResponse response = new StartTransactionResponse();
        StartTransactionResponseStatus status = new StartTransactionResponseStatus();
        status.setMessage("OK");
        response.status(status);

        ActStartTransactionRequest req = new ActStartTransactionRequest();
        req.setOperationId("123");
        req.setOperationDate(new Date());

        String path = "/radd-fsu-private/api/v1/act/transaction/start";
        Mockito.when(actService
                .startTransaction(Mockito.anyString(), Mockito.any())
        ).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(path)
                .header(PN_PAGOPA_UID, "myUid")
                .header( PN_PAGOPA_CX_ID, "cxId")
                .header( PN_PAGOPA_CX_TYPE, "PA")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(req), ActStartTransactionRequest.class)
                .exchange()
                .expectStatus().isOk();
    }

}