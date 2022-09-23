package it.pagopa.pn.radd.middleware.db.entities;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
@Data
@NoArgsConstructor
public class RaddTransactionEntity {

    public static final String COL_IUN = "iun";
    public static final String COL_OPERATION_ID = "operationId";
    public static final String COL_STATUS = "operation_status";
    public static final String COL_FILE_KEY = "fileKey";
    public static final String COL_QR_CODE = "qrCode";
    public static final String COL_RECIPIENT_ID = "recipientId";
    public static final String COL_RECIPIENT_TYPE = "recipientType";
    public static final String COL_DELEGATE_ID = "delegateId";
    public static final String COL_UID = "uid";
    public static final String COL_OPERATION_START_DATE = "operationStartDate";
    public static final String COL_OPERATION_END_DATE = "operationEndDate";
    public static final String COL_VERSION_TOKEN = "versionToken";
    public static final String COL_ERROR_REASON = "errorReason";
    public static final String IUN_INDEX = "iun-global";


    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_OPERATION_ID)}))
    private String operationId;

    @Getter(onMethod=@__({@DynamoDbSecondaryPartitionKey(indexNames = { IUN_INDEX}), @DynamoDbAttribute(COL_IUN)}))
    private String iun;



    @Getter(onMethod=@__({@DynamoDbAttribute(COL_FILE_KEY)}))
    private String fileKey;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_QR_CODE)}))
    private String qrCode;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_RECIPIENT_ID)}))
    private String recipientId;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_RECIPIENT_TYPE)}))
    private String recipientType;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DELEGATE_ID)}))
    private String delegateId;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_UID)}))
    private String uid;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_STATUS)}))
    private String status;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_OPERATION_START_DATE)}))
    private String operationStartDate;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_OPERATION_END_DATE)}))
    private String operationEndDate;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_VERSION_TOKEN)}))
    private String versionToken;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_ERROR_REASON)}))
    private String errorReason;

}
