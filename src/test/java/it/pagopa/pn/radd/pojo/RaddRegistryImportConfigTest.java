package it.pagopa.pn.radd.pojo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RaddRegistryImportConfigTest {
    /**
     * Method under test: {@link RaddRegistryImportConfig#canEqual(Object)}
     */
    @Test
    void testCanEqual() {
        assertFalse((new RaddRegistryImportConfig()).canEqual("Other"));
    }

    /**
     * Method under test: {@link RaddRegistryImportConfig#canEqual(Object)}
     */
    @Test
    void testCanEqual2() {
        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDefaultEndValidity(1);
        raddRegistryImportConfig.setDeleteRole("Delete Role");

        RaddRegistryImportConfig raddRegistryImportConfig2 = new RaddRegistryImportConfig();
        raddRegistryImportConfig2.setDefaultEndValidity(1);
        raddRegistryImportConfig2.setDeleteRole("Delete Role");
        assertTrue(raddRegistryImportConfig.canEqual(raddRegistryImportConfig2));
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>default or parameterless constructor of {@link RaddRegistryImportConfig}
     *   <li>{@link RaddRegistryImportConfig#setDefaultEndValidity(int)}
     *   <li>{@link RaddRegistryImportConfig#setDeleteRole(String)}
     *   <li>{@link RaddRegistryImportConfig#toString()}
     *   <li>{@link RaddRegistryImportConfig#getDefaultEndValidity()}
     *   <li>{@link RaddRegistryImportConfig#getDeleteRole()}
     * </ul>
     */
    @Test
    void testConstructor() {
        RaddRegistryImportConfig actualRaddRegistryImportConfig = new RaddRegistryImportConfig();
        actualRaddRegistryImportConfig.setDefaultEndValidity(1);
        actualRaddRegistryImportConfig.setDeleteRole("Delete Role");
        String actualToStringResult = actualRaddRegistryImportConfig.toString();
        assertEquals(1, actualRaddRegistryImportConfig.getDefaultEndValidity());
        assertEquals("Delete Role", actualRaddRegistryImportConfig.getDeleteRole());
        assertEquals("RaddRegistryImportConfig(defaultEndValidity=1, deleteRole=Delete Role)", actualToStringResult);
    }

    /**
     * Method under test: {@link RaddRegistryImportConfig#equals(Object)}
     */
    @Test
    void testEquals() {
        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDefaultEndValidity(1);
        raddRegistryImportConfig.setDeleteRole("Delete Role");
        assertNotEquals(raddRegistryImportConfig, null);
    }

    /**
     * Method under test: {@link RaddRegistryImportConfig#equals(Object)}
     */
    @Test
    void testEquals2() {
        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDefaultEndValidity(1);
        raddRegistryImportConfig.setDeleteRole("Delete Role");
        assertNotEquals(raddRegistryImportConfig, "Different type to RaddRegistryImportConfig");
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RaddRegistryImportConfig#equals(Object)}
     *   <li>{@link RaddRegistryImportConfig#hashCode()}
     * </ul>
     */
    @Test
    void testEquals3() {
        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDefaultEndValidity(1);
        raddRegistryImportConfig.setDeleteRole("Delete Role");
        assertEquals(raddRegistryImportConfig, raddRegistryImportConfig);
        int expectedHashCodeResult = raddRegistryImportConfig.hashCode();
        assertEquals(expectedHashCodeResult, raddRegistryImportConfig.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RaddRegistryImportConfig#equals(Object)}
     *   <li>{@link RaddRegistryImportConfig#hashCode()}
     * </ul>
     */
    @Test
    void testEquals4() {
        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDefaultEndValidity(1);
        raddRegistryImportConfig.setDeleteRole("Delete Role");

        RaddRegistryImportConfig raddRegistryImportConfig2 = new RaddRegistryImportConfig();
        raddRegistryImportConfig2.setDefaultEndValidity(1);
        raddRegistryImportConfig2.setDeleteRole("Delete Role");
        assertEquals(raddRegistryImportConfig, raddRegistryImportConfig2);
        int expectedHashCodeResult = raddRegistryImportConfig.hashCode();
        assertEquals(expectedHashCodeResult, raddRegistryImportConfig2.hashCode());
    }

    /**
     * Method under test: {@link RaddRegistryImportConfig#equals(Object)}
     */
    @Test
    void testEquals5() {
        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDefaultEndValidity(2);
        raddRegistryImportConfig.setDeleteRole("Delete Role");

        RaddRegistryImportConfig raddRegistryImportConfig2 = new RaddRegistryImportConfig();
        raddRegistryImportConfig2.setDefaultEndValidity(1);
        raddRegistryImportConfig2.setDeleteRole("Delete Role");
        assertNotEquals(raddRegistryImportConfig, raddRegistryImportConfig2);
    }

    /**
     * Method under test: {@link RaddRegistryImportConfig#equals(Object)}
     */
    @Test
    void testEquals6() {
        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDefaultEndValidity(1);
        raddRegistryImportConfig.setDeleteRole(null);

        RaddRegistryImportConfig raddRegistryImportConfig2 = new RaddRegistryImportConfig();
        raddRegistryImportConfig2.setDefaultEndValidity(1);
        raddRegistryImportConfig2.setDeleteRole("Delete Role");
        assertNotEquals(raddRegistryImportConfig, raddRegistryImportConfig2);
    }

    /**
     * Method under test: {@link RaddRegistryImportConfig#equals(Object)}
     */
    @Test
    void testEquals7() {
        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDefaultEndValidity(1);
        raddRegistryImportConfig.setDeleteRole("it.pagopa.pn.radd.pojo.RaddRegistryImportConfig");

        RaddRegistryImportConfig raddRegistryImportConfig2 = new RaddRegistryImportConfig();
        raddRegistryImportConfig2.setDefaultEndValidity(1);
        raddRegistryImportConfig2.setDeleteRole("Delete Role");
        assertNotEquals(raddRegistryImportConfig, raddRegistryImportConfig2);
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RaddRegistryImportConfig#equals(Object)}
     *   <li>{@link RaddRegistryImportConfig#hashCode()}
     * </ul>
     */
    @Test
    void testEquals8() {
        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDefaultEndValidity(1);
        raddRegistryImportConfig.setDeleteRole(null);

        RaddRegistryImportConfig raddRegistryImportConfig2 = new RaddRegistryImportConfig();
        raddRegistryImportConfig2.setDefaultEndValidity(1);
        raddRegistryImportConfig2.setDeleteRole(null);
        assertEquals(raddRegistryImportConfig, raddRegistryImportConfig2);
        int expectedHashCodeResult = raddRegistryImportConfig.hashCode();
        assertEquals(expectedHashCodeResult, raddRegistryImportConfig2.hashCode());
    }
}

