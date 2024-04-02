package it.pagopa.pn.radd.middleware.db.entities;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class PnRaddRegistryImportEntityTest {
    /**
     * Method under test: {@link PnRaddRegistryImportEntity#canEqual(Object)}
     */
    @Test
    void testCanEqual() {
        assertFalse((new PnRaddRegistryImportEntity()).canEqual("Other"));
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#canEqual(Object)}
     */
    @Test
    void testCanEqual2() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertTrue(pnRaddRegistryImportEntity.canEqual(pnRaddRegistryImportEntity2));
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>default or parameterless constructor of {@link PnRaddRegistryImportEntity}
     *   <li>{@link PnRaddRegistryImportEntity#setChecksum(String)}
     *   <li>{@link PnRaddRegistryImportEntity#setConfig(String)}
     *   <li>{@link PnRaddRegistryImportEntity#setCreatedAt(Instant)}
     *   <li>{@link PnRaddRegistryImportEntity#setCxId(String)}
     *   <li>{@link PnRaddRegistryImportEntity#setError(String)}
     *   <li>{@link PnRaddRegistryImportEntity#setFileKey(String)}
     *   <li>{@link PnRaddRegistryImportEntity#setRequestId(String)}
     *   <li>{@link PnRaddRegistryImportEntity#setStatus(String)}
     *   <li>{@link PnRaddRegistryImportEntity#setTtl(Long)}
     *   <li>{@link PnRaddRegistryImportEntity#setUpdatedAt(Instant)}
     *   <li>{@link PnRaddRegistryImportEntity#toString()}
     *   <li>{@link PnRaddRegistryImportEntity#getChecksum()}
     *   <li>{@link PnRaddRegistryImportEntity#getConfig()}
     *   <li>{@link PnRaddRegistryImportEntity#getUpdatedAt()}
     *   <li>{@link PnRaddRegistryImportEntity#getCreatedAt()}
     *   <li>{@link PnRaddRegistryImportEntity#getCxId()}
     *   <li>{@link PnRaddRegistryImportEntity#getError()}
     *   <li>{@link PnRaddRegistryImportEntity#getFileKey()}
     *   <li>{@link PnRaddRegistryImportEntity#getRequestId()}
     *   <li>{@link PnRaddRegistryImportEntity#getStatus()}
     *   <li>{@link PnRaddRegistryImportEntity#getTtl()}
     * </ul>
     */
    @Test
    void testConstructor() {
        PnRaddRegistryImportEntity actualPnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        actualPnRaddRegistryImportEntity.setChecksum("Checksum");
        actualPnRaddRegistryImportEntity.setConfig("Config");
        actualPnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        actualPnRaddRegistryImportEntity.setCxId("42");
        actualPnRaddRegistryImportEntity.setError("An error occurred");
        actualPnRaddRegistryImportEntity.setFileKey("File Key");
        actualPnRaddRegistryImportEntity.setRequestId("42");
        actualPnRaddRegistryImportEntity.setStatus("Status");
        actualPnRaddRegistryImportEntity.setTtl(1L);
        Instant updatedAt = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        actualPnRaddRegistryImportEntity.setUpdatedAt(updatedAt);
        String actualToStringResult = actualPnRaddRegistryImportEntity.toString();
        assertEquals("Checksum", actualPnRaddRegistryImportEntity.getChecksum());
        assertEquals("Config", actualPnRaddRegistryImportEntity.getConfig());
        Instant updatedAt2 = actualPnRaddRegistryImportEntity.getUpdatedAt();
        assertSame(updatedAt2, actualPnRaddRegistryImportEntity.getCreatedAt());
        assertEquals("42", actualPnRaddRegistryImportEntity.getCxId());
        assertEquals("An error occurred", actualPnRaddRegistryImportEntity.getError());
        assertEquals("File Key", actualPnRaddRegistryImportEntity.getFileKey());
        assertEquals("42", actualPnRaddRegistryImportEntity.getRequestId());
        assertEquals("Status", actualPnRaddRegistryImportEntity.getStatus());
        assertEquals(1L, actualPnRaddRegistryImportEntity.getTtl().longValue());
        assertSame(updatedAt.EPOCH, updatedAt2);
        assertEquals("PnRaddRegistryImportEntity(cxId=42, requestId=42, fileKey=File Key, checksum=Checksum, status=Status,"
                + " error=An error occurred, config=Config, ttl=1, createdAt=1970-01-01T00:00:00Z, updatedAt=1970-01-01T00"
                + ":00:00Z)", actualToStringResult);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, null);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals2() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, "Different type to PnRaddRegistryImportEntity");
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link PnRaddRegistryImportEntity#equals(Object)}
     *   <li>{@link PnRaddRegistryImportEntity#hashCode()}
     * </ul>
     */
    @Test
    void testEquals3() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity);
        int expectedHashCodeResult = pnRaddRegistryImportEntity.hashCode();
        assertEquals(expectedHashCodeResult, pnRaddRegistryImportEntity.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link PnRaddRegistryImportEntity#equals(Object)}
     *   <li>{@link PnRaddRegistryImportEntity#hashCode()}
     * </ul>
     */
    @Test
    void testEquals4() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
        int expectedHashCodeResult = pnRaddRegistryImportEntity.hashCode();
        assertEquals(expectedHashCodeResult, pnRaddRegistryImportEntity2.hashCode());
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals5() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("42");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals6() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum(null);
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals7() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("42");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals8() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig(null);
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals9() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity.setCreatedAt(LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals10() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("File Key");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals11() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId(null);
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals12() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("42");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals13() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError(null);
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals14() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("42");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals15() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey(null);
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals16() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("File Key");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals17() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId(null);
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals18() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("42");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals19() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus(null);
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals20() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(3L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals21() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(null);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Method under test: {@link PnRaddRegistryImportEntity#equals(Object)}
     */
    @Test
    void testEquals22() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity.setUpdatedAt(LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertNotEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link PnRaddRegistryImportEntity#equals(Object)}
     *   <li>{@link PnRaddRegistryImportEntity#hashCode()}
     * </ul>
     */
    @Test
    void testEquals23() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum(null);
        pnRaddRegistryImportEntity.setConfig("Config");
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum(null);
        pnRaddRegistryImportEntity2.setConfig("Config");
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
        int expectedHashCodeResult = pnRaddRegistryImportEntity.hashCode();
        assertEquals(expectedHashCodeResult, pnRaddRegistryImportEntity2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link PnRaddRegistryImportEntity#equals(Object)}
     *   <li>{@link PnRaddRegistryImportEntity#hashCode()}
     * </ul>
     */
    @Test
    void testEquals24() {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setChecksum("Checksum");
        pnRaddRegistryImportEntity.setConfig(null);
        pnRaddRegistryImportEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity.setCxId("42");
        pnRaddRegistryImportEntity.setError("An error occurred");
        pnRaddRegistryImportEntity.setFileKey("File Key");
        pnRaddRegistryImportEntity.setRequestId("42");
        pnRaddRegistryImportEntity.setStatus("Status");
        pnRaddRegistryImportEntity.setTtl(1L);
        pnRaddRegistryImportEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

        PnRaddRegistryImportEntity pnRaddRegistryImportEntity2 = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity2.setChecksum("Checksum");
        pnRaddRegistryImportEntity2.setConfig(null);
        pnRaddRegistryImportEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        pnRaddRegistryImportEntity2.setCxId("42");
        pnRaddRegistryImportEntity2.setError("An error occurred");
        pnRaddRegistryImportEntity2.setFileKey("File Key");
        pnRaddRegistryImportEntity2.setRequestId("42");
        pnRaddRegistryImportEntity2.setStatus("Status");
        pnRaddRegistryImportEntity2.setTtl(1L);
        pnRaddRegistryImportEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertEquals(pnRaddRegistryImportEntity, pnRaddRegistryImportEntity2);
        int expectedHashCodeResult = pnRaddRegistryImportEntity.hashCode();
        assertEquals(expectedHashCodeResult, pnRaddRegistryImportEntity2.hashCode());
    }
}

