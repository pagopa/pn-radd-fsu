package it.pagopa.pn.radd.middleware.queue.event;

import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.GenericEventHeader;
import lombok.*;

@Data
public class PnInternalCapCheckerEvent implements GenericEvent<GenericEventHeader, PnInternalCapCheckerEvent.Payload> {

    private GenericEventHeader header;

    private Payload payload;

    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private String zipCode;
    }
}
