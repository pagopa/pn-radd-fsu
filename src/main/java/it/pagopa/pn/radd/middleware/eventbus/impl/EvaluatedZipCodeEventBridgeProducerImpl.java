package it.pagopa.pn.radd.middleware.eventbus.impl;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.eventbus.AbstractEventBridgeProducer;
import it.pagopa.pn.radd.pojo.EvaluatedZipCodeEvent;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.eventbridge.EventBridgeAsyncClient;

@Component
public class EvaluatedZipCodeEventBridgeProducerImpl extends AbstractEventBridgeProducer<EvaluatedZipCodeEvent> {
    protected EvaluatedZipCodeEventBridgeProducerImpl(EventBridgeAsyncClient amazonEventBridge, PnRaddFsuConfig pnRaddFsuConfig, ObjectMapperUtil objectMapperUtil) {
        super(amazonEventBridge, pnRaddFsuConfig.getEventBus().getSource(), pnRaddFsuConfig.getEventBus().getDetailType(), pnRaddFsuConfig.getEventBus().getName(), objectMapperUtil);
    }
}
