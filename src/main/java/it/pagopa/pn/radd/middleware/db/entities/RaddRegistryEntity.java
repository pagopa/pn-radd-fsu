package it.pagopa.pn.radd.middleware.db.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.Instant;

@DynamoDbBean
@Setter
@ToString
@EqualsAndHashCode
@Builder
public class RaddRegistryEntity {

    public static final String COL_REGISTRY_ID = "registryId";
    public static final String COL_CXID = "cxId";
    public static final String COL_REQUEST_ID = "requestId";
    public static final String COL_NORMALIZED_ADDRESS= "normalizedAddress";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_PHONE_NUMBER = "phoneNumber";
    public static final String COL_GEOLOCATION = "geoLocation";
    public static final String COL_ZIP_CODE = "zipCode";
    public static final String COL_OPENING_TIME = "openingTime";
    public static final String COL_START_VALIDITY = "startValidity";
    public static final String COL_END_VALIDITY = "endValidity";

    public static final String CXID_REQUESTID_INDEX = "cxId-requestId-index";
    public static final String ZIPCODE_INDEX = "zipCode-index";


    @Getter(onMethod = @__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_REGISTRY_ID)}))
    private String registryId;
    @Getter(onMethod = @__({@DynamoDbSortKey, @DynamoDbSecondaryPartitionKey(indexNames = CXID_REQUESTID_INDEX), @DynamoDbAttribute(COL_CXID)}))
    private String cxId;
    @Getter(onMethod = @__({@DynamoDbSecondarySortKey(indexNames = CXID_REQUESTID_INDEX), @DynamoDbAttribute(COL_REQUEST_ID)}))
    private String requestId;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_NORMALIZED_ADDRESS)}))
    private String normalizedAddress;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_DESCRIPTION)}))
    private String description;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_PHONE_NUMBER)}))
    private String phoneNumber;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_GEOLOCATION)}))
    private String geoLocation;
    @Getter(onMethod = @__({@DynamoDbSecondaryPartitionKey(indexNames = ZIPCODE_INDEX), @DynamoDbAttribute(COL_ZIP_CODE)}))
    private String zipCode;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_OPENING_TIME)}))
    private String openingTime;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_START_VALIDITY)}))
    private Instant startValidity;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_END_VALIDITY)}))
    private Instant endValidity;

}
