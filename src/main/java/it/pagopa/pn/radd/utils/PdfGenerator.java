package it.pagopa.pn.radd.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class PdfGenerator {
    private static final String FIELD_DENOMINATION = "denomination";

    private final DocumentComposition documentComposition;

    public PdfGenerator(DocumentComposition documentComposition) {
        this.documentComposition = documentComposition;
    }

    public byte[] generateCoverFile(String denomination) throws IOException {

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put(FIELD_DENOMINATION, denomination);
        return documentComposition.executePdfTemplate(
                DocumentComposition.TemplateType.COVER_FILE,
                templateModel
        );

    }
}

