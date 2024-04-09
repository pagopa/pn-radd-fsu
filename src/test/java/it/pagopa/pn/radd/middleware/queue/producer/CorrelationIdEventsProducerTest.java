package it.pagopa.pn.radd.middleware.queue.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.queue.event.CorrelationIdEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CorrelationIdEventsProducerTest {

    private CorrelationIdEventsProducer correlationIdEventsProducer;

    @Mock
    private SqsClient sqsClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PnRaddFsuConfig cfg;

    @BeforeEach
    void setup() {
        PnRaddFsuConfig.Sqs sqs = mock(PnRaddFsuConfig.Sqs.class);
        Mockito.when( cfg.getSqs() ).thenReturn (sqs);
        Mockito.when( sqs.getInputQueueName() ).thenReturn("queueName");

        GetQueueUrlResponse response = GetQueueUrlResponse.builder()
                .queueUrl("queueUrl")
                .build();

        Mockito.when(sqsClient.getQueueUrl(Mockito.any(software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest.class))).thenReturn(response);
        correlationIdEventsProducer = new SqsCorrelationIdEventsProducer(sqsClient, objectMapper, cfg);
    }

    @ExtendWith(MockitoExtension.class)
    @Test
    void buildAsseverationEvent() {

        CorrelationIdEvent correlationIdEvent = correlationIdEventsProducer.buildCorrelationIdEvent("correlationId");

        Assertions.assertNotNull(correlationIdEvent);
    }

    @ExtendWith(MockitoExtension.class)
    @Test
    void sendAsseverationEvent() {
        assertThrows(NullPointerException.class, () -> correlationIdEventsProducer.sendCorrelationIdEvent("correlationId"));
    }

}