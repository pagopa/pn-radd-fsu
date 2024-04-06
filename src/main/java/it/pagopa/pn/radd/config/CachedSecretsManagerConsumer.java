package it.pagopa.pn.radd.config;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class CachedSecretsManagerConsumer {

    public static final String ERROR_MESSAGE_SECRET_MANAGER = "Secret not found";
    public static final String ERROR_CODE_SECRET_MANAGER = "PN_RADD_ALT_SECRET_MANAGER";


    private final SecretsManagerClient secretsManagerClient;
    private final Duration cacheExpiration = Duration.of(5, ChronoUnit.MINUTES);

    public CachedSecretsManagerConsumer(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }

    private final ConcurrentHashMap<String, ExpiringValue> valueCache = new ConcurrentHashMap<>();

    public <T> Optional<T> getSecretValue(String secretId) {
        if (!StringUtils.hasText(secretId)) {
            log.warn("missing secret name or ARN");
            return Optional.empty();
        }

        Object optValue = valueCache.computeIfAbsent(secretId, key -> new ExpiringValue())
                .getValueCheckTimestamp();
        if (optValue == null) {
            log.debug("Required secret is not in cache");
            optValue = getSecret(secretId);
            valueCache.put(secretId, new ExpiringValue(optValue, cacheExpiration));
        }
        return (Optional<T>) Optional.of(optValue);
    }


    public GetSecretValueResponse getSecret(String secretId) {
        GetSecretValueRequest secretValueRequest = GetSecretValueRequest.builder().secretId(secretId).build();
        try {
            long startTime = System.currentTimeMillis();
            log.info("START - SecretsManager.getSecretValue");
            GetSecretValueResponse secretValueResponse = secretsManagerClient.getSecretValue(secretValueRequest);
            log.info("END - SecretsManager.getSecretValue Response OK Timelapse: {} ms", System.currentTimeMillis() - startTime);
            return secretValueResponse;
        } catch (Exception e) {
            throw new PnInternalException(ERROR_MESSAGE_SECRET_MANAGER, ERROR_CODE_SECRET_MANAGER, e);
        }
    }

    @Value
    private static class ExpiringValue {
        Object value;
        Instant timestamp;

        private ExpiringValue(){
            this.value = null;
            this.timestamp = Instant.EPOCH;
        }

        public ExpiringValue(Object value, Instant cacheExpiration) {
            this.value = value;
            this.timestamp = cacheExpiration;
        }

        public ExpiringValue(Object value, Duration cacheExpiration) {
            this (value, Instant.now().plus( cacheExpiration ));
        }

        public Object getValueCheckTimestamp() {
            Object result = null;
            if ( Instant.now().isBefore( timestamp ) ){
                result = value;
            }
            return result;
        }
    }
}

