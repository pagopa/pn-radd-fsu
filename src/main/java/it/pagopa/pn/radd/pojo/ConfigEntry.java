package it.pagopa.pn.radd.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
public class ConfigEntry {
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @JsonProperty("startValidity")
    private Instant startValidity;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @JsonProperty("endValidity")
    private Instant endValidity;
}