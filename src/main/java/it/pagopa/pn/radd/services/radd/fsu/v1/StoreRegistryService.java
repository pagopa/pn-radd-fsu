package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.StoreRegistriesResponse;
import it.pagopa.pn.radd.middleware.db.RaddRegistryDAO;
import it.pagopa.pn.radd.utils.RaddRegistryUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@CustomLog
public class StoreRegistryService {

    private final RaddRegistryDAO raddRegistryDAO;
    private final RaddRegistryUtils raddRegistryUtils;


    public Mono<StoreRegistriesResponse> retrieveStoreRegistries(Integer limit, String lastKey) {
        return raddRegistryDAO.scanRegistries(limit, lastKey)
                .map(raddRegistryUtils::mapToStoreRegistriesResponse);
    }

}
