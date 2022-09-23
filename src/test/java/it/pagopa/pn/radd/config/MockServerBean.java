package it.pagopa.pn.radd.config;


import lombok.extern.slf4j.Slf4j;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.stop.Stop;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class MockServerBean {
    private final ClientAndServer mockServer;
    @Value("${mockserver.bean.port}")
    private int port = 1040;

    public MockServerBean(){
        this.initializationExpection();
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

    public void kill(){
        Stop.stopQuietly(mockServer);
        log.info("MockServer is died");
    }

}
