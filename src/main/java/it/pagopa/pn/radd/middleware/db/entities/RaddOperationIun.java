package it.pagopa.pn.radd.middleware.db.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
@Getter
@Setter
@ToString
@NoArgsConstructor
public class RaddOperationIun {
    public static final String COL_ID = "id";
    public static final String COL_IUN = "iun";
    public static final String COL_OPERATION_ID = "operationId";
    public static final String INDEX_SECONDARY_NAME = "iun-global-index";

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_ID)}))
    private String id;
    @Getter(onMethod=@__({@DynamoDbSecondaryPartitionKey(indexNames = INDEX_SECONDARY_NAME ), @DynamoDbAttribute(COL_IUN)}))
    private String iun;
    @Getter(onMethod=@__({@DynamoDbSecondarySortKey(indexNames = INDEX_SECONDARY_NAME ), @DynamoDbAttribute(COL_OPERATION_ID)}))
    private String operationId;

}
