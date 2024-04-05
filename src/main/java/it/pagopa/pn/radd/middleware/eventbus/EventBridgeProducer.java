package it.pagopa.pn.radd.middleware.eventbus;

import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

public interface EventBridgeProducer<T> {
    default Mono<Void> sendEvent(T event) {
        return this.sendEvent(Collections.singletonList(event));
    }

    Mono<Void> sendEvent(List<T> events);
}