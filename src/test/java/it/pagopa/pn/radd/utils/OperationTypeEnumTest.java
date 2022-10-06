package it.pagopa.pn.radd.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationTypeEnumTest {

    @Test
    public void testValueOf() {
        assertEquals("ACT", OperationTypeEnum.ACT.name());
        assertEquals("AOR", OperationTypeEnum.AOR.name());
    }
}