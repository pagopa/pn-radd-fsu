package it.pagopa.pn.radd.middleware.queue.consumer.handler;


import it.pagopa.pn.radd.middleware.queue.consumer.AddressManagerRequestHandler;
import it.pagopa.pn.radd.pojo.PnAddressManagerRequestDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@AllArgsConstructor
@Slf4j
public class AddressManagerEventHandler {
    private AddressManagerRequestHandler handler;
    private static final String HANDLER_REQUEST = "pnAddressManagerEventInboundConsumer";
    @Bean
    public Consumer<Message<PnAddressManagerRequestDTO>> pnAddressManagerEventInboundConsumer() {
        return message -> {
            try {
                log.debug("Handle message from {} with content {}", "Address Manager", message);
                PnAddressManagerRequestDTO response = message.getPayload();

                handler.handleMessage(response)
                        .doOnSuccess(unused -> log.debug(HANDLER_REQUEST))
                        .doOnError(throwable ->  {
                            log.error(HANDLER_REQUEST, throwable.getMessage());
                        })
                        .block();
            } catch (Exception ex) {
                throw ex;
            }
        };
    }
   
}
