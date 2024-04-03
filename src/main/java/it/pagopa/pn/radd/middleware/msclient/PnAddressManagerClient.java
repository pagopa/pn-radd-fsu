package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.api.NormalizeAddressServiceApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.AcceptedResponseDto;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;
import it.pagopa.pn.radd.pojo.AddressManagerRequest;
import it.pagopa.pn.radd.utils.RaddRegistryUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@AllArgsConstructor
@Component
@CustomLog
public class PnAddressManagerClient extends BaseClient {

    private final NormalizeAddressServiceApi normalizeAddressServiceApi;
    private final PnRaddFsuConfig pnRaddFsuConfig;
    private final RaddRegistryUtils raddRegistryUtils;
    protected static final String PN_ADDRESS_MANAGER_CX_ID_VALUE = "pn-radd-alt";
    public Mono<AcceptedResponseDto> normalizeAddresses(AddressManagerRequest request) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_ADDRESS_MANAGER, "normalize");
        return normalizeAddressServiceApi.normalize(PN_ADDRESS_MANAGER_CX_ID_VALUE, pnRaddFsuConfig.getAddressManagerApiKey(), raddRegistryUtils.getNormalizeRequestDtoFromAddressManagerRequest(request))
                .doOnError(ex -> log.logInvokationResultDownstreamFailed(PnLogger.EXTERNAL_SERVICES.PN_ADDRESS_MANAGER, ex.getMessage()));
    }
}
