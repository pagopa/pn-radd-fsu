package it.pagopa.pn.radd.middleware.eventbus;

import it.pagopa.pn.api.dto.events.GenericEventBridgeEvent;
import it.pagopa.pn.radd.exception.EventBridgeSendException;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.eventbridge.EventBridgeAsyncClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

import java.util.List;

@Slf4j
@Component
public abstract class AbstractEventBridgeProducer<T extends GenericEventBridgeEvent> implements EventBridgeProducer<T> {

    private final EventBridgeAsyncClient amazonEventBridge;
    private final String eventBusName;
    private final String eventBusDetailType;
    private final String eventBusSource;
    private final ObjectMapperUtil objectMapperUtil;

    protected AbstractEventBridgeProducer(EventBridgeAsyncClient amazonEventBridge, String eventBusSource, String detailType, String name, ObjectMapperUtil objectMapperUtil) {
        this.amazonEventBridge = amazonEventBridge;
        this.eventBusSource = eventBusSource;
        this.eventBusName = name;
        this.eventBusDetailType = detailType;
        this.objectMapperUtil = objectMapperUtil;
    }

    private PutEventsRequest putEventsRequestBuilder(List<T> events) {
        PutEventsRequest putEventsRequest = PutEventsRequest.builder()
                .entries(events.stream()
                    .map(this::buildEventRequest)
                    .toList()
                )
                .build();

        log.debug("PutEventsRequest: {}", putEventsRequest);
        return putEventsRequest;
    }

    private PutEventsRequestEntry buildEventRequest(T event) {
        return PutEventsRequestEntry.builder()
                .eventBusName(eventBusName)
                .detailType(eventBusDetailType)
                .source(eventBusSource)
                .detail(objectMapperUtil.toJson(event.getDetail()))
                .build();
    }

    @Override
    public Mono<Void> sendEvent(T event) {
        return sendEvent(List.of(event));
    }

    @Override
    public Mono<Void> sendEvent(List<T> events) {
        return Mono.fromFuture(amazonEventBridge.putEvents(putEventsRequestBuilder(events)))
                .doOnError(throwable -> log.error("Error sending event on event bridge", throwable))
                .flatMap(response -> {
                    if(response.failedEntryCount() != null && response.failedEntryCount() > 0) {
                        log.error("error sending event on event bus={} response={}", eventBusName, response);
                        return Mono.error(new EventBridgeSendException(String.format("Error sending event on event bus: %s", eventBusName)));
                    }
                    log.debug("Event sent successfully: {}", response.entries());
                    return Mono.empty();
                });
    }
}
