package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.radd.config.CachedSecretsManagerConsumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecretManagerServiceTest {
    @InjectMocks
    SecretService secretManagerService;

    @Mock
    SecretsManagerClient secretsManagerClient;

    @Mock
    CachedSecretsManagerConsumer cachedSecretsManagerConsumer;

    @Test
    void getSecretValue() {
        GetSecretValueResponse response = GetSecretValueResponse.builder().secretString("test").build();
        when(cachedSecretsManagerConsumer.getSecretValue("test")).thenReturn(Optional.of(response));
        Assertions.assertEquals("test", secretManagerService.getSecret("test"));
    }

    @Test
    void getSecretValueThrow() {
        when(cachedSecretsManagerConsumer.getSecretValue("test")).thenReturn(Optional.empty());
        Assertions.assertThrows(PnInternalException.class,() -> secretManagerService.getSecret("test"));
    }
}
