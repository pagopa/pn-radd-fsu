package it.pagopa.pn.radd.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.OriginalRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RequestResponse;
import it.pagopa.pn.radd.config.CachedSecretsManagerConsumer;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.services.radd.fsu.v1.SecretService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RaddRegistryUtilsTest {
    /**
     * Method under test: {@link RaddRegistryUtils#prepareGlobalResult(List, boolean, int)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testPrepareGlobalResult() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(new ObjectMapper());
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(null)));
        raddRegistryUtils.prepareGlobalResult(new ArrayList<>(), true, 2);
    }

    /**
     * Method under test: {@link RaddRegistryUtils#prepareGlobalResult(List, boolean, int)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testPrepareGlobalResult2() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(mock(ObjectMapper.class));
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(null)));
        raddRegistryUtils.prepareGlobalResult(new ArrayList<>(), true, 2);
    }

    /**
     * Method under test: {@link RaddRegistryUtils#prepareGlobalResult(List, boolean, int)}
     */
    @Test
    void testPrepareGlobalResult3() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        pnRaddFsuConfig.setMaxPageNumber(3);
        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(new ObjectMapper());
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(null)));
        ArrayList<RaddRegistryRequestEntity> queryResult = new ArrayList<>();
        RequestResponse actualPrepareGlobalResultResult = raddRegistryUtils.prepareGlobalResult(queryResult, true, 2);
        assertTrue(actualPrepareGlobalResultResult.getItems().isEmpty());
        assertEquals(queryResult, actualPrepareGlobalResultResult.getNextPagesKey());
        assertTrue(actualPrepareGlobalResultResult.getMoreResult());
    }

    /**
     * Method under test: {@link RaddRegistryUtils#prepareGlobalResult(List, boolean, int)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testPrepareGlobalResult4() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" because "this.pnRaddFsuConfig" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(new ObjectMapper());
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, null,
                new SecretService(new CachedSecretsManagerConsumer(null)));
        raddRegistryUtils.prepareGlobalResult(new ArrayList<>(), true, 2);
    }

    /**
     * Method under test: {@link RaddRegistryUtils#prepareGlobalResult(List, boolean, int)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testPrepareGlobalResult5() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   it.pagopa.pn.commons.exceptions.PnInternalException: Internal Server Error; nested exception is com.fasterxml.jackson.core.JsonParseException: Unrecognized token 'Original': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')
        //    at [Source: (String)"Original Request"; line: 1, column: 9]
        //       at it.pagopa.pn.radd.utils.ObjectMapperUtil.toObject(ObjectMapperUtil.java:30)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.lambda$prepareGlobalResult$5(RaddRegistryUtils.java:183)
        //       at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
        //       at java.util.stream.SliceOps$1$1.accept(SliceOps.java:200)
        //       at java.util.ArrayList$ArrayListSpliterator.tryAdvance(ArrayList.java:1602)
        //       at java.util.stream.ReferencePipeline.forEachWithCancel(ReferencePipeline.java:129)
        //       at java.util.stream.AbstractPipeline.copyIntoWithCancel(AbstractPipeline.java:527)
        //       at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:513)
        //       at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
        //       at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:575)
        //       at java.util.stream.AbstractPipeline.evaluateToArrayNode(AbstractPipeline.java:260)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:616)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:622)
        //       at java.util.stream.ReferencePipeline.toList(ReferencePipeline.java:627)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:188)
        //   com.fasterxml.jackson.core.JsonParseException: Unrecognized token 'Original': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')
        //    at [Source: (String)"Original Request"; line: 1, column: 9]
        //       at com.fasterxml.jackson.core.JsonParser._constructError(JsonParser.java:2391)
        //       at com.fasterxml.jackson.core.base.ParserMinimalBase._reportError(ParserMinimalBase.java:745)
        //       at com.fasterxml.jackson.core.json.ReaderBasedJsonParser._reportInvalidToken(ReaderBasedJsonParser.java:2961)
        //       at com.fasterxml.jackson.core.json.ReaderBasedJsonParser._handleOddValue(ReaderBasedJsonParser.java:2002)
        //       at com.fasterxml.jackson.core.json.ReaderBasedJsonParser.nextToken(ReaderBasedJsonParser.java:802)
        //       at com.fasterxml.jackson.databind.ObjectMapper._initForReading(ObjectMapper.java:4761)
        //       at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4667)
        //       at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3629)
        //       at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3597)
        //       at it.pagopa.pn.radd.utils.ObjectMapperUtil.toObject(ObjectMapperUtil.java:28)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.lambda$prepareGlobalResult$5(RaddRegistryUtils.java:183)
        //       at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
        //       at java.util.stream.SliceOps$1$1.accept(SliceOps.java:200)
        //       at java.util.ArrayList$ArrayListSpliterator.tryAdvance(ArrayList.java:1602)
        //       at java.util.stream.ReferencePipeline.forEachWithCancel(ReferencePipeline.java:129)
        //       at java.util.stream.AbstractPipeline.copyIntoWithCancel(AbstractPipeline.java:527)
        //       at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:513)
        //       at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
        //       at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:575)
        //       at java.util.stream.AbstractPipeline.evaluateToArrayNode(AbstractPipeline.java:260)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:616)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:622)
        //       at java.util.stream.ReferencePipeline.toList(ReferencePipeline.java:627)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:188)
        //   See https://diff.blue/R013 to resolve this issue.

        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(new ObjectMapper());
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(null)));

        RaddRegistryRequestEntity raddRegistryRequestEntity = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity.setCorrelationId("42");
        raddRegistryRequestEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setCxId("42");
        raddRegistryRequestEntity.setError("An error occurred");
        raddRegistryRequestEntity.setOriginalRequest("Original Request");
        raddRegistryRequestEntity.setPk("Pk");
        raddRegistryRequestEntity.setRegistryId("42");
        raddRegistryRequestEntity.setRequestId("42");
        raddRegistryRequestEntity.setStatus("Status");
        raddRegistryRequestEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setZipCode("21654");

        ArrayList<RaddRegistryRequestEntity> queryResult = new ArrayList<>();
        queryResult.add(raddRegistryRequestEntity);
        raddRegistryUtils.prepareGlobalResult(queryResult, true, 2);
    }

    /**
     * Method under test: {@link RaddRegistryUtils#prepareGlobalResult(List, boolean, int)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testPrepareGlobalResult6() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   it.pagopa.pn.commons.exceptions.PnInternalException: Internal Server Error; nested exception is com.fasterxml.jackson.core.JsonParseException: Unrecognized token 'RADD_ALT_JSON_PROCESSING': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')
        //    at [Source: (String)"RADD_ALT_JSON_PROCESSING"; line: 1, column: 25]
        //       at it.pagopa.pn.radd.utils.ObjectMapperUtil.toObject(ObjectMapperUtil.java:30)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.lambda$prepareGlobalResult$5(RaddRegistryUtils.java:183)
        //       at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
        //       at java.util.stream.SliceOps$1$1.accept(SliceOps.java:200)
        //       at java.util.ArrayList$ArrayListSpliterator.tryAdvance(ArrayList.java:1602)
        //       at java.util.stream.ReferencePipeline.forEachWithCancel(ReferencePipeline.java:129)
        //       at java.util.stream.AbstractPipeline.copyIntoWithCancel(AbstractPipeline.java:527)
        //       at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:513)
        //       at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
        //       at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:575)
        //       at java.util.stream.AbstractPipeline.evaluateToArrayNode(AbstractPipeline.java:260)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:616)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:622)
        //       at java.util.stream.ReferencePipeline.toList(ReferencePipeline.java:627)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:188)
        //   com.fasterxml.jackson.core.JsonParseException: Unrecognized token 'RADD_ALT_JSON_PROCESSING': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')
        //    at [Source: (String)"RADD_ALT_JSON_PROCESSING"; line: 1, column: 25]
        //       at com.fasterxml.jackson.core.JsonParser._constructError(JsonParser.java:2391)
        //       at com.fasterxml.jackson.core.base.ParserMinimalBase._reportError(ParserMinimalBase.java:745)
        //       at com.fasterxml.jackson.core.json.ReaderBasedJsonParser._reportInvalidToken(ReaderBasedJsonParser.java:2961)
        //       at com.fasterxml.jackson.core.json.ReaderBasedJsonParser._handleOddValue(ReaderBasedJsonParser.java:2002)
        //       at com.fasterxml.jackson.core.json.ReaderBasedJsonParser.nextToken(ReaderBasedJsonParser.java:802)
        //       at com.fasterxml.jackson.databind.ObjectMapper._initForReading(ObjectMapper.java:4761)
        //       at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4667)
        //       at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3629)
        //       at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3597)
        //       at it.pagopa.pn.radd.utils.ObjectMapperUtil.toObject(ObjectMapperUtil.java:28)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.lambda$prepareGlobalResult$5(RaddRegistryUtils.java:183)
        //       at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
        //       at java.util.stream.SliceOps$1$1.accept(SliceOps.java:200)
        //       at java.util.ArrayList$ArrayListSpliterator.tryAdvance(ArrayList.java:1602)
        //       at java.util.stream.ReferencePipeline.forEachWithCancel(ReferencePipeline.java:129)
        //       at java.util.stream.AbstractPipeline.copyIntoWithCancel(AbstractPipeline.java:527)
        //       at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:513)
        //       at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
        //       at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:575)
        //       at java.util.stream.AbstractPipeline.evaluateToArrayNode(AbstractPipeline.java:260)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:616)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:622)
        //       at java.util.stream.ReferencePipeline.toList(ReferencePipeline.java:627)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:188)
        //   See https://diff.blue/R013 to resolve this issue.

        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(new ObjectMapper());
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(null)));

        RaddRegistryRequestEntity raddRegistryRequestEntity = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity.setCorrelationId("42");
        raddRegistryRequestEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setCxId("42");
        raddRegistryRequestEntity.setError("An error occurred");
        raddRegistryRequestEntity.setOriginalRequest("Original Request");
        raddRegistryRequestEntity.setPk("Pk");
        raddRegistryRequestEntity.setRegistryId("42");
        raddRegistryRequestEntity.setRequestId("42");
        raddRegistryRequestEntity.setStatus("Status");
        raddRegistryRequestEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setZipCode("21654");

        RaddRegistryRequestEntity raddRegistryRequestEntity2 = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity2.setCorrelationId("Errore durante il processo di mapping json.");
        raddRegistryRequestEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity2.setCxId("Errore durante il processo di mapping json.");
        raddRegistryRequestEntity2.setError("Errore durante il processo di mapping json.");
        raddRegistryRequestEntity2.setOriginalRequest("RADD_ALT_JSON_PROCESSING");
        raddRegistryRequestEntity2.setPk("RADD_ALT_JSON_PROCESSING");
        raddRegistryRequestEntity2.setRegistryId("Errore durante il processo di mapping json.");
        raddRegistryRequestEntity2.setRequestId("Errore durante il processo di mapping json.");
        raddRegistryRequestEntity2.setStatus("RADD_ALT_JSON_PROCESSING");
        raddRegistryRequestEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity2.setZipCode("OX1 1PT");

        ArrayList<RaddRegistryRequestEntity> queryResult = new ArrayList<>();
        queryResult.add(raddRegistryRequestEntity2);
        queryResult.add(raddRegistryRequestEntity);
        raddRegistryUtils.prepareGlobalResult(queryResult, true, 2);
    }

    /**
     * Method under test: {@link RaddRegistryUtils#prepareGlobalResult(List, boolean, int)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testPrepareGlobalResult7() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   it.pagopa.pn.commons.exceptions.PnInternalException: Internal Server Error; nested exception is com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot construct instance of `it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.OriginalRequest` (although at least one Creator exists): no int/Int-argument constructor/factory method to deserialize from Number value (42)
        //    at [Source: (String)"42"; line: 1, column: 1]
        //       at it.pagopa.pn.radd.utils.ObjectMapperUtil.toObject(ObjectMapperUtil.java:30)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.lambda$prepareGlobalResult$5(RaddRegistryUtils.java:183)
        //       at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
        //       at java.util.stream.SliceOps$1$1.accept(SliceOps.java:200)
        //       at java.util.ArrayList$ArrayListSpliterator.tryAdvance(ArrayList.java:1602)
        //       at java.util.stream.ReferencePipeline.forEachWithCancel(ReferencePipeline.java:129)
        //       at java.util.stream.AbstractPipeline.copyIntoWithCancel(AbstractPipeline.java:527)
        //       at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:513)
        //       at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
        //       at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:575)
        //       at java.util.stream.AbstractPipeline.evaluateToArrayNode(AbstractPipeline.java:260)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:616)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:622)
        //       at java.util.stream.ReferencePipeline.toList(ReferencePipeline.java:627)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:188)
        //   com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot construct instance of `it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.OriginalRequest` (although at least one Creator exists): no int/Int-argument constructor/factory method to deserialize from Number value (42)
        //    at [Source: (String)"42"; line: 1, column: 1]
        //       at com.fasterxml.jackson.databind.exc.MismatchedInputException.from(MismatchedInputException.java:63)
        //       at com.fasterxml.jackson.databind.DeserializationContext.reportInputMismatch(DeserializationContext.java:1728)
        //       at com.fasterxml.jackson.databind.DeserializationContext.handleMissingInstantiator(DeserializationContext.java:1353)
        //       at com.fasterxml.jackson.databind.deser.ValueInstantiator.createFromInt(ValueInstantiator.java:324)
        //       at com.fasterxml.jackson.databind.deser.std.StdValueInstantiator.createFromInt(StdValueInstantiator.java:376)
        //       at com.fasterxml.jackson.databind.deser.BeanDeserializerBase.deserializeFromNumber(BeanDeserializerBase.java:1442)
        //       at com.fasterxml.jackson.databind.deser.BeanDeserializer._deserializeOther(BeanDeserializer.java:199)
        //       at com.fasterxml.jackson.databind.deser.BeanDeserializer.deserialize(BeanDeserializer.java:187)
        //       at com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.readRootValue(DefaultDeserializationContext.java:323)
        //       at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4674)
        //       at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3629)
        //       at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3597)
        //       at it.pagopa.pn.radd.utils.ObjectMapperUtil.toObject(ObjectMapperUtil.java:28)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.lambda$prepareGlobalResult$5(RaddRegistryUtils.java:183)
        //       at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
        //       at java.util.stream.SliceOps$1$1.accept(SliceOps.java:200)
        //       at java.util.ArrayList$ArrayListSpliterator.tryAdvance(ArrayList.java:1602)
        //       at java.util.stream.ReferencePipeline.forEachWithCancel(ReferencePipeline.java:129)
        //       at java.util.stream.AbstractPipeline.copyIntoWithCancel(AbstractPipeline.java:527)
        //       at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:513)
        //       at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
        //       at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:575)
        //       at java.util.stream.AbstractPipeline.evaluateToArrayNode(AbstractPipeline.java:260)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:616)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:622)
        //       at java.util.stream.ReferencePipeline.toList(ReferencePipeline.java:627)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:188)
        //   See https://diff.blue/R013 to resolve this issue.

        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(new ObjectMapper());
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(null)));

        RaddRegistryRequestEntity raddRegistryRequestEntity = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity.setCorrelationId("42");
        raddRegistryRequestEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setCxId("42");
        raddRegistryRequestEntity.setError("An error occurred");
        raddRegistryRequestEntity.setOriginalRequest("42");
        raddRegistryRequestEntity.setPk("Pk");
        raddRegistryRequestEntity.setRegistryId("42");
        raddRegistryRequestEntity.setRequestId("42");
        raddRegistryRequestEntity.setStatus("Status");
        raddRegistryRequestEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setZipCode("21654");

        ArrayList<RaddRegistryRequestEntity> queryResult = new ArrayList<>();
        queryResult.add(raddRegistryRequestEntity);
        raddRegistryUtils.prepareGlobalResult(queryResult, true, 2);
    }

    /**
     * Method under test: {@link RaddRegistryUtils#prepareGlobalResult(List, boolean, int)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testPrepareGlobalResult8() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   it.pagopa.pn.commons.exceptions.PnInternalException: Internal Server Error; nested exception is com.fasterxml.jackson.databind.exc.MismatchedInputException: No content to map due to end-of-input
        //    at [Source: (String)""; line: 1, column: 0]
        //       at it.pagopa.pn.radd.utils.ObjectMapperUtil.toObject(ObjectMapperUtil.java:30)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.lambda$prepareGlobalResult$5(RaddRegistryUtils.java:183)
        //       at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
        //       at java.util.stream.SliceOps$1$1.accept(SliceOps.java:200)
        //       at java.util.ArrayList$ArrayListSpliterator.tryAdvance(ArrayList.java:1602)
        //       at java.util.stream.ReferencePipeline.forEachWithCancel(ReferencePipeline.java:129)
        //       at java.util.stream.AbstractPipeline.copyIntoWithCancel(AbstractPipeline.java:527)
        //       at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:513)
        //       at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
        //       at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:575)
        //       at java.util.stream.AbstractPipeline.evaluateToArrayNode(AbstractPipeline.java:260)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:616)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:622)
        //       at java.util.stream.ReferencePipeline.toList(ReferencePipeline.java:627)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:188)
        //   com.fasterxml.jackson.databind.exc.MismatchedInputException: No content to map due to end-of-input
        //    at [Source: (String)""; line: 1, column: 0]
        //       at com.fasterxml.jackson.databind.exc.MismatchedInputException.from(MismatchedInputException.java:59)
        //       at com.fasterxml.jackson.databind.ObjectMapper._initForReading(ObjectMapper.java:4765)
        //       at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4667)
        //       at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3629)
        //       at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3597)
        //       at it.pagopa.pn.radd.utils.ObjectMapperUtil.toObject(ObjectMapperUtil.java:28)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.lambda$prepareGlobalResult$5(RaddRegistryUtils.java:183)
        //       at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
        //       at java.util.stream.SliceOps$1$1.accept(SliceOps.java:200)
        //       at java.util.ArrayList$ArrayListSpliterator.tryAdvance(ArrayList.java:1602)
        //       at java.util.stream.ReferencePipeline.forEachWithCancel(ReferencePipeline.java:129)
        //       at java.util.stream.AbstractPipeline.copyIntoWithCancel(AbstractPipeline.java:527)
        //       at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:513)
        //       at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
        //       at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:575)
        //       at java.util.stream.AbstractPipeline.evaluateToArrayNode(AbstractPipeline.java:260)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:616)
        //       at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:622)
        //       at java.util.stream.ReferencePipeline.toList(ReferencePipeline.java:627)
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:188)
        //   See https://diff.blue/R013 to resolve this issue.

        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(new ObjectMapper());
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(null)));

        RaddRegistryRequestEntity raddRegistryRequestEntity = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity.setCorrelationId("42");
        raddRegistryRequestEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setCxId("42");
        raddRegistryRequestEntity.setError("An error occurred");
        raddRegistryRequestEntity.setOriginalRequest("");
        raddRegistryRequestEntity.setPk("Pk");
        raddRegistryRequestEntity.setRegistryId("42");
        raddRegistryRequestEntity.setRequestId("42");
        raddRegistryRequestEntity.setStatus("Status");
        raddRegistryRequestEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setZipCode("21654");

        ArrayList<RaddRegistryRequestEntity> queryResult = new ArrayList<>();
        queryResult.add(raddRegistryRequestEntity);
        raddRegistryUtils.prepareGlobalResult(queryResult, true, 2);
    }

    /**
     * Method under test: {@link RaddRegistryUtils#prepareGlobalResult(List, boolean, int)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testPrepareGlobalResult9() throws JsonProcessingException {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(Mockito.<String>any(), Mockito.<Class<OriginalRequest>>any()))
                .thenReturn(new OriginalRequest());
        ObjectMapperUtil objectMapperUtil = new ObjectMapperUtil(objectMapper);
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(null)));

        RaddRegistryRequestEntity raddRegistryRequestEntity = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity.setCorrelationId("42");
        raddRegistryRequestEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setCxId("42");
        raddRegistryRequestEntity.setError("An error occurred");
        raddRegistryRequestEntity.setOriginalRequest("Original Request");
        raddRegistryRequestEntity.setPk("Pk");
        raddRegistryRequestEntity.setRegistryId("42");
        raddRegistryRequestEntity.setRequestId("42");
        raddRegistryRequestEntity.setStatus("Status");
        raddRegistryRequestEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setZipCode("21654");

        ArrayList<RaddRegistryRequestEntity> queryResult = new ArrayList<>();
        queryResult.add(raddRegistryRequestEntity);
        raddRegistryUtils.prepareGlobalResult(queryResult, true, 2);
    }

    /**
     * Method under test: {@link RaddRegistryUtils#prepareGlobalResult(List, boolean, int)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testPrepareGlobalResult10() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        ObjectMapperUtil objectMapperUtil = mock(ObjectMapperUtil.class);
        when(objectMapperUtil.toObject(Mockito.<String>any(), Mockito.<Class<OriginalRequest>>any()))
                .thenReturn(new OriginalRequest());
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(null)));

        RaddRegistryRequestEntity raddRegistryRequestEntity = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity.setCorrelationId("42");
        raddRegistryRequestEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setCxId("42");
        raddRegistryRequestEntity.setError("An error occurred");
        raddRegistryRequestEntity.setOriginalRequest("Original Request");
        raddRegistryRequestEntity.setPk("Pk");
        raddRegistryRequestEntity.setRegistryId("42");
        raddRegistryRequestEntity.setRequestId("42");
        raddRegistryRequestEntity.setStatus("Status");
        raddRegistryRequestEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setZipCode("21654");

        ArrayList<RaddRegistryRequestEntity> queryResult = new ArrayList<>();
        queryResult.add(raddRegistryRequestEntity);
        raddRegistryUtils.prepareGlobalResult(queryResult, true, 2);
    }

    /**
     * Method under test: {@link RaddRegistryUtils#prepareGlobalResult(List, boolean, int)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testPrepareGlobalResult11() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "it.pagopa.pn.radd.config.PnRaddFsuConfig.getMaxPageNumber()" is null
        //       at it.pagopa.pn.radd.utils.RaddRegistryUtils.prepareGlobalResult(RaddRegistryUtils.java:192)
        //   See https://diff.blue/R013 to resolve this issue.

        ObjectMapperUtil objectMapperUtil = mock(ObjectMapperUtil.class);
        when(objectMapperUtil.toObject(Mockito.<String>any(), Mockito.<Class<OriginalRequest>>any()))
                .thenReturn(new OriginalRequest());
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        RaddRegistryUtils raddRegistryUtils = new RaddRegistryUtils(objectMapperUtil, pnRaddFsuConfig,
                new SecretService(new CachedSecretsManagerConsumer(null)));

        RaddRegistryRequestEntity raddRegistryRequestEntity = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity.setCorrelationId("42");
        raddRegistryRequestEntity
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setCxId("42");
        raddRegistryRequestEntity.setError("An error occurred");
        raddRegistryRequestEntity.setOriginalRequest("Original Request");
        raddRegistryRequestEntity.setPk("Pk");
        raddRegistryRequestEntity.setRegistryId("42");
        raddRegistryRequestEntity.setRequestId("42");
        raddRegistryRequestEntity.setStatus("Status");
        raddRegistryRequestEntity
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity.setZipCode("21654");

        RaddRegistryRequestEntity raddRegistryRequestEntity2 = new RaddRegistryRequestEntity();
        raddRegistryRequestEntity2.setCorrelationId("Correlation Id");
        raddRegistryRequestEntity2
                .setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity2.setCxId("Cx Id");
        raddRegistryRequestEntity2.setError("Error");
        raddRegistryRequestEntity2
                .setOriginalRequest("it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity");
        raddRegistryRequestEntity2.setPk("it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity");
        raddRegistryRequestEntity2.setRegistryId("Registry Id");
        raddRegistryRequestEntity2.setRequestId("Request Id");
        raddRegistryRequestEntity2.setStatus("it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity");
        raddRegistryRequestEntity2
                .setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        raddRegistryRequestEntity2.setZipCode("OX1 1PT");

        ArrayList<RaddRegistryRequestEntity> queryResult = new ArrayList<>();
        queryResult.add(raddRegistryRequestEntity2);
        queryResult.add(raddRegistryRequestEntity);
        raddRegistryUtils.prepareGlobalResult(queryResult, true, 2);
    }
}

