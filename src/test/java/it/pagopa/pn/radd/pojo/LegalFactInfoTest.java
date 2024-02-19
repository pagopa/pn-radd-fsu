package it.pagopa.pn.radd.pojo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactCategoryDto;
import org.junit.jupiter.api.Test;

class LegalFactInfoTest {
    /**
     * Method under test: {@link LegalFactInfo#canEqual(Object)}
     */
    @Test
    void testCanEqual() {
        assertFalse((new LegalFactInfo()).canEqual("Other"));
    }

    /**
     * Method under test: {@link LegalFactInfo#canEqual(Object)}
     */
    @Test
    void testCanEqual2() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertTrue(legalFactInfo.canEqual(legalFactInfo2));
    }

    /**
     * Method under test: {@link LegalFactInfo#canEqual(Object)}
     */
    @Test
    void testCanEqual3() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertTrue(legalFactInfo.canEqual(legalFactInfo2));
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>default or parameterless constructor of {@link LegalFactInfo}
     *   <li>{@link LegalFactInfo#setCategory(LegalFactCategoryDto)}
     *   <li>{@link LegalFactInfo#setContentType(String)}
     *   <li>{@link LegalFactInfo#setKey(String)}
     *   <li>{@link LegalFactInfo#setUrl(String)}
     *   <li>{@link LegalFactInfo#toString()}
     *   <li>{@link LegalFactInfo#getCategory()}
     *   <li>{@link LegalFactInfo#getContentType()}
     *   <li>{@link LegalFactInfo#getKey()}
     *   <li>{@link LegalFactInfo#getUrl()}
     * </ul>
     */
    @Test
    void testConstructor() {
        LegalFactInfo actualLegalFactInfo = new LegalFactInfo();
        actualLegalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        actualLegalFactInfo.setContentType("text/plain");
        actualLegalFactInfo.setKey("Key");
        actualLegalFactInfo.setUrl("https://example.org/example");
        String actualToStringResult = actualLegalFactInfo.toString();
        assertEquals(LegalFactCategoryDto.SENDER_ACK, actualLegalFactInfo.getCategory());
        assertEquals("text/plain", actualLegalFactInfo.getContentType());
        assertEquals("Key", actualLegalFactInfo.getKey());
        assertEquals("https://example.org/example", actualLegalFactInfo.getUrl());
        assertEquals(
                "LegalFactInfo(key=Key, url=https://example.org/example, contentType=text/plain, category=SENDER_ACK)",
                actualToStringResult);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");
        assertNotEquals(null, legalFactInfo);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals2() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, "Different type to LegalFactInfo");
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link LegalFactInfo#equals(Object)}
     *   <li>{@link LegalFactInfo#hashCode()}
     * </ul>
     */
    @Test
    void testEquals3() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");
        assertEquals(legalFactInfo, legalFactInfo);
        int expectedHashCodeResult = legalFactInfo.hashCode();
        assertEquals(expectedHashCodeResult, legalFactInfo.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link LegalFactInfo#equals(Object)}
     *   <li>{@link LegalFactInfo#hashCode()}
     * </ul>
     */
    @Test
    void testEquals4() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertEquals(legalFactInfo, legalFactInfo2);
        int expectedHashCodeResult = legalFactInfo.hashCode();
        assertEquals(expectedHashCodeResult, legalFactInfo2.hashCode());
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals5() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType("Not all who wander are lost");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals6() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType(null);
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals7() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("https://example.org/example");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals8() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey(null);
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals9() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("Key");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals10() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl(null);

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link LegalFactInfo#equals(Object)}
     *   <li>{@link LegalFactInfo#hashCode()}
     * </ul>
     */
    @Test
    void testEquals11() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType(null);
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setContentType(null);
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertEquals(legalFactInfo, legalFactInfo2);
        int expectedHashCodeResult = legalFactInfo.hashCode();
        assertEquals(expectedHashCodeResult, legalFactInfo2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link LegalFactInfo#equals(Object)}
     *   <li>{@link LegalFactInfo#hashCode()}
     * </ul>
     */
    @Test
    void testEquals12() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey(null);
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey(null);
        legalFactInfo2.setUrl("https://example.org/example");
        assertEquals(legalFactInfo, legalFactInfo2);
        int expectedHashCodeResult = legalFactInfo.hashCode();
        assertEquals(expectedHashCodeResult, legalFactInfo2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link LegalFactInfo#equals(Object)}
     *   <li>{@link LegalFactInfo#hashCode()}
     * </ul>
     */
    @Test
    void testEquals13() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl(null);

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl(null);
        assertEquals(legalFactInfo, legalFactInfo2);
        int expectedHashCodeResult = legalFactInfo.hashCode();
        assertEquals(expectedHashCodeResult, legalFactInfo2.hashCode());
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals14() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, null);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals15() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, "Different type to LegalFactInfo");
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link LegalFactInfo#equals(Object)}
     *   <li>{@link LegalFactInfo#hashCode()}
     * </ul>
     */
    @Test
    void testEquals16() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");
        assertEquals(legalFactInfo, legalFactInfo);
        int expectedHashCodeResult = legalFactInfo.hashCode();
        assertEquals(expectedHashCodeResult, legalFactInfo.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link LegalFactInfo#equals(Object)}
     *   <li>{@link LegalFactInfo#hashCode()}
     * </ul>
     */
    @Test
    void testEquals17() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertEquals(legalFactInfo, legalFactInfo2);
        int expectedHashCodeResult = legalFactInfo.hashCode();
        assertEquals(expectedHashCodeResult, legalFactInfo2.hashCode());
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals18() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(null);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals19() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.DIGITAL_DELIVERY);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals20() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType("Not all who wander are lost");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals21() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType(null);
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals22() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("https://example.org/example");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals23() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey(null);
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals24() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("Key");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Method under test: {@link LegalFactInfo#equals(Object)}
     */
    @Test
    void testEquals25() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl(null);

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertNotEquals(legalFactInfo, legalFactInfo2);
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link LegalFactInfo#equals(Object)}
     *   <li>{@link LegalFactInfo#hashCode()}
     * </ul>
     */
    @Test
    void testEquals26() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(null);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(null);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertEquals(legalFactInfo, legalFactInfo2);
        int expectedHashCodeResult = legalFactInfo.hashCode();
        assertEquals(expectedHashCodeResult, legalFactInfo2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link LegalFactInfo#equals(Object)}
     *   <li>{@link LegalFactInfo#hashCode()}
     * </ul>
     */
    @Test
    void testEquals27() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType(null);
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType(null);
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl("https://example.org/example");
        assertEquals(legalFactInfo, legalFactInfo2);
        int expectedHashCodeResult = legalFactInfo.hashCode();
        assertEquals(expectedHashCodeResult, legalFactInfo2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link LegalFactInfo#equals(Object)}
     *   <li>{@link LegalFactInfo#hashCode()}
     * </ul>
     */
    @Test
    void testEquals28() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey(null);
        legalFactInfo.setUrl("https://example.org/example");

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey(null);
        legalFactInfo2.setUrl("https://example.org/example");
        assertEquals(legalFactInfo, legalFactInfo2);
        int expectedHashCodeResult = legalFactInfo.hashCode();
        assertEquals(expectedHashCodeResult, legalFactInfo2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link LegalFactInfo#equals(Object)}
     *   <li>{@link LegalFactInfo#hashCode()}
     * </ul>
     */
    @Test
    void testEquals29() {
        LegalFactInfo legalFactInfo = new LegalFactInfo();
        legalFactInfo.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo.setContentType("text/plain");
        legalFactInfo.setKey("Key");
        legalFactInfo.setUrl(null);

        LegalFactInfo legalFactInfo2 = new LegalFactInfo();
        legalFactInfo2.setCategory(LegalFactCategoryDto.SENDER_ACK);
        legalFactInfo2.setContentType("text/plain");
        legalFactInfo2.setKey("Key");
        legalFactInfo2.setUrl(null);
        assertEquals(legalFactInfo, legalFactInfo2);
        int expectedHashCodeResult = legalFactInfo.hashCode();
        assertEquals(expectedHashCodeResult, legalFactInfo2.hashCode());
    }
}

