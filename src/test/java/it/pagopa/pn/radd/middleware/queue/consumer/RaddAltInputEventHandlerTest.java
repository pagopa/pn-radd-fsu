package it.pagopa.pn.radd.middleware.queue.consumer;

import it.pagopa.pn.radd.middleware.queue.consumer.event.PnRaddAltNormalizeRequestEvent;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class RaddAltInputEventHandlerTest {

    @Mock
    private RegistryService registryService;

    @Mock
    private Message<PnRaddAltNormalizeRequestEvent.Payload> message;

    @InjectMocks
    private RaddAltInputEventHandler raddAltInputEventHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldHandleMessageSuccessfully() {
        PnRaddAltNormalizeRequestEvent.Payload event = new PnRaddAltNormalizeRequestEvent.Payload();
        when(message.getPayload()).thenReturn(event);
        when(registryService.handleNormalizeRequestEvent(event)).thenReturn(Mono.empty());

        raddAltInputEventHandler.pnRaddAltInputNormalizeRequestConsumer().accept(message);

        verify(registryService, times(1)).handleNormalizeRequestEvent(event);
    }
}