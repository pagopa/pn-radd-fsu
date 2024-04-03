package it.pagopa.pn.radd.middleware.queue.consumer;

import it.pagopa.pn.radd.middleware.queue.consumer.event.PnAddressManagerEvent;
import it.pagopa.pn.radd.services.radd.fsu.v1.RegistryService;
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
    private RegistryService registryService;

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
        when(message.getPayload()).thenReturn(event);
        when(registryService.handleMessage(event)).thenReturn(Mono.empty());

        addressManagerEventHandler.pnAddressManagerEventInboundConsumer().accept(message);

        verify(registryService, times(1)).handleMessage(event);
    }
}