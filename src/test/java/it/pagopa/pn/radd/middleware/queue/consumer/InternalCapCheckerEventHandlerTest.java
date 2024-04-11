package it.pagopa.pn.radd.middleware.queue.consumer;

import it.pagopa.pn.radd.middleware.queue.consumer.handler.InternalCapCheckerEventHandler;
import it.pagopa.pn.radd.middleware.queue.event.PnInternalCapCheckerEvent;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class InternalCapCheckerEventHandlerTest {

    @Mock
    private RegistryService registryService;

    @Mock
    private Message<PnInternalCapCheckerEvent> message;

    @InjectMocks
    private InternalCapCheckerEventHandler internalCapCheckerEventHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldHandleMessageSuccessfully() {
        PnInternalCapCheckerEvent event = new PnInternalCapCheckerEvent();
        when(message.getPayload()).thenReturn(event);
        when(registryService.handleInternalCapCheckerMessage(event)).thenReturn(Mono.empty());

        internalCapCheckerEventHandler.pnInternalCapCheckerEventInboundConsumer().accept(message);

        verify(registryService, times(1)).handleInternalCapCheckerMessage(event);
    }
}