package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.RaddFiscalCodeEnsureException;
import it.pagopa.pn.radd.exception.RaddIunNotFoundException;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActStartTransactionRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.StartTransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TransactionService {

    private final PnDataVaultClient pnDataVaultClient;
    private final PnDeliveryClient pnDeliveryClient;

    private final RaddTransactionDAO raddTransactionDAO;

    public TransactionService(PnDataVaultClient pnDataVaultClient, PnDeliveryClient pnDeliveryClient, RaddTransactionDAO raddTransactionDAO) {
        this.pnDataVaultClient = pnDataVaultClient;
        this.pnDeliveryClient = pnDeliveryClient;
        this.raddTransactionDAO = raddTransactionDAO;
    }

    public Mono<StartTransactionResponse> startTransaction(String uid, Mono<ActStartTransactionRequest> request){
        log.info("Service");

        return request
                .map(m-> {
                    if (m != null) log.info("M not null");
                    return m;
                })
                .zipWhen(item -> getIun(item.getRecipientType().getValue(), item.getRecipientTaxId(), item.getQrCode()))
                .zipWhen(value -> getCounterNotification(value.getT2(), value.getT1().getOperationId())
                        .flatMap(counter -> getEnsureFiscalCode(value.getT1().getRecipientTaxId()))
                )
                .map( allData -> {
                    String iun = allData.getT1().getT2();
                    String ensureFiscalCode = allData.getT2();
                    log.info("Iun data : {}", iun);
                    log.info("Fiscal Code ensure : {}", ensureFiscalCode);
                    return new StartTransactionResponse();
                });

    }

    private Mono<String> getEnsureFiscalCode(String fiscalCode){
        return this.pnDataVaultClient.getEnsureFiscalCode(fiscalCode)
                .map(response -> {

                    if (response == null || Strings.isBlank(response)){
                        throw new RaddFiscalCodeEnsureException();
                    }
                    return response;
                }).onErrorResume(Mono::error);
    }

    private Mono<Integer> getCounterNotification(String iun, String operationId){
        return Mono.fromFuture(this.raddTransactionDAO.countTransactionIunIdPractice(iun, operationId)
                .thenApply(response -> response)
        );
    }

    private Mono<String> getIun(String recipientType, String recipientTaxId, String qrCode){
        return this.pnDeliveryClient.getCheckAar(recipientType, recipientTaxId, qrCode)
                .map(response -> {
                    if (response == null || Strings.isBlank(response.getIun())){
                        throw new RaddIunNotFoundException();
                    }
                    return response.getIun();
                }).onErrorResume(Mono::error);
    }

}
