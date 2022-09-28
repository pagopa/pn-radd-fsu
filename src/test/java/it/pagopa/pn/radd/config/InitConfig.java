package it.pagopa.pn.radd.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class InitConfig {
    @Value("${mockserver.bean.port}")
    private int port;


    @Bean
    @Qualifier("mockServerBean")
    public MockServerBean getMockServer(){
        log.info("Port :  {}", port);
        return new MockServerBean(port);
    }
}
