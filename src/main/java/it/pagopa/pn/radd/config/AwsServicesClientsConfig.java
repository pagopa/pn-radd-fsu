package it.pagopa.pn.radd.config;

import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.geoplaces.GeoPlacesAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AwsServicesClientsConfig {

    @Value("${aws.geo.region-code}")
    private String awsGeoRegionCode;

    private final AwsConfigs awsConfigs;

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                                   .region(Region.of(awsConfigs.getRegionCode()))
                                   .credentialsProvider(DefaultCredentialsProvider.create())
                                   .build();
    }


    @Bean
    public GeoPlacesAsyncClient geoPlacesAsyncClient() {
        return GeoPlacesAsyncClient.builder()
                                   .region(Region.of(awsGeoRegionCode))
                                   .credentialsProvider(DefaultCredentialsProvider.create())
                                   .build();
    }

}
