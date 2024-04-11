package it.pagopa.pn.radd.middleware.queue.producer;

import it.pagopa.pn.api.dto.events.EventPublisher;
import it.pagopa.pn.api.dto.events.GenericEventHeader;
import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.radd.middleware.queue.event.CorrelationIdEvent;

import java.time.Instant;
import java.util.UUID;

import static it.pagopa.pn.radd.utils.Const.RADD_NORMALIZE_REQUEST;

public interface CorrelationIdEventsProducer extends MomProducer<CorrelationIdEvent> {

    default void sendCorrelationIdEvent(String correlationId) {
        CorrelationIdEvent correlationIdEvent = buildCorrelationIdEvent(correlationId);
        this.push(correlationIdEvent);
    }

    default CorrelationIdEvent buildCorrelationIdEvent(String correlationId) {
        return CorrelationIdEvent.builder()
                .header(GenericEventHeader.builder()
                        .eventType(RADD_NORMALIZE_REQUEST)
                        .eventId(UUID.randomUUID().toString())
                        .publisher(EventPublisher.RADD_ALT.name())
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
