package it.pagopa.pn.radd.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileDownloadResponseDto;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.queue.consumer.HandleEventUtils;
import it.pagopa.pn.radd.services.radd.fsu.v1.SafeStorageEventService;
import lombok.CustomLog;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Configuration
@CustomLog
public class SafeStorageEventHandler {

    private final PnRaddFsuConfig pnRaddFsuConfig;

    private final SafeStorageEventService safeStorageEventService;

    private static final String HANDLER_REQUEST = "pnSafeStorageEventInboundConsumer";


    public SafeStorageEventHandler(PnRaddFsuConfig pnRaddFsuConfig, SafeStorageEventService safeStorageEventService) {
        this.pnRaddFsuConfig = pnRaddFsuConfig;
        this.safeStorageEventService = safeStorageEventService;
    }

    @Bean
    public Consumer<Message<FileDownloadResponseDto>> pnSafeStorageEventInboundConsumer() {
        return message -> {
            log.debug("Handle message from {} with content {}", PnLogger.EXTERNAL_SERVICES.PN_SAFE_STORAGE, message);
            FileDownloadResponseDto response = message.getPayload();
            MDC.put(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY, response.getKey());
            if (pnRaddFsuConfig.getRegistrySafeStorageDocType().equals(response.getDocumentType())) {
                Mono<Void> handledMessage = safeStorageEventService.handleSafeStorageResponse(response)
                        .doOnSuccess(unused -> {
                            MDC.remove(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY);
                            log.logEndingProcess(HANDLER_REQUEST);
                        })
                        .doOnError(throwable -> {
                            log.logEndingProcess(HANDLER_REQUEST, false, throwable.getMessage());
                            MDC.remove(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY);
                            HandleEventUtils.handleException(message.getHeaders(), throwable);
                        });
                MDCUtils.addMDCToContextAndExecute(handledMessage).block();
            } else {
                log.debug("Safe storage event received is not handled - documentType={}", response.getDocumentType());
            }
        };
    }
}
