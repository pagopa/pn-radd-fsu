package it.pagopa.pn.radd.middleware.queue.consumer;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.UUID;

import static it.pagopa.pn.radd.utils.Const.ERROR_CODE_RADD_ALT_EVENTTYPENOTSUPPORTED;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class PnEventInboundService {
    private final EventHandler eventHandler;
    private final PnRaddFsuConfig pnRaddFsuConfig;

    @Bean
    public MessageRoutingCallback customRouter() {
        return new MessageRoutingCallback() {
            @Override
            public FunctionRoutingResult routingResult(Message<?> message) {
                MessageHeaders messageHeaders = message.getHeaders();

                String traceId = null;
                String messageId = null;

                if (messageHeaders.containsKey("aws_messageId"))
                    messageId = messageHeaders.get("aws_messageId", String.class);
                if (messageHeaders.containsKey("X-Amzn-Trace-Id"))
                    traceId = messageHeaders.get("X-Amzn-Trace-Id", String.class);

                traceId = Objects.requireNonNullElseGet(traceId, () -> "traceId:" + UUID.randomUUID());
                MDCUtils.clearMDCKeys();
                MDC.put(MDCUtils.MDC_TRACE_ID_KEY, traceId);
                MDC.put(MDCUtils.MDC_PN_CTX_MESSAGE_ID, messageId);
                return new FunctionRoutingResult(handleMessage(message));
            }
        };
    }

    private String handleMessage(Message<?> message) {
        log.debug("Message received from customRouter {}", message);
        String eventType = (String) message.getHeaders().get("eventType");
        log.info("Message received from customRouter with eventType = {}", eventType );

        if(!StringUtils.hasText(eventType)) {
            eventType = handleOtherEvent(message);
        }

        String handlerName = eventHandler.getHandler().get(eventType);
        if (!StringUtils.hasText(handlerName)) {
            log.error("undefined handler for eventType={}", eventType);
        }

        log.debug("Handler for eventType={} is {}", eventType, handlerName);

        return handlerName;
    }

    private String handleOtherEvent(Message<?> message) {
        String eventType;
        String queueName = (String) message.getHeaders().get("aws_receivedQueue");
        if (Objects.equals(queueName, pnRaddFsuConfig.getSqs().getSafeStorageQueueName())) {
            eventType = "SAFE_STORAGE_EVENTS";
        }
        else if(Objects.equals(queueName, pnRaddFsuConfig.getSqs().getAddressManagerQueueName())) {
            eventType = "ADDRESS_MANAGER_EVENTS";
        }
        else {
            log.error("eventType not present, cannot start scheduled action headers={} payload={}", message.getHeaders(), message.getPayload());
            throw new PnInternalException("eventType not present, cannot start scheduled action", ERROR_CODE_RADD_ALT_EVENTTYPENOTSUPPORTED);
        }
        return eventType;
    }

}
