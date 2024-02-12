package it.pagopa.pn.radd.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}

