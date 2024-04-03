package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.api.NormalizeAddressServiceApi;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.AcceptedResponseDto;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.pojo.AddressManagerRequest;
import it.pagopa.pn.radd.pojo.AddressManagerRequestAddress;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import it.pagopa.pn.radd.utils.RaddRegistryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PnAddressManagerClientTest {

    @Mock
    private NormalizeAddressServiceApi normalizeAddressServiceApi;

    @Mock
    private PnRaddFsuConfig pnRaddFsuConfig;

    private PnAddressManagerClient pnAddressManagerClient;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        pnAddressManagerClient = new PnAddressManagerClient(normalizeAddressServiceApi, pnRaddFsuConfig, new RaddRegistryUtils(new ObjectMapperUtil(new com.fasterxml.jackson.databind.ObjectMapper()), pnRaddFsuConfig));
    }

    @Test
    public void shouldNormalizeAddressesSuccessfully() {
        AddressManagerRequest request = new AddressManagerRequest();
        AddressManagerRequestAddress address = new AddressManagerRequestAddress();
        address.setId("id");
        request.setAddresses(List.of(address));
        when(normalizeAddressServiceApi.normalize(any(), any(), any())).thenReturn(Mono.just(new AcceptedResponseDto()));
        when(pnRaddFsuConfig.getAddressManagerApiKey()).thenReturn("testApiKey");

        Mono<AcceptedResponseDto> result = pnAddressManagerClient.normalizeAddresses(request);

        StepVerifier.create(result)
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();
    }

    @Test
    public void shouldHandleErrorWhenNormalizingAddresses() {
        AddressManagerRequest request = new AddressManagerRequest();
        AddressManagerRequestAddress address = new AddressManagerRequestAddress();
        address.setId("id");
        request.setAddresses(List.of(address));
        when(pnRaddFsuConfig.getAddressManagerApiKey()).thenReturn("testApiKey");
        when(normalizeAddressServiceApi.normalize(any(), any(), any())).thenReturn(Mono.error(new RuntimeException("Test exception")));

        Mono<AcceptedResponseDto> result = pnAddressManagerClient.normalizeAddresses(request);

        StepVerifier.create(result)
                .verifyError(RuntimeException.class);
    }
}
