package it.pagopa.pn.radd.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
public class AddressManagerRequest {

    private String correlationId;
    private List<AddressManagerRequestAddress> addresses;
}
