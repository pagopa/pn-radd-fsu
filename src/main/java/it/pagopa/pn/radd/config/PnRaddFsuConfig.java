package it.pagopa.pn.radd.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.radd.pojo.DocumentTypeEnum;
import it.pagopa.pn.radd.utils.HtmlSanitizer;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "pn.radd")
@Import(SharedAutoConfiguration.class)
public class PnRaddFsuConfig {
    private Integer attemptBatchWriter;
    private String clientDeliveryBasepath;
    private String clientDeliveryPushBasepath;
    private String clientDeliveryPushInternalBasepath;
    private String clientDatavaultBasepath;
    private String clientSafeStorageBasepath;
    private String safeStorageCxId;
    private String safeStorageDocType;
    public String registrySafeStorageDocType;
    private String applicationBasepath;
    private Sqs sqs;
    private Dao dao;
    private int registryDefaultEndValidity;
    private String registryDefaultDeleteRule;
    private EventBus eventBus;
    private String addressManagerApiKeySecret;
    private String addressManagerBaseUrl;

    private HtmlSanitizer.SanitizeMode sanitizeMode;
    private List<DocumentTypeEnum> documentTypeEnumFilter = new ArrayList<>();

    private Long registryImportUploadFileTtl;

    private String evaluatedZipCodeConfigType;
    private Integer evaluatedZipCodeConfigNumber;

    private RegistryImportProgress registryImportProgress;

    @Data
    public static class RegistryImportProgress {
        private Integer delay;
        private Integer lockAtMost;
        private Integer lockAtLeast;
    }

    private Integer maxQuerySize;

    @Data
    public static class Dao {
        private String raddTransactionTable;
        private String iunsOperationsTable;
        private String raddRegistryRequestTable;
        private String raddRegistryImportTable;
        private String raddRegistryTable;
        private String shedlockTableName;
    }

    @Data
    public static class Sqs {
        private String internalCapCheckerQueueName;
        private String inputQueueName;
        private String safeStorageQueueName;
        private String addressManagerQueueName;
    }

    @Data
    public static class EventBus {
        private String name;
        private String detailType;
        private String source;
    }
}
