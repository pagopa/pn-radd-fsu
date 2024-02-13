package it.pagopa.pn.radd.middleware.db.entities;

import it.pagopa.pn.radd.rest.radd.v1.dto.CxTypeAuthFleet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import static it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity.ITEMS_SEPARATOR;
import static it.pagopa.pn.radd.utils.Utils.transactionIdBuilder;

/**
 * Questa entità mappa la tabella pn-operations-iuns. Viene utilizzata solo dal caso d'uso AOR.
 */
@DynamoDbBean
@Getter
@Setter
@ToString
@NoArgsConstructor
public class OperationsIunsEntity {

    public static final String COL_TRANSACTION_ID = "transactionId";
    public static final String COL_IUN = "iun";

    public static final String IUN_TRANSACTION_INDEX = "iun-transaction-index";
    private static final int CX_TYPE_INDEX = 0;
    private static final int CX_ID_INDEX = 1;
    private static final int OPERATION_ID_INDEX = 2;

    @Getter(onMethod = @__({@DynamoDbPartitionKey, @DynamoDbSecondarySortKey(indexNames = IUN_TRANSACTION_INDEX), @DynamoDbAttribute(COL_TRANSACTION_ID)}))
    private String transactionId;

    @Getter(onMethod = @__({@DynamoDbSortKey, @DynamoDbSecondaryPartitionKey(indexNames = IUN_TRANSACTION_INDEX), @DynamoDbAttribute(COL_IUN)}))
    private String iun;


    public OperationsIunsEntity(String cxType, String cxId, String operationId) {
        this.setTransactionId(cxType, cxId, operationId);
    }

    private void setTransactionId(String cxType, String cxId, String operationId) {
        this.setTransactionId(transactionIdBuilder(CxTypeAuthFleet.valueOf(cxType), cxId, operationId));
    }

    @DynamoDbIgnore
    private String getCxType() {
        return this.getTransactionId().split(ITEMS_SEPARATOR)[CX_TYPE_INDEX];
    }

    @DynamoDbIgnore
    private String getCxId() {
        return this.getTransactionId().split(ITEMS_SEPARATOR)[CX_ID_INDEX];
    }

    @DynamoDbIgnore
    private String getOperationId() {
        return this.getTransactionId().split(ITEMS_SEPARATOR)[OPERATION_ID_INDEX];
    }
}
