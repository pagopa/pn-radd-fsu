package it.pagopa.pn.radd.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PnAddressManagerRequestDTO {


    @JsonProperty("body")
    private Body body;

    @JsonProperty("cxId")
    private String cxId;


    @Data
    public class Body {
        @JsonProperty("correlationId")
        private String correlationId;

        @JsonProperty("resultItems")
        private List<ResultItem> resultItems;
    }

    @Data
    public class ResultItem {
        @JsonProperty("id")
        private String id;

        @JsonProperty("normalizedAddress")
        private NormalizedAddress normalizedAddress;

        @JsonProperty("error")
        private String error;
    }

    @Data
    public class NormalizedAddress {
        @JsonProperty("addressRow")
        private String addressRow;

        @JsonProperty("addressRow2")
        private String addressRow2;

        @JsonProperty("cap")
        private String cap;

        @JsonProperty("city")
        private String city;

        @JsonProperty("city2")
        private String city2;

        @JsonProperty("pr")
        private String pr;

        @JsonProperty("country")
        private String country;

        @JsonProperty("nameRow2")
        private String nameRow2;
    }
}
