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
    public static final String IUN_INDEX = "iun-global";


    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_OPERATION_ID)}))
    private String operationId;

    @Getter(onMethod=@__({@DynamoDbSecondaryPartitionKey(indexNames = { IUN_INDEX}), @DynamoDbAttribute(COL_IUN)}))
    private String iun;

}
