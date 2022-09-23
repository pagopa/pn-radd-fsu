package it.pagopa.pn.radd.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
public abstract class BaseTest {

    @Autowired
    private MockServerBean mockServer;


    @AfterEach
    public void killMockServer(){
        mockServer.kill();
    }

}
