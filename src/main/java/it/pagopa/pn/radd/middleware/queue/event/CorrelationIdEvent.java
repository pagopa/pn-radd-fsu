package it.pagopa.pn.radd.middleware.queue.event;

import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class CorrelationIdEvent implements GenericEvent<StandardEventHeader, CorrelationIdEvent.Payload> {
    private StandardEventHeader header;
    private Payload payload;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder(toBuilder = true)
    @EqualsAndHashCode
    @ToString
    public static class Payload {

        @NotEmpty
        private String correlationId;

    }
}
