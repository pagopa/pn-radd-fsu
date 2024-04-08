package it.pagopa.pn.radd.middleware.queue.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.AbstractSqsMomProducer;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.queue.event.RaddAltCapCheckerEvent;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
public class SqsRaddAltCapCheckerProducer extends AbstractSqsMomProducer<RaddAltCapCheckerEvent> implements RaddAltCapCheckerProducer {

    public SqsRaddAltCapCheckerProducer(SqsClient sqsClient, ObjectMapper objectMapper, PnRaddFsuConfig cfg) {
        super(sqsClient, cfg.getSqs().getInternalCapCheckerQueueName(), objectMapper, RaddAltCapCheckerEvent.class);
    }
}
