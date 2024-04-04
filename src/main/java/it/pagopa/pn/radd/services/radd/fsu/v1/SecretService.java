package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.radd.config.CachedSecretsManagerConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Optional;

import static it.pagopa.pn.radd.config.CachedSecretsManagerConsumer.ERROR_CODE_SECRET_MANAGER;
import static it.pagopa.pn.radd.config.CachedSecretsManagerConsumer.ERROR_MESSAGE_SECRET_MANAGER;


@Slf4j
@Component
@RequiredArgsConstructor
public class SecretService {

    private final CachedSecretsManagerConsumer cachedSecretsManagerConsumer;

    public String getSecret(String secretId) {
        Optional<GetSecretValueResponse> opt = cachedSecretsManagerConsumer.getSecretValue(secretId);
        if(opt.isEmpty()) {
            throw new PnInternalException(ERROR_MESSAGE_SECRET_MANAGER, ERROR_CODE_SECRET_MANAGER);
        }
        return opt.get().secretString();
    }
}
