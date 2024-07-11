package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.pojo.RaddRegistryRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.io.*;


@ExtendWith(MockitoExtension.class)
class CsvServiceTest {

    @InjectMocks
    CsvService csvService;

    @Test
    void readCsvOk() throws IOException {
        File file = new File("src/test/resources", "radd-registry.csv");
        InputStream inputStream = new FileInputStream(file);

        StepVerifier.create(csvService.readItemsFromCsv(RaddRegistryRequest.class, inputStream.readAllBytes(), 1))
                .expectNextMatches(raddRegistryRequests -> raddRegistryRequests.size() == 8)
                .verifyComplete();
    }

    @Test
    void readCsvKo() throws IOException {
        File file = new File("src/test/resources", "radd-registry-error.csv");
        InputStream inputStream = new FileInputStream(file);
        StepVerifier.create(csvService.readItemsFromCsv(RaddRegistryRequest.class, inputStream.readAllBytes(), 1))
                .expectError(RaddGenericException.class);
    }

}