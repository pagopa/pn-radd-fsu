package it.pagopa.pn.radd.middleware.queue.producer;

import it.pagopa.pn.api.dto.events.GenericEventHeader;
import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.radd.pojo.RaddAltCapCheckerEvent;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface RaddAltCapCheckerProducer extends MomProducer<RaddAltCapCheckerEvent>  {

    default Mono<Void> sendCapCheckerEvent(String zipCode ) {
        RaddAltCapCheckerEvent capCheckerEvent = buildCapCheckerEvent(zipCode);
        return Mono.fromRunnable(() -> push(capCheckerEvent));
    }

    default RaddAltCapCheckerEvent buildCapCheckerEvent(String zipCode) {
        return RaddAltCapCheckerEvent.builder()
                .header( GenericEventHeader.builder()
                        .eventType( "CAP_CHECKER_EVENT" ) //Creare
                        .publisher( "RADD_ALT") //F
                        .eventId(UUID.randomUUID() + "_zipcode_checker")// IXME create RADD_ALT on EventPublisher
                        .createdAt( Instant.now() )
                        .build()
                )
                .payload( RaddAltCapCheckerEvent.Payload.builder()
                        .zipCode(zipCode)
                        .build()
                )
                .build();
    }
}
