package it.pagopa.pn.radd.middleware.eventbus.impl;

import it.pagopa.pn.api.dto.events.PnEvaluatedZipCodeEvent;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.eventbus.AbstractEventBridgeProducer;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.eventbridge.EventBridgeAsyncClient;

@Component
public class EvaluatedZipCodeEventBridgeProducerImpl extends AbstractEventBridgeProducer<PnEvaluatedZipCodeEvent> {
    protected EvaluatedZipCodeEventBridgeProducerImpl(EventBridgeAsyncClient amazonEventBridge, PnRaddFsuConfig pnRaddFsuConfig, ObjectMapperUtil objectMapperUtil) {
        super(amazonEventBridge, pnRaddFsuConfig.getEventBus().getSource(), pnRaddFsuConfig.getEventBus().getDetailType(), pnRaddFsuConfig.getEventBus().getName(), objectMapperUtil);
    }
}
