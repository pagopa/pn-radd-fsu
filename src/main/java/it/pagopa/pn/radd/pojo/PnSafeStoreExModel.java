package it.pagopa.pn.radd.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PnSafeStoreExModel {
    private String resultDescription;
    private List<String> errorList;
    private String resultCode;


}
