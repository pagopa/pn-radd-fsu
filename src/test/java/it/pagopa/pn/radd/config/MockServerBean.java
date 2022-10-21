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
        this.port = port;
        log.info("Mock server started on : {}", port);
    }

    public void stop(){
        this.mockServer.stop();
    }

    public void initializationExpection(String file){
        log.info("- Initialize Mock Server Expection");
        Resource resource = new ClassPathResource(file);
        try {
            String path = resource.getFile().getAbsolutePath();
            log.info(" - Path : {} ", path);
            ConfigurationProperties.initializationJsonPath(path);
            this.mockServer = ClientAndServer.startClientAndServer(port);
        } catch (IOException e) {
            log.warn(" - File webhook not found");
        }
    }
}
