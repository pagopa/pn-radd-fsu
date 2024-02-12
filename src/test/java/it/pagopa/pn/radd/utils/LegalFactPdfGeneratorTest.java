package it.pagopa.pn.radd.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import freemarker.template.Configuration;
import freemarker.template.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class LegalFactPdfGeneratorTest {
    private static final String TEST_DIR_NAME = "target" + File.separator + "generated-test-PDF";
    private static final Path TEST_DIR_PATH = Paths.get(TEST_DIR_NAME);

    private PdfGenerator pdfUtils;

    @BeforeEach
    public void setup() throws IOException {
        Configuration freemarker = new Configuration(new Version(2, 3, 0)); // Version is a final class
        HtmlSanitizer htmlSanitizer = new HtmlSanitizer(buildObjectMapper(),
                HtmlSanitizer.SanitizeMode.ESCAPING);
        DocumentComposition documentComposition = new DocumentComposition(freemarker, htmlSanitizer);
        pdfUtils = new PdfGenerator(documentComposition);

        // create target test folder, if not exists
        if (Files.notExists(TEST_DIR_PATH)) {
            Files.createDirectory(TEST_DIR_PATH);
        }
    }

    @Test
    void generateNotificationReceivedLegalFactTest() throws IOException {
        Path filePath = Paths.get(TEST_DIR_NAME + File.separator + "test_ReceivedLegalFact.pdf");
        Assertions.assertDoesNotThrow(() -> Files.write(filePath,
                pdfUtils.generateCoverFile("denomination")));
        System.out.print("*** Received pdf successfully created at: " + filePath);
    }
    private ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = ((JsonMapper.Builder) ((JsonMapper.Builder) JsonMapper.builder()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)).build();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
