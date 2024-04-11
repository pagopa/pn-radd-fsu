package it.pagopa.pn.radd.middleware.queue.producer;

import it.pagopa.pn.api.dto.events.EventPublisher;
import it.pagopa.pn.api.dto.events.GenericEventHeader;
import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.radd.middleware.queue.event.RegistryImportProgressEvent;

import java.time.Instant;

import static it.pagopa.pn.radd.utils.Const.IMPORT_COMPLETED;

public interface RegistryImportProgressProducer extends MomProducer<RegistryImportProgressEvent> {
    default void sendRegistryImportCompletedEvent(String cxId, String requestId) {
        RegistryImportProgressEvent event = buildNotification(cxId, requestId);
        this.push(event);
    }

    default RegistryImportProgressEvent buildNotification(String cxId, String requestId) {
        return RegistryImportProgressEvent.builder()
                .header(GenericEventHeader.builder()
                        .publisher(EventPublisher.RADD_ALT.name())
                        .createdAt(Instant.now())
                        .eventId(requestId)
                        .eventType(IMPORT_COMPLETED)
                        .build()
                )
                .payload(RegistryImportProgressEvent.Payload.builder()
                        .cxId(cxId)
                        .requestId(requestId)
                        .build()
                )
                .build();
    }
}
