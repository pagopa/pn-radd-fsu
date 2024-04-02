package it.pagopa.pn.radd.pojo;

import lombok.Data;

@Data
public class AddressManagerOriginalRequest {
    private String addressRow;
    private String cap;
    private String city;
    private String pr;
    private String country;
    private String startValidity;
    private String endValidity;
    private String openingTime;
    private String description;
    private String geoLocation;
    private String phoneNumber;

}
