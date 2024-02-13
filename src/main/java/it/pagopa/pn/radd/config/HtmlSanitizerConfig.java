package it.pagopa.pn.radd.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.utils.HtmlSanitizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HtmlSanitizerConfig {

    @Bean
    public HtmlSanitizer htmlSanitizer(ObjectMapper objectMapper, PnRaddFsuConfig pnRaddFsuConfig) {
        return new HtmlSanitizer(objectMapper, pnRaddFsuConfig.getSanitizeMode());
    }
}
