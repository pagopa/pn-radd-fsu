package it.pagopa.pn.radd.pojo;

import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class TransactionData {
    private String transactionId;
    private String uid;
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
    private String versionId;
    private OperationTypeEnum operationType;
    private List<String> iuns = new ArrayList<>();
    private List<String> urls = new ArrayList<>();
    private Map<String, String> zipAttachments;


}
