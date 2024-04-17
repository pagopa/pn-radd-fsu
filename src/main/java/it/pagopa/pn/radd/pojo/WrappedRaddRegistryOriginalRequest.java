package it.pagopa.pn.radd.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WrappedRaddRegistryOriginalRequest {
    private RaddRegistryOriginalRequest request;
    private List<String> errors = new ArrayList<>();
}
