package it.pagopa.pn.radd.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "pn.radd")
@Import(SharedAutoConfiguration.class)
public class PnRaddFsuConfig {

    private String clientDeliveryBasepath;
    private String clientDeliveryPushBasepath;
    private String clientDeliveryPushInternalBasepath;
    private String clientDatavaultBasepath;
    private String clientSafeStorageBasepath;
    private String safeStorageCxId;
    private String safeStorageDocType;
}
