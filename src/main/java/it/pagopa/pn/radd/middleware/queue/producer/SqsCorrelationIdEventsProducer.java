package it.pagopa.pn.radd.middleware.queue.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.AbstractSqsMomProducer;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import it.pagopa.pn.radd.middleware.queue.event.CorrelationIdEvent;

@Component
public class SqsCorrelationIdEventsProducer extends AbstractSqsMomProducer<CorrelationIdEvent> implements CorrelationIdEventsProducer {
    public SqsCorrelationIdEventsProducer(SqsClient sqsClient, ObjectMapper objectMapper, PnRaddFsuConfig cfg) {
        super(sqsClient, cfg.getSqs().getInputQueueName(), objectMapper, CorrelationIdEvent.class);
    }
}
