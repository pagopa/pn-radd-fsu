package it.pagopa.pn.radd.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.pojo.RaddRegistryRequest;
import it.pagopa.pn.radd.pojo.WrappedRaddRegistryOriginalRequest;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaddRegistryRequestEntityMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RaddRegistryRequestEntityMapper mapper = new RaddRegistryRequestEntityMapper(new ObjectMapperUtil(objectMapper));

    @Test
    void testRetrieveOriginalRequestPhoneNumberOk() throws IOException {
        List<RaddRegistryRequest> requests = readCsvFile("src/test/resources/radd-registry-telefono-ok.csv");

        for (RaddRegistryRequest request : requests) {
            WrappedRaddRegistryOriginalRequest result = mapper.retrieveOriginalRequest(request);
            assertNotNull(result);
            assertTrue(result.getErrors().isEmpty());
            assertNotNull(result.getRequest());
        }
    }

    @Test
    void testRetrieveOriginalRequestPhoneNumberKo() throws IOException {
        List<RaddRegistryRequest> requests = readCsvFile("src/test/resources/radd-registry-telefono-ko.csv");

        for (RaddRegistryRequest request : requests) {
            WrappedRaddRegistryOriginalRequest result = mapper.retrieveOriginalRequest(request);
            assertNotNull(result);
            assertTrue(result.getErrors().stream().allMatch(e -> e.contains("Il campo telefono non rispetta il formato definito")));
            assertNotNull(result.getRequest());
        }
    }

    private List<RaddRegistryRequest> readCsvFile(String filePath) throws IOException {
        List<RaddRegistryRequest> requests = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                RaddRegistryRequest request = new RaddRegistryRequest(
                        values[0], // paese
                        values[1], // citta
                        values[2], // provincia
                        values[3], // cap
                        values[4], // via
                        values[5], // dataInizioValidita
                        values[6], // dataFineValidita
                        values[7], // descrizione
                        values[8], // orariApertura
                        values[9], // coordinateGeoReferenziali
                        values[10], // telefono
                        values[11], // capacita
                        values[12]  // externalCode
                );
                requests.add(request);
            }
        }
        return requests;
    }
}