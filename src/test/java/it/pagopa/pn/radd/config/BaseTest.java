package it.pagopa.pn.radd.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.stop.Stop;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URL;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
public abstract class BaseTest {
    private ClientAndServer mockServer;

    @BeforeEach
    public void setUpMockServer(){
        log.info("Mock Server : ");
        Resource resource = new ClassPathResource("webhook.json");
        try {
            String path = resource.getFile().getAbsolutePath();
            log.info("Path : {} ", path);
            ConfigurationProperties.initializationJsonPath(path);
        } catch (IOException e) {
            log.warn("File webhook not found");
        }

        this.mockServer = ClientAndServer.startClientAndServer(1040);

        log.info(" - started");
    }

    @AfterEach
    public void killMockServer(){
        Stop.stopQuietly(mockServer);
        log.info("Mock server killed");
    }

}
