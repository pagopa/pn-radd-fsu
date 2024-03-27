package it.pagopa.pn.radd.middleware.db.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/**
 * Questa entità mappa la tabella pn-operations-iuns. Viene utilizzata solo dal caso d'uso AOR.
 */
@DynamoDbBean
@Getter
@Setter
@ToString
@NoArgsConstructor
public class PnAnagraficheRaddImportEntity {

    public static final String COL_CX_ID = "cxId";
    public static final String COL_REQUEST_ID = "requestId";
    public static final String COL_FILE_KEY = "fileKey";
    public static final String COL_CHECKSUM = "checksum";
    public static final String COL_STATUS = "status";
    public static final String COL_ERROR = "error";
    public static final String COL_CONFIG = "config";
    public static final String COL_TTL = "ttl";

    public static final String STATUS_INDEX = "status-index";
    public static final String FILE_KEY_INDEX = "fileKey-index";

    @Getter(onMethod = @__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_CX_ID)}))
    private String cxId;

    @Getter(onMethod = @__({@DynamoDbSortKey, @DynamoDbAttribute(COL_REQUEST_ID)}))
    private String requestId;

    @Getter(onMethod = @__({@DynamoDbSecondaryPartitionKey(indexNames = FILE_KEY_INDEX), @DynamoDbAttribute(COL_FILE_KEY)}))
    private String fileKey;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CHECKSUM)}))
    private String checksum;

    @Getter(onMethod = @__({@DynamoDbSecondaryPartitionKey(indexNames = STATUS_INDEX), @DynamoDbAttribute(COL_STATUS)}))
    private  String status;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_ERROR)}))
    private String error;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CONFIG)}))
    private String config;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_TTL)}))
    private Long ttl;
}
