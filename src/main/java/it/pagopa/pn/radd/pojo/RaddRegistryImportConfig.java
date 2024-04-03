package it.pagopa.pn.radd.pojo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RaddRegistryImportConfig {
    @JsonProperty("defaultEndValidity")
    private int defaultEndValidity;

    @JsonProperty("deleteRole")
    private String deleteRole;
}
