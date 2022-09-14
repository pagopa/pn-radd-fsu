package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActStartTransactionRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.StartTransactionResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.StartTransactionResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TransactionService {

    private final PnDataVaultClient pnDataVaultClient;

    public TransactionService(PnDataVaultClient pnDataVaultClient) {
        this.pnDataVaultClient = pnDataVaultClient;
    }

    public Mono<StartTransactionResponse> startTransaction(String uid, Mono<ActStartTransactionRequest> request){
        log.info("Service");

        return request.map(m-> {
                if (m != null) log.info("M not null");
                return m;
            }).flatMap(item -> {
                return pnDataVaultClient.getEnsureFiscalCode(item.getRecipientTaxId());
            })
            .map(response -> {
                log.info("Code ensure : {}", response);
                StartTransactionResponse startTransactionResponse = new StartTransactionResponse();
                StartTransactionResponseStatus startTransactionResponseStatus = new StartTransactionResponseStatus();
                startTransactionResponseStatus.setCode(StartTransactionResponseStatus.CodeEnum.NUMBER_2);
                startTransactionResponse.setStatus(startTransactionResponseStatus);
                return startTransactionResponse;
            });
    }
}
