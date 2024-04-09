package it.pagopa.pn.radd.middleware.queue.event;

import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

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
        private static final int CXID_POSITION = 0;
        private static final int REQUESTID_POSITION = 1;
        private static final int INDEX_POSITION = 2;
        private static final String ITEMS_SEPARATOR = "#";

        private String id;
        private NormalizedAddress normalizedAddress;
        private String error;

        public static String[] splitId(String id) {
            return id.split(ITEMS_SEPARATOR);
        }
        public static String retrieveCxIdFromId(String id) {
            String[] idItems = splitId(id);
            return idItems.length == 3 ? idItems[CXID_POSITION] : StringUtils.EMPTY;
        }

        public static String retrieveRequestIdFromId(String id) {
            String[] idItems = splitId(id);
            return idItems.length == 3 ? idItems[REQUESTID_POSITION] : StringUtils.EMPTY;
        }

        public String retrieveIndexFromId(String id) {
            String[] idItems = splitId(id);
            return idItems.length == 3 ? idItems[INDEX_POSITION] : StringUtils.EMPTY;
        }
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
