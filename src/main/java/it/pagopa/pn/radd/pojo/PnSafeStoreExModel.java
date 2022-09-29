package it.pagopa.pn.radd.pojo;

import lombok.Data;

import java.util.List;

@Data
public class PnSafeStoreExModel {
    private String resultDescription;
    private List<String> errorList;
    private String resultCode;


}
