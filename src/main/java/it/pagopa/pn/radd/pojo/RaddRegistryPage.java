package it.pagopa.pn.radd.pojo;

import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryEntityV2;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class RaddRegistryPage {

    private List<RaddRegistryEntityV2> items;
    private String lastKey;
}
