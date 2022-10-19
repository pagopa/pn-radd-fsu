package it.pagopa.pn.radd.middleware.db.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("aws")
public class AwsConfigs {

    private String profileName;
    private String regionCode;
    private String bucketName;
    private String endpointUrl;
    private Boolean useAwsKeyspace;

    private String accessKeyId;
    private String secretAccessKey;

    private String dynamodbTable;
    private String dynamodbTableHistory;
    private String dynamodbIunsoperationsTable;
}