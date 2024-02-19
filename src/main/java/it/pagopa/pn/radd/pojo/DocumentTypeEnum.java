package it.pagopa.pn.radd.pojo;

import static it.pagopa.pn.radd.utils.Const.ATTESTAZIONE_OPPONIBILE_A_TERZI;
import static it.pagopa.pn.radd.utils.Const.ATTO_NOTIFICATO;

public enum DocumentTypeEnum {
    DOCUMENT(ATTO_NOTIFICATO),

    ATTACHMENT(ATTO_NOTIFICATO),

    LEGAL_FACT(ATTESTAZIONE_OPPONIBILE_A_TERZI),

    LEGAL_FACT_EXTERNAL(ATTESTAZIONE_OPPONIBILE_A_TERZI),

    COVER_FILE(ATTO_NOTIFICATO),

    AAR(ATTO_NOTIFICATO);

    private String value;

    DocumentTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
