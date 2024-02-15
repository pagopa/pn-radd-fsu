package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.radd.services.radd.fsu.v1.ActService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Date;

// TODO: Test disabilitati da riparare in fase di aggiornamento rispettiva API


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

        String path = "/radd-net/api/v1/act/inquiry";
        Mockito.when(actService
                .actInquiry(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())
        ).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("iun", "")
                        .queryParam("uid", "123-456")
                        .queryParam("recipientTaxId", "MRASSS90A67H718I")
                        .queryParam("recipientType", "PF")
                        .queryParam("qrCode", "qrCode").build())
                .header(PN_PAGOPA_UID, "myUid")
                .header(PN_PAGOPA_CX_ID, "cxId")
                .header(PN_PAGOPA_CX_TYPE, "PA")
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

        String path = "/radd-net/api/v1/act/transaction/complete";
        Mockito.when(actService
                .completeTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())
        ).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(path)
                .header(PN_PAGOPA_UID, "myUid")
                .header(PN_PAGOPA_CX_ID, "cxId")
                .header(PN_PAGOPA_CX_TYPE, "PA")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(req), CompleteTransactionRequest.class)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void abortActTransactionTest() {
        AbortTransactionResponse response = new AbortTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setMessage("OK");
        response.status(status);

        AbortTransactionRequest req = new AbortTransactionRequest();
        req.setOperationId("123");
        req.setOperationDate(new Date());

        String path = "/radd-net/api/v1/act/transaction/abort";
        Mockito.when(actService.abortTransaction(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any())
        ).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(path)
                .header(PN_PAGOPA_UID, "myUid")
                .header(PN_PAGOPA_CX_ID, "cxId")
                .header(PN_PAGOPA_CX_TYPE, "PA")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(req), AbortTransactionRequest.class)
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
        req.setQrCode("abcQrcode");
        req.setVersionToken("123TokenDocument");
        req.setFileKey("123FileKey");
        req.setOperationId("123");
        req.setRecipientTaxId("TNTGTR76E21H751S");
        req.setRecipientType(ActStartTransactionRequest.RecipientTypeEnum.PG);
        req.setChecksum("YTlkZGRkNzgyZWM0NzkyODdjNmQ0NGE5ZDM2YTg4ZjQ5OTE1ZGM2NjliYjgzNzViMTZhMmE5ZmE3NmE4ZDQzNwo");
        req.setOperationDate(new Date());

        String path = "/radd-net/api/v1/act/transaction/start";
        Mockito.when(actService
                .startTransaction(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any())
        ).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(path)
                .header(PN_PAGOPA_UID, "myUid")
                .header(PN_PAGOPA_CX_ID, "cxId")
                .header(PN_PAGOPA_CX_TYPE, "PA")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(req), ActStartTransactionRequest.class)
                .exchange()
                .expectStatus().isOk();
    }

}