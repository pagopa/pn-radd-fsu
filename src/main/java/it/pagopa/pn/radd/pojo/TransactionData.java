package it.pagopa.pn.radd.pojo;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class TransactionData {
    private String iun;
    private String qrCode;
    private String operationId;
    private String fileKey;
    private Date operationDate;
    private String checksum;
    private String recipientId;
    private String recipientType;
    private String delegateId;
    private String ensureRecipientId;
    private String ensureDelegateId;
    private List<String> urls;



}
