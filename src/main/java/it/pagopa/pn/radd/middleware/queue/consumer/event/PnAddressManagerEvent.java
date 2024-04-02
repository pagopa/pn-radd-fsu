package it.pagopa.pn.radd.middleware.queue.consumer.event;

import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import lombok.*;

import java.util.List;

@Data
public class PnAddressManagerEvent implements GenericEvent<StandardEventHeader, PnAddressManagerEvent.Payload> {

    private StandardEventHeader header;

    private Payload payload;

    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private String correlationId;
        private List<ResultItem> resultItems;
        private String cxId;
    }

    @Data
    public static class ResultItem {
        private String id;
        private NormalizedAddress normalizedAddress;
        private String error;
    }

    @Data
    public static class NormalizedAddress {
        private String addressRow;
        private String addressRow2;
        private String cap;
        private String city;
        private String city2;
        private String pr;
        private String country;
        private String nameRow2;
    }
}
