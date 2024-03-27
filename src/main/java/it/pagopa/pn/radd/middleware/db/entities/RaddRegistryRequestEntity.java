package it.pagopa.pn.radd.middleware.db.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/**
 * Questa entità mappa la tabella pn-richiesteRadd.
 */
@DynamoDbBean
@Getter
@Setter
@ToString
@NoArgsConstructor
public class RaddRegistryRequestEntity {
    public static final String ITEMS_SEPARATOR = "#";

    public static final String COL_PK = "pk";
    public static final String COL_REQUEST_ID = "requestId";
    public static final String COL_CORRELATION_ID= "correlationId";
    public static final String COL_CREATED_AT = "createdAt";
    public static final String COL_UPDATED_AT = "updatedAt";
    public static final String COL_ORIGINAL_REQUEST = "originalRequest";
    public static final String COL_ZIP_CODE = "zipCode";
    public static final String COL_STATUS = "status";
    public static final String COL_ERROR = "error";
    public static final String COL_CX_ID = "cxId";
    public static final String COL_REGISTRY_ID = "registryId";

    public static final String CXID_REQUESTID_INDEX = "cxId-requestId-index";
    public static final String CORRELATIONID_INDEX = "correlationId-index";
    public static final String CXID_REGISTRYID_INDEX = "cxId-registryId-index";

    private static final int INDEX_POSITION = 2;


    @Getter(onMethod = @__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)}))
    private String pk;
    @Getter(onMethod = @__({@DynamoDbSecondarySortKey(indexNames = CXID_REQUESTID_INDEX), @DynamoDbAttribute(COL_REQUEST_ID)}))
    private String requestId;
    @Getter(onMethod = @__({@DynamoDbSecondaryPartitionKey(indexNames = CORRELATIONID_INDEX), @DynamoDbAttribute(COL_CORRELATION_ID)}))
    private String correlationId;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CREATED_AT)}))
    private String createdAt;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_UPDATED_AT)}))
    private String updatedAt;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_ORIGINAL_REQUEST)}))
    private String originalRequest;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_ZIP_CODE)}))
    private String zipCode;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_STATUS)}))
    private String status;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_ERROR)}))
    private String error;
    @Getter(onMethod = @__({@DynamoDbSecondaryPartitionKey(indexNames = CXID_REQUESTID_INDEX), @DynamoDbSecondaryPartitionKey(indexNames = CXID_REGISTRYID_INDEX), @DynamoDbAttribute(COL_CX_ID)}))
    private String cxId;
    @Getter(onMethod = @__({@DynamoDbSecondarySortKey(indexNames = CXID_REQUESTID_INDEX), @DynamoDbAttribute(COL_REGISTRY_ID)}))
    private String registryId;

    @DynamoDbIgnore
    private String retrieveIndexFromPk() {
        return this.getPk().split(ITEMS_SEPARATOR).length == 2 ? this.getPk().split(ITEMS_SEPARATOR)[INDEX_POSITION] : StringUtils.EMPTY;
    }

}
