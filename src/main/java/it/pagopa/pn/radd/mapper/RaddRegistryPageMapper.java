package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.GetRegistryResponseV2;
import it.pagopa.pn.radd.alt.generated.openapi.server.v2.dto.RegistryV2;
import it.pagopa.pn.radd.pojo.RaddRegistryPage;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
@CustomLog
public class RaddRegistryPageMapper {

    private  final RaddRegistryMapper raddRegistryMapper;

    public GetRegistryResponseV2 toDto(RaddRegistryPage page) {
        if (page == null) {
            return null;
        }

        List<RegistryV2> registryList = new ArrayList<>();
        page.getItems().forEach(i -> registryList.add(raddRegistryMapper.toDto(i)));

        GetRegistryResponseV2 dto = new GetRegistryResponseV2();
        dto.setItems(registryList);
        dto.setLastKey(page.getLastKey());

        return dto;
    }

}