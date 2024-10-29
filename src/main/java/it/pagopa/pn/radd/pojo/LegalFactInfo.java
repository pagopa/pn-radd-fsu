package it.pagopa.pn.radd.pojo;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactCategoryV20Dto;
import lombok.Data;

@Data
public class LegalFactInfo {
    String key;
    String url;
    String contentType;
    LegalFactCategoryV20Dto category;
}
