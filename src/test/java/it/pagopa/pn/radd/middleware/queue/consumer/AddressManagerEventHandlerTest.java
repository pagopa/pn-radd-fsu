package it.pagopa.pn.radd.middleware.queue.consumer;

import it.pagopa.pn.radd.middleware.queue.consumer.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.*;

class AddressManagerEventHandlerTest {

    @Mock
    private RegistryImportService registryImportService;

    @Mock
    private Message<PnAddressManagerEvent> message;

    @InjectMocks
    private AddressManagerEventHandler addressManagerEventHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldHandleMessageSuccessfully() {
        PnAddressManagerEvent event = new PnAddressManagerEvent();
        PnAddressManagerEvent.Payload payload = mock(PnAddressManagerEvent.Payload.class);
        when(payload.getCorrelationId()).thenReturn("correlationId");
        event.setPayload(payload);
        when(message.getPayload()).thenReturn(event);
        when(registryImportService.handleAddressManagerEvent(event)).thenReturn(Mono.empty());

        addressManagerEventHandler.pnAddressManagerEventInboundConsumer().accept(message);

        verify(registryImportService, times(1)).handleAddressManagerEvent(event);
    }
}