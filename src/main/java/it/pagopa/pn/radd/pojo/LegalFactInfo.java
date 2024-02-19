package it.pagopa.pn.radd.pojo;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactCategoryDto;
import lombok.Data;

@Data
public class LegalFactInfo {
    String key;
    String url;
    String contentType;
    LegalFactCategoryDto category;
}
