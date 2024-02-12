package it.pagopa.pn.radd.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.pn.radd.config.HtmlSanitizerConfig;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
class HtmlSanitizerConfigTest {

    private HtmlSanitizerConfig config;

    @BeforeEach
    void setUp() {

        config = new HtmlSanitizerConfig();
    }

    @Test
    void htmlSanitizer() {
        ObjectMapper objectMapper = buildObjectMapper();
        PnRaddFsuConfig pnRaddFsuConfig = new PnRaddFsuConfig();
        pnRaddFsuConfig.setSanitizeMode(HtmlSanitizer.SanitizeMode.DELETE_HTML);

        HtmlSanitizer actual = config.htmlSanitizer(objectMapper, pnRaddFsuConfig);

        Assertions.assertNotNull(actual);
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = ((JsonMapper.Builder)((JsonMapper.Builder)JsonMapper.builder().configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)).build();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}