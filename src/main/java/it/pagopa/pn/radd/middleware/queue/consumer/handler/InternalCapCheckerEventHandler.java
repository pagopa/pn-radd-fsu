package it.pagopa.pn.radd.middleware.queue.consumer.handler;

import it.pagopa.pn.radd.middleware.queue.consumer.HandleEventUtils;
import it.pagopa.pn.radd.middleware.queue.event.PnInternalCapCheckerEvent;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@AllArgsConstructor
@CustomLog
public class InternalCapCheckerEventHandler {

    private RegistryService registryService;
    private static final String HANDLER_REQUEST = "pnInternalCapCheckerEventInboundConsumer";
    @Bean
    public Consumer<Message<PnInternalCapCheckerEvent>> pnInternalCapCheckerEventInboundConsumer() {
        return message -> {
            log.debug("Handle message from {} with content {}", "Internal Cap Checker", message);
            PnInternalCapCheckerEvent response = message.getPayload();

            registryService.handleInternalCapCheckerMessage(response)
                    .doOnSuccess(unused -> log.logEndingProcess(HANDLER_REQUEST))
                    .doOnError(throwable ->  {
                        log.logEndingProcess(HANDLER_REQUEST, false, throwable.getMessage());
                        HandleEventUtils.handleException(message.getHeaders(), throwable);
                    })
                    .block();
        };
    }
   
}
