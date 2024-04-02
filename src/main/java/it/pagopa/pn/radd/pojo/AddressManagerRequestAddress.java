package it.pagopa.pn.radd.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressManagerRequestAddress {

    private String id;
    private String addressRow;
    private String cap;
    private String city;
    private String pr;
    private String country;
}
