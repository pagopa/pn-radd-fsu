package it.pagopa.pn.radd.middleware.db.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.math.BigDecimal;

@DynamoDbBean
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class BiasPointEntity {

    public static final String COL_ADDRESS_NUMBER = "addressNumber";
    public static final String COL_COUNTRY = "country";
    public static final String COL_ADDRESS = "locality";
    public static final String COL_POSTAL_CODE = "postalCode";
    public static final String COL_SUB_REGION = "subRegion";
    public static final String COL_OVERALL = "overall";

    private BigDecimal addressNumber;
    private BigDecimal country;
    private BigDecimal locality;
    private BigDecimal postalCode;
    private BigDecimal subRegion;
    private BigDecimal overall;

}
