package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.mapper.AORInquiryResponseMapper;
import it.pagopa.pn.radd.mapper.StartTransactionResponseMapper;
import it.pagopa.pn.radd.mapper.TransactionDataMapper;
import it.pagopa.pn.radd.microservice.msclient.generated.pndeliverypush.internal.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.radd.middleware.db.RaddTransactionDAO;
import it.pagopa.pn.radd.middleware.msclient.PnDataVaultClient;
import it.pagopa.pn.radd.middleware.msclient.PnDeliveryPushClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.pojo.TransactionData;
import it.pagopa.pn.radd.rest.radd.v1.dto.AORInquiryResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.AorStartTransactionRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.StartTransactionResponse;
import it.pagopa.pn.radd.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;


@Slf4j
@Service
public class AorService extends BaseService {

    private final PnDeliveryPushClient pnDeliveryPushClient;
    private final TransactionDataMapper transactionDataMapper;

    public AorService(PnDeliveryPushClient pnDeliveryPushClient, PnDataVaultClient pnDataVaultClient, PnSafeStorageClient pnSafeStorageClient,
                      TransactionDataMapper transactionDataMapper, RaddTransactionDAO raddTransactionDAO) {
        super(pnDataVaultClient, raddTransactionDAO, pnSafeStorageClient);
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.transactionDataMapper = transactionDataMapper;
    }

    public Mono<AORInquiryResponse> aorInquiry(String uid, String recipientTaxId, String recipientType){
        if (StringUtils.isBlank(recipientTaxId)){
            throw new PnInvalidInputException("Il campo codice fiscale non è valorizzato");
        }
        return this.getIunFromPaperNotificationFailed(recipientTaxId)
                .collectList()
                .map(list -> {
                    if (list.isEmpty()){
                        throw new RaddGenericException(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED_FOR_CF, ExceptionCodeEnum.KO);
                    }
                    return AORInquiryResponseMapper.fromResult();
                })
                .onErrorResume(RaddGenericException.class, ex -> Mono.just(AORInquiryResponseMapper.fromException(ex)));
    }


    public Mono<StartTransactionResponse> startTransaction(String uid, Mono<AorStartTransactionRequest> aorStartTransactionRequest){
        return aorStartTransactionRequest.map(req -> validationAorStartTransaction(uid, req))
                .zipWhen(this::getEnsureRecipientAndDelegate, (transaction, transactionReq) -> transactionReq)
                .zipWhen(transaction -> {
                    transaction.setIuns(new ArrayList<>());
                    transaction.setUrls(new ArrayList<>());
                    return this.getIunFromPaperNotificationFailed(transaction.getEnsureRecipientId())
                                .flatMap(item -> {
                                    transaction.getIuns().add(item.getIun());
                                    transaction.getUrls().add(item.getAarUrl());
                                    return createTransaction(transaction, uid, item.getIun())
                                            .map(entity -> item);
                                }).collectList().map(list -> transaction);
                },(transaction, transactionWithIuns) -> transactionWithIuns)
                .zipWhen(this::verifyCheckSum, (transaction, responseChecksum) -> transaction)
                .zipWhen(this::updateFileMetadata, (transaction, transactionUpdate) -> transactionUpdate)
                .map(transactionData -> StartTransactionResponseMapper.fromResult(transactionData.getUrls()))
                .onErrorResume(RaddGenericException.class, exception -> Mono.just(StartTransactionResponseMapper.fromException(exception)));
    }

    private TransactionData validationAorStartTransaction(String uid, AorStartTransactionRequest req){
        if (Strings.isBlank(req.getOperationId())){
            throw new PnInvalidInputException("Id operazione non valorizzato");
        }
        if (Strings.isBlank(req.getRecipientTaxId())){
            throw new PnInvalidInputException("Codice fiscale non valorizzato");
        }
        if (!Utils.checkPersonType(req.getRecipientType().getValue())){
            throw new PnInvalidInputException("Recipient Type non valorizzato correttamente");
        }
        return this.transactionDataMapper.toTransaction(uid, req);
    }

    private Flux<ResponsePaperNotificationFailedDtoDto> getIunFromPaperNotificationFailed(String recipientTaxId){
        return this.pnDeliveryPushClient.getPaperNotificationFailed(recipientTaxId)
                .filter(item -> StringUtils.equalsIgnoreCase(recipientTaxId, item.getRecipientInternalId()))
                .onErrorResume(NullPointerException.class, ex -> Mono.error(new RaddGenericException(ExceptionTypeEnum.NO_NOTIFICATIONS_FAILED, ExceptionCodeEnum.KO)));
    }

}
