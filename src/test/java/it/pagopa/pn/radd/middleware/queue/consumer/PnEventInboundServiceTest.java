package it.pagopa.pn.radd.middleware.queue.consumer;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class PnEventInboundServiceTest {
    /**
     * Method under test: {@link PnEventInboundService#customRouter()}
     */
    @Test
    void testCustomRouter() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R002 Missing observers.
        //   Diffblue Cover was unable to create an assertion.
        //   Add getters for the following fields or make them package-private:
        //     1.this$0

        assertNull((new PnEventInboundService(new EventHandler(), new PnRaddFsuConfig())).customRouter().functionDefinition(null));
    }
}

