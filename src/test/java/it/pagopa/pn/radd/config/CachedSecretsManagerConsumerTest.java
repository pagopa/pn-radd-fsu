package it.pagopa.pn.radd.config;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {CachedSecretsManagerConsumer.class})
@ExtendWith(SpringExtension.class)
class CachedSecretsManagerConsumerTest {
    @Autowired
    private CachedSecretsManagerConsumer cachedSecretsManagerConsumer;

    @MockBean
    private SecretsManagerClient secretsManagerClient;


    /**
     * Method under test: {@link CachedSecretsManagerConsumer#getSecretValue(String)}
     */
    @Test
    void testGetSecretValue2() throws AwsServiceException, SdkClientException {
        when(secretsManagerClient.getSecretValue(Mockito.<GetSecretValueRequest>any()))
                .thenThrow(new PnInternalException("An error occurred"));
        assertThrows(PnInternalException.class, () -> cachedSecretsManagerConsumer.getSecretValue("42"));
        verify(secretsManagerClient).getSecretValue(Mockito.<GetSecretValueRequest>any());
    }

    /**
     * Method under test: {@link CachedSecretsManagerConsumer#getSecretValue(String)}
     */
    @Test
    void testGetSecretValue3() throws AwsServiceException, SdkClientException {
        when(secretsManagerClient.getSecretValue(Mockito.<GetSecretValueRequest>any()))
                .thenThrow(new PnInternalException("An error occurred"));
        assertThrows(PnInternalException.class,
                () -> cachedSecretsManagerConsumer.getSecretValue("Value for {} not in cache"));
        verify(secretsManagerClient).getSecretValue(Mockito.<GetSecretValueRequest>any());
    }

    /**
     * Method under test: {@link CachedSecretsManagerConsumer#getSecretValue(String)}
     */
    @Test
    void testGetSecretValue4() throws AwsServiceException, SdkClientException {
        when(secretsManagerClient.getSecretValue(Mockito.<GetSecretValueRequest>any()))
                .thenThrow(new PnInternalException("An error occurred"));
        assertFalse(cachedSecretsManagerConsumer.getSecretValue(null).isPresent());
    }

    /**
     * Method under test: {@link CachedSecretsManagerConsumer#getSecret(String)}
     */
    @Test
    void testGetSecret() throws AwsServiceException, SdkClientException {
        when(secretsManagerClient.getSecretValue(Mockito.<GetSecretValueRequest>any())).thenReturn(null);
        assertNull(cachedSecretsManagerConsumer.getSecret("42"));
        verify(secretsManagerClient).getSecretValue(Mockito.<GetSecretValueRequest>any());
    }

    /**
     * Method under test: {@link CachedSecretsManagerConsumer#getSecret(String)}
     */
    @Test
    void testGetSecret2() throws AwsServiceException, SdkClientException {
        when(secretsManagerClient.getSecretValue(Mockito.<GetSecretValueRequest>any()))
                .thenThrow(new PnInternalException("An error occurred"));
        assertThrows(PnInternalException.class, () -> cachedSecretsManagerConsumer.getSecret("42"));
        verify(secretsManagerClient).getSecretValue(Mockito.<GetSecretValueRequest>any());
    }
}
