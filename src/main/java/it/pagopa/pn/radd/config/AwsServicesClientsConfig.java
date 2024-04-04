package it.pagopa.pn.radd.config;

import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AwsServicesClientsConfig {

    private final AwsConfigs awsConfigs;

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.of(awsConfigs.getRegionCode()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

}
