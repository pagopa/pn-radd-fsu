package it.pagopa.pn.radd.config;


import lombok.extern.slf4j.Slf4j;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Slf4j
public class MockServerBean {
    private ClientAndServer mockServer;
    private final int port;

    public MockServerBean(int port){
        this.initializationExpection();
        this.port = port;
        this.mockServer = ClientAndServer.startClientAndServer(port);
        log.info("Mock server started on : {}", port);
    }

    private void initializationExpection(){
        log.info("- Initialize Mock Server Expection");
        Resource resource = new ClassPathResource("webhook.json");
        try {
            String path = resource.getFile().getAbsolutePath();
            log.info(" - Path : {} ", path);
            ConfigurationProperties.initializationJsonPath(path);
        } catch (IOException e) {
            log.warn(" - File webhook not found");
        }
    }
}
