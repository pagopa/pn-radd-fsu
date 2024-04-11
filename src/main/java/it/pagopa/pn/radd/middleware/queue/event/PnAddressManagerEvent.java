package it.pagopa.pn.radd.middleware.queue.event;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({
        PnAddressManagerEvent.JSON_PROPERTY_CORRELATION_ID,
        PnAddressManagerEvent.JSON_PROPERTY_RESULT_ITEMS
})
@Data
public class PnAddressManagerEvent {
    public static final String JSON_PROPERTY_CORRELATION_ID = "correlationId";
    private String correlationId;

    public static final String JSON_PROPERTY_RESULT_ITEMS = "resultItems";
    private List<ResultItem> resultItems = new ArrayList<>();

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
