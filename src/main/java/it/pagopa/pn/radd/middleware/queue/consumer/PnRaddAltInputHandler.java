package it.pagopa.pn.radd.middleware.queue.consumer;

import it.pagopa.pn.radd.middleware.queue.consumer.event.PnRaddAltNormalizeRequestEvent;
import it.pagopa.pn.radd.services.radd.fsu.v1.RaddAltInputService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.AbstractMap;
import java.util.function.Consumer;
@Configuration
@CustomLog
@RequiredArgsConstructor
public class PnRaddAltInputHandler {

    private final RaddAltInputService raddAltInputService;

    private static final String HANDLER_REQUEST = "pnRaddAltInputHandler";

    @Bean
    public Consumer<Message<PnRaddAltNormalizeRequestEvent.Payload>> pnRaddAltNormalizeRequestConsumer() {
        return message -> {
            log.logStartingProcess(HANDLER_REQUEST);
            log.debug(HANDLER_REQUEST + "- message: {}", message);
            MDC.put("correlationId", message.getPayload().getCorrelationId());
            raddAltInputService.handleRequest(message.getPayload())
                    .doOnSuccess(unused -> log.logEndingProcess(HANDLER_REQUEST))
                    .doOnError(throwable ->  {
                        log.logEndingProcess(HANDLER_REQUEST, false, throwable.getMessage());
                        HandleEventUtils.handleException(message.getHeaders(), throwable);
                    })
                    .block();
        };
    }

}
