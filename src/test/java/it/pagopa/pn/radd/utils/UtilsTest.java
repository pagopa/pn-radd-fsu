package it.pagopa.pn.radd.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DownloadUrl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class UtilsTest {
    /**
     * Method under test: {@link Utils#checkOperationType(String)}
     */
    @Test
    void testCheckOperationType() {
        assertFalse(Utils.checkOperationType("Operation Type"));
    }

    /**
     * Method under test: {@link Utils#checkPersonType(String)}
     */
    @Test
    void testCheckPersonType() {
        assertFalse(Utils.checkPersonType("Person Type"));
        assertTrue(Utils.checkPersonType(Const.PF));
        assertTrue(Utils.checkPersonType(Const.PG));
    }

    /**
     * Method under test: {@link Utils#getDocumentDownloadUrl(String, String, String, String)}
     */
    @Test
    void testGetDocumentDownloadUrl() {
        DownloadUrl actualDocumentDownloadUrl = Utils.getDocumentDownloadUrl("https://example.org/example",
                "https://example.org/example", "https://example.org/example", "https://example.org/example");
        assertTrue(actualDocumentDownloadUrl.getNeedAuthentication());
        assertEquals(
                "https://example.org/example/radd-net/api/v1/download/https://example.org/example/https://example.org"
                        + "/example?attachmentId=https://example.org/example",
                actualDocumentDownloadUrl.getUrl());
    }

    /**
     * Method under test: {@link Utils#getDocumentDownloadUrl(String, String, String, String)}
     */
    @Test
    void testGetDocumentDownloadUrl4() {
        DownloadUrl actualDocumentDownloadUrl = Utils.getDocumentDownloadUrl("https://example.org/example",
                "https://example.org/example", "https://example.org/example", null);
        assertTrue(actualDocumentDownloadUrl.getNeedAuthentication());
        assertEquals(
                "https://example.org/example/radd-net/api/v1/download/https://example.org/example/https://example.org"
                        + "/example",
                actualDocumentDownloadUrl.getUrl());
    }

    /**
     * Method under test: {@link Utils#transactionIdBuilder(CxTypeAuthFleet, String, String)}
     */
    @Test
    void testTransactionIdBuilder() {
        assertEquals("PA#42#42", Utils.transactionIdBuilder(CxTypeAuthFleet.PA, "42", "42"));
        assertEquals("PF#42#42", Utils.transactionIdBuilder(CxTypeAuthFleet.PF, "42", "42"));
        assertEquals("PG#42#42", Utils.transactionIdBuilder(CxTypeAuthFleet.PG, "42", "42"));
    }
}

