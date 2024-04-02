package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.mapper.AddressManagerOriginalRequestMapper;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.msclient.PnAddressManagerClient;
import it.pagopa.pn.radd.middleware.queue.consumer.event.PnRaddAltNormalizeRequestEvent;
import it.pagopa.pn.radd.pojo.AddressManagerOriginalRequest;
import it.pagopa.pn.radd.pojo.AddressManagerRequest;
import it.pagopa.pn.radd.pojo.AddressManagerRequestAddress;
import it.pagopa.pn.radd.pojo.RegistryRequestStatus;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static it.pagopa.pn.radd.constant.AddressManagerConstant.POSTEL;
import static it.pagopa.pn.radd.constant.ProcessStatus.PROCESS_SERVICE_NORMALIZE_ADDRESS;

@CustomLog
@Service
@RequiredArgsConstructor
public class RaddAltInputService {

    private RaddRegistryRequestDAO raddRegistryRequestDAO;
    private PnAddressManagerClient pnAddressManagerClient;

    public Mono<Void> handleRequest(PnRaddAltNormalizeRequestEvent.Payload payload) {
        log.logStartingProcess(PROCESS_SERVICE_NORMALIZE_ADDRESS);
        AddressManagerRequest request = new AddressManagerRequest();
        request.setCorrelationId(payload.getCorrelationId());

        return raddRegistryRequestDAO.getAllFromCorrelationId(payload.getCorrelationId(), RegistryRequestStatus.NOT_WORKED.name())
                .collectList()
                .zipWhen(entities -> Mono.just(AddressManagerOriginalRequestMapper.getRequestAddressFromOriginalRequest(entities)))
                .flatMap(tuple -> {
                    request.setAddresses(tuple.getT2());
                    return pnAddressManagerClient.normalizeAddresses(request, tuple.getT1()).thenReturn(tuple);
                })
                .flatMap(tuple -> raddRegistryRequestDAO.updateRecordsInPendig(tuple.getT1()))
                .doOnError(ex -> log.logInvokationResultDownstreamFailed(POSTEL, ex.getMessage()));
    }
}
