package it.pagopa.pn.radd.middleware.queue.consumer;

import it.pagopa.pn.radd.middleware.queue.consumer.handler.RaddAltInputEventHandler;
import it.pagopa.pn.radd.middleware.queue.event.PnRaddAltNormalizeRequestEvent;
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
    private Message<PnRaddAltNormalizeRequestEvent.Payload> messageNormalizeRequest;

    @Mock
    private Message<it.pagopa.pn.radd.middleware.queue.consumer.event.ImportCompletedRequestEvent.Payload> messageImportCompleted;

    @InjectMocks
    private RaddAltInputEventHandler raddAltInputEventHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldHandleNormalizeRequestSuccessfully() {
        PnRaddAltNormalizeRequestEvent.Payload event = mock(PnRaddAltNormalizeRequestEvent.Payload.class);
        when(event.getCorrelationId()).thenReturn("correlationId");
        when(messageNormalizeRequest.getPayload()).thenReturn(event);
        when(registryService.handleNormalizeRequestEvent(event)).thenReturn(Mono.empty());

        raddAltInputEventHandler.pnRaddAltInputNormalizeRequestConsumer().accept(messageNormalizeRequest);

        verify(registryService, times(1)).handleNormalizeRequestEvent(event);
    }

    @Test
    void shouldHandleImportCompletedSuccessfully() {
        it.pagopa.pn.radd.middleware.queue.consumer.event.ImportCompletedRequestEvent.Payload event = mock(it.pagopa.pn.radd.middleware.queue.consumer.event.ImportCompletedRequestEvent.Payload.class);
        when(event.getCxId()).thenReturn("cxId");
        when(event.getRequestId()).thenReturn("requestId");
        when(messageImportCompleted.getPayload()).thenReturn(event);

        when(registryService.handleImportCompletedRequest(event)).thenReturn(Mono.empty());

        raddAltInputEventHandler.pnRaddAltImportCompletedRequestConsumer().accept(messageImportCompleted);

        verify(registryService, times(1)).handleImportCompletedRequest(event);
    }
}