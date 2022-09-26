package it.pagopa.pn.radd.rest.radd.fsu;

import it.pagopa.pn.radd.middleware.msclient.PnDeliveryInternalClient;
import it.pagopa.pn.radd.rest.radd.v1.api.AorDocumentInquiryApi;
import it.pagopa.pn.radd.rest.radd.v1.api.AorTransactionManagementApi;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Random;


@RestController
public class AorPrivateRestV1Controller implements AorDocumentInquiryApi, AorTransactionManagementApi {

    private final Random rnd = new Random();

    private final PnDeliveryInternalClient pnDeliveryInternalClient;

    public AorPrivateRestV1Controller(PnDeliveryInternalClient pnDeliveryInternalClient) {
        this.pnDeliveryInternalClient = pnDeliveryInternalClient;
    }

    @Override
    public Mono<ResponseEntity<AORInquiryResponse>> aorInquiry(String uid, String recipientTaxId,
                                                               String recipientType, ServerWebExchange exchange) {
        AORInquiryResponse response = new AORInquiryResponse();
        response.setResult(Boolean.TRUE);
        ResponseStatus status = new ResponseStatus();
        status.setCode(ResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage("OK");
        response.setStatus(status);
        return Mono.delay(Duration.ofMillis(rnd.nextInt(500))).just(ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @Override
    public Mono<ResponseEntity<AbortTransactionResponse>> abortAorTransaction(String uid, Mono<AbortTransactionRequest> abortTransactionRequest, ServerWebExchange exchange) {
        AbortTransactionResponse response = new AbortTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage("OK");
        response.setStatus(status);
        return Mono.delay(Duration.ofMillis(rnd.nextInt(500))).just(ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @Override
    public Mono<ResponseEntity<CompleteTransactionResponse>> completeAorTransaction(String uid, Mono<CompleteTransactionRequest> completeTransactionRequest, ServerWebExchange exchange) {
        CompleteTransactionResponse response = new CompleteTransactionResponse();
        TransactionResponseStatus status = new TransactionResponseStatus();
        status.setCode(TransactionResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage("OK");
        response.setStatus(status);
        return Mono.delay(Duration.ofMillis(rnd.nextInt(500))).just(ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @Override
    public Mono<ResponseEntity<StartTransactionResponse>> startAorTransaction(String uid, Mono<AorStartTransactionRequest> aorStartTransactionRequest, ServerWebExchange exchange) {
        StartTransactionResponse response = new StartTransactionResponse();
        StartTransactionResponseStatus status = new StartTransactionResponseStatus();
        status.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_0);
        status.setMessage("OK");
        status.setRetryAfter(BigDecimal.valueOf(600));
        response.setStatus(status);

        final String iun = "LJLH-GNTJ-DVXR-202209-J-1";
        final String docIdx = "0";

        return Mono.just(response)
                .zipWhen(r -> pnDeliveryInternalClient.getPresignedUrlDocument(iun, docIdx),
                        (r, n) -> {
                            r.setUrlList(Arrays.asList(n.getUrl()));
                            return r;
                        })
                .map(re -> ResponseEntity.status(HttpStatus.OK).body(re));
    }
}
