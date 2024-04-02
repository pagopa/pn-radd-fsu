package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.api.NormalizeAddressServiceApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.AcceptedResponseDto;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.mapper.AddressManagerOriginalRequestMapper;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import it.pagopa.pn.radd.pojo.AddressManagerRequest;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

import static it.pagopa.pn.radd.constant.AddressManagerConstant.POSTEL;


@AllArgsConstructor
@Component
@CustomLog
public class PnAddressManagerClient extends BaseClient {

    private final NormalizeAddressServiceApi normalizeAddressServiceApi;

    private final PnRaddFsuConfig pnRaddFsuConfig;


    public Mono<AcceptedResponseDto> normalizeAddresses(AddressManagerRequest request, List<RaddRegistryRequestEntity> listEntities) {
        log.trace("ENSURE FISCAL CODE TICK {}", new Date().getTime());
        return normalizeAddressServiceApi.normalize("", "", AddressManagerOriginalRequestMapper.getNormalizeRequestDtoFromAddressManagerRequest(request))
                .map(item -> {
                    log.trace("ENSURE FISCAL CODE TOCK {}", new Date().getTime());
                    return item;
                })
                .doOnError(ex -> log.logInvokationResultDownstreamFailed(POSTEL, ex.getMessage()));
    }
}
