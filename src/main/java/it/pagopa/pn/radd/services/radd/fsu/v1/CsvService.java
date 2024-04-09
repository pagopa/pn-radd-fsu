package it.pagopa.pn.radd.services.radd.fsu.v1;

import com.opencsv.bean.CsvToBeanBuilder;
import it.pagopa.pn.radd.exception.RaddGenericException;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.opencsv.ICSVParser.DEFAULT_QUOTE_CHARACTER;


@Component
@RequiredArgsConstructor
@CustomLog
public class CsvService {

    public static final String ERROR_RADD_ALT_READING_CSV = "Error reading CSV: ";

    public <T> Mono<List<T>> readItemsFromCsv(Class<T> csvClass, byte[] file, int skipLines) {
        try {
            StringReader stringReader = new StringReader(new String(file, StandardCharsets.UTF_8));
            CsvToBeanBuilder<T> csvToBeanBuilder = new CsvToBeanBuilder<>(stringReader);
            csvToBeanBuilder.withSeparator(';');
            csvToBeanBuilder.withQuoteChar(DEFAULT_QUOTE_CHARACTER);
            csvToBeanBuilder.withSkipLines(skipLines);
            csvToBeanBuilder.withOrderedResults(true);
            csvToBeanBuilder.withType(csvClass);

            List<T> parsedItems = csvToBeanBuilder.build().parse();
            return Mono.just(new ArrayList<>(parsedItems));
        }catch (Exception e){
            log.error(ERROR_RADD_ALT_READING_CSV + e.getMessage(), e);
            return Mono.error(new RaddGenericException(ERROR_RADD_ALT_READING_CSV + e.getMessage()));
        }
    }
}
