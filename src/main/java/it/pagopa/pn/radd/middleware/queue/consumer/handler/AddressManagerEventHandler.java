package it.pagopa.pn.radd.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.radd.middleware.queue.consumer.HandleEventUtils;
import it.pagopa.pn.radd.middleware.queue.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@AllArgsConstructor
@CustomLog
public class AddressManagerEventHandler {

    private RegistryService registryService;
    private static final String HANDLER_REQUEST = "pnAddressManagerEventInboundConsumer";
    @Bean
    public Consumer<Message<PnAddressManagerEvent>> pnAddressManagerEventInboundConsumer() {
        return message -> {
            log.debug("Handle message from {} with content {}", "Address Manager", message);
            PnAddressManagerEvent response = message.getPayload();
            MDC.put(MDCUtils.MDC_PN_CTX_REQUEST_ID, response.getPayload().getCorrelationId());
            var monoResult = registryService.handleAddressManagerEvent(response)
                    .doOnSuccess(unused -> log.logEndingProcess(HANDLER_REQUEST))
                    .doOnError(throwable ->  {
                        log.logEndingProcess(HANDLER_REQUEST, false, throwable.getMessage());
                        HandleEventUtils.handleException(message.getHeaders(), throwable);
                    });

            MDCUtils.addMDCToContextAndExecute(monoResult).block();
        };
    }
   
}
