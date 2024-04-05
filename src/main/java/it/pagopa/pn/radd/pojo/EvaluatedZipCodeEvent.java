package it.pagopa.pn.radd.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.pn.api.dto.events.GenericEventBridgeEvent;
import lombok.*;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class EvaluatedZipCodeEvent implements GenericEventBridgeEvent<EvaluatedZipCodeEvent.Detail> {

    private Detail detail;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder(toBuilder = true)
    @ToString
    @EqualsAndHashCode
    public static class Detail {
        @JsonProperty("configKey")
        private String configKey;

        @JsonProperty("configType")
        private String configType = "ZIPCODE";

        @JsonProperty("configs")
        private List<ConfigEntry> configs;
    }
}
