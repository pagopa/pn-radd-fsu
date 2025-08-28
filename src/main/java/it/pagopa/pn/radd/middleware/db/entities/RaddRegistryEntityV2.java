package it.pagopa.pn.radd.middleware.db.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.util.List;

@DynamoDbBean
@Setter
@ToString
@EqualsAndHashCode
public class RaddRegistryEntityV2 {

    public static final String COL_PARTNER_ID = "partnerId";
    public static final String COL_LOCATION_ID = "locationId";
    public static final String COL_EXTERNAL_CODES = "externalCodes";
    public static final String COL_PHONE_NUMBERS = "phoneNumbers";
    public static final String COL_EMAIL = "email";
    public static final String COL_APPOINTMENT_REQUIRED = "appointmentRequired";
    public static final String COL_WEBSITE = "website";
    public static final String COL_PARTNER_TYPE = "partnerType";
    public static final String COL_CREATION_TIMESTAMP = "creationTimestamp";
    public static final String COL_UPDATE_TIMESTAMP = "updateTimestamp";
    public static final String COL_ADDRESS = "address";
    public static final String COL_NORMALIZED_ADDRESS = "normalizedAddress";
    public static final String COL_MODIFIED_ADDRESS = "modifiedAddress";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_OPENING_TIME = "openingTime";
    public static final String COL_START_VALIDITY = "startValidity";
    public static final String COL_END_VALIDITY = "endValidity";
    public static final String COL_UID = "uid";

    @Getter(onMethod = @__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PARTNER_ID)}))
    private String partnerId;

    @Getter(onMethod = @__({@DynamoDbSortKey, @DynamoDbAttribute(COL_LOCATION_ID)}))
    private String locationId;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_EXTERNAL_CODES)}))
    private List<String> externalCodes;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_PHONE_NUMBERS)}))
    private List<String> phoneNumbers;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_EMAIL)}))
    private String email;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_APPOINTMENT_REQUIRED)}))
    private Boolean appointmentRequired;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_WEBSITE)}))
    private String website;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_PARTNER_TYPE)}))
    private String partnerType;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CREATION_TIMESTAMP)}))
    private Instant creationTimestamp;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_UPDATE_TIMESTAMP)}))
    private Instant updateTimestamp;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_ADDRESS)}))
    private AddressEntity address;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_NORMALIZED_ADDRESS)}))
    private NormalizedAddressEntity normalizedAddress;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_MODIFIED_ADDRESS)}))
    private Boolean modifiedAddress;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_DESCRIPTION)}))
    private String description;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_OPENING_TIME)}))
    private String openingTime;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_START_VALIDITY)}))
    private Instant startValidity;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_END_VALIDITY)}))
    private Instant endValidity;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_UID)}))
    private String uid;

}

