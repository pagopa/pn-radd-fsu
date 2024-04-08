package it.pagopa.pn.radd.middleware.queue.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.AbstractSqsMomProducer;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.queue.event.RegistryImportProgressEvent;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
public class RegistryImportProgressEventProducer extends AbstractSqsMomProducer<RegistryImportProgressEvent> implements RegistryImportProgressProducer {

    public RegistryImportProgressEventProducer(SqsClient sqsClient, PnRaddFsuConfig config, ObjectMapper objectMapper) {
        super(sqsClient, config.getSqs().getInputQueueName(), objectMapper, RegistryImportProgressEvent.class);
    }
}
