package it.pagopa.pn.radd.middleware.queue.producer;

import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.radd.middleware.queue.event.CorrelationIdEvent;

import java.time.Instant;

public interface CorrelationIdEventsProducer extends MomProducer<CorrelationIdEvent> {

    default void sendCorrelationIdEvent(String correlationId) {
        CorrelationIdEvent correlationIdEvent = buildCorrelationIdEvent(correlationId);
        this.push(correlationIdEvent);
    }

    default CorrelationIdEvent buildCorrelationIdEvent(String correlationId) {
        return CorrelationIdEvent.builder()
                .header(StandardEventHeader.builder()
                        .eventType("RADD_NORMALIZE_REQUEST")
                        .publisher("RADD_ALT") //TODO replace with EventPublisher.RADD_ALT.name() when it will be available
                        .createdAt(Instant.now())
                        .build()
                )
                .payload(CorrelationIdEvent.Payload.builder()
                        .correlationId(correlationId)
                        .build()
                )
                .build();
    }
}
