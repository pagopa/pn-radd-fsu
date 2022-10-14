package it.pagopa.pn.radd.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseTest {



    @Slf4j
    @SpringBootTest
    @ActiveProfiles("test")
    @AutoConfigureMockMvc
    public static abstract class WithMockServer{
        @Autowired
        private MockServerBean mockServer;

        public WithMockServer(){
            log.info(this.getClass().getSimpleName());
        }
    }

}
