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
public class OperationsIunsEntity {

    public static final String COL_ID= "id";
    public static final String COL_IUN = "iun";
    public static final String COL_OPERATION_ID = "operationId";
    public static final String SECONDARY_INDEX = "iun-and-operation-index";
    public static final String OPERATION_INDEX = "operation-index";


    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_ID)}))
    private String id;

    @Getter(onMethod=@__({@DynamoDbSecondaryPartitionKey(indexNames = SECONDARY_INDEX), @DynamoDbAttribute(COL_IUN)}))
    private String iun;

    @Getter(onMethod=@__({@DynamoDbSecondarySortKey(indexNames = SECONDARY_INDEX), @DynamoDbSecondaryPartitionKey(indexNames = OPERATION_INDEX), @DynamoDbAttribute(COL_OPERATION_ID)}))
    private String operationId;

}
