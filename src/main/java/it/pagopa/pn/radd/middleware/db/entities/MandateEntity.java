package it.pagopa.pn.radd.middleware.db.entities;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.Instant;
import java.util.Set;
import it.pagopa.pn.radd.middleware.db.BaseDao;

/**
 * Entity Delega
 */
@DynamoDbBean
@Data
@NoArgsConstructor
public class MandateEntity {

    public static final String MANDATE_PREFIX = "MANDATE#";

    public static final String COL_PK = "pk";
    public static final String COL_SK = "sk";
    public static final String COL_S_DELEGATE = "s_delegate";
    public static final String COL_I_STATE = "i_state";
    public static final String COL_B_DELEGATORISPERSON = "b_delegatorisperson";
    public static final String COL_B_DELEGATEISPERSON = "b_delegateisperson";
    public static final String COL_D_VALIDFROM = "d_validfrom";
    public static final String COL_D_VALIDTO = "d_validto";
    public static final String COL_S_MANDATEID = "s_mandateid";
    public static final String COL_T_CREATED = "t_created";
    public static final String COL_T_ACCEPTED = "t_accepted";
    public static final String COL_T_REJECTED = "t_rejected";
    public static final String COL_T_REVOKED = "t_revoked";
    public static final String COL_S_VALIDATIONCODE = "s_validationcode";
    public static final String COL_A_VISIBILITYIDS = "a_visibilityids";
    public static final String COL_I_TTL = "i_ttl";

    public MandateEntity(String delegator, String mandateid)
    {
        this.setDelegator(delegator);
        this.setMandateId(mandateid);
    }

    @DynamoDbAttribute(COL_S_MANDATEID)
    public String getMandateId(){
        return this.sk.replace(MANDATE_PREFIX, "");
    }
    public void setMandateId(String id){
        this.sk = MANDATE_PREFIX + id;
    }

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)})) private String delegator;
    @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)}))  private String sk;


    @Getter(onMethod=@__({@DynamoDbSecondaryPartitionKey(indexNames = { BaseDao.GSI_INDEX_DELEGATE_STATE}), @DynamoDbAttribute(COL_S_DELEGATE)}))  private String delegate;
    @Getter(onMethod=@__({@DynamoDbSecondarySortKey(indexNames = { BaseDao.GSI_INDEX_DELEGATE_STATE }), @DynamoDbAttribute(COL_I_STATE)}))  private int state;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_B_DELEGATORISPERSON)}))  private Boolean delegatorisperson;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_B_DELEGATEISPERSON)}))  private Boolean delegateisperson;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_D_VALIDFROM)}))  private Instant validfrom;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_D_VALIDTO)}))  private Instant validto;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_T_CREATED)}))  private Instant created;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_T_ACCEPTED)}))  private Instant accepted;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_T_REJECTED)}))  private Instant rejected;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_T_REVOKED)}))  private Instant revoked;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_S_VALIDATIONCODE)}))  private String validationcode;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_A_VISIBILITYIDS)}))  private Set<String> visibilityIds;


    // per lo storico e per la struttura dati di appoggio
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_I_TTL)}))  private Long ttl;
    
}
