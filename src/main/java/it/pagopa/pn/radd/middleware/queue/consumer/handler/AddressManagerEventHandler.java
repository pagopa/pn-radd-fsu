package it.pagopa.pn.radd.middleware.queue.consumer.handler;


import it.pagopa.pn.radd.middleware.queue.consumer.AddressManagerRequestHandler;
import it.pagopa.pn.radd.middleware.queue.consumer.HandleEventUtils;
import it.pagopa.pn.radd.middleware.queue.consumer.event.PnAddressManagerEvent;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@AllArgsConstructor
@CustomLog
public class AddressManagerEventHandler {
    private AddressManagerRequestHandler handler;
    private static final String HANDLER_REQUEST = "pnAddressManagerEventInboundConsumer";
    @Bean
    public Consumer<Message<PnAddressManagerEvent>> pnAddressManagerEventInboundConsumer() {
        return message -> {
            log.debug("Handle message from {} with content {}", "Address Manager", message);
            PnAddressManagerEvent response = message.getPayload();

            handler.handleMessage(response)
                    .doOnSuccess(unused -> log.logEndingProcess(HANDLER_REQUEST))
                    .doOnError(throwable ->  {
                        log.logEndingProcess(HANDLER_REQUEST, false, throwable.getMessage());
                        HandleEventUtils.handleException(message.getHeaders(), throwable);
                    })
                    .block();
        };
    }
   
}
