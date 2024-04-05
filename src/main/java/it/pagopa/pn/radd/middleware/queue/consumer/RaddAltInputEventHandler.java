package it.pagopa.pn.radd.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.radd.middleware.queue.consumer.HandleEventUtils;
import it.pagopa.pn.radd.middleware.queue.event.PnRaddAltNormalizeRequestEvent;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;
@Configuration
@CustomLog
@RequiredArgsConstructor
public class RaddAltInputEventHandler {

    private final RegistryService registryService;

    private static final String HANDLER_NORMALIZE_REQUEST = "pnRaddAltInputNormalizeRequestConsumer";

    @Bean
    public Consumer<Message<PnRaddAltNormalizeRequestEvent.Payload>> pnRaddAltInputNormalizeRequestConsumer() {
        return message -> {
            log.logStartingProcess(HANDLER_NORMALIZE_REQUEST);
            log.debug(HANDLER_NORMALIZE_REQUEST + "- message: {}", message);
            MDC.put(MDCUtils.MDC_PN_CTX_REQUEST_ID, message.getPayload().getCorrelationId());
            var monoResult = registryService.handleNormalizeRequestEvent(message.getPayload())
                    .doOnSuccess(unused -> log.logEndingProcess(HANDLER_NORMALIZE_REQUEST))
                    .doOnError(throwable ->  {
                        log.logEndingProcess(HANDLER_NORMALIZE_REQUEST, false, throwable.getMessage());
                        HandleEventUtils.handleException(message.getHeaders(), throwable);
                    });

            MDCUtils.addMDCToContextAndExecute(monoResult).block();
        };
    }

}
