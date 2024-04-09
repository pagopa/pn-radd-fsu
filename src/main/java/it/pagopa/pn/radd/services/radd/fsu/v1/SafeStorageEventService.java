package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileDownloadResponseDto;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.exception.RaddImportException;
import it.pagopa.pn.radd.mapper.RaddRegistryRequestEntityMapper;
import it.pagopa.pn.radd.middleware.db.RaddRegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.middleware.msclient.DocumentDownloadClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.middleware.queue.producer.CorrelationIdEventsProducer;
import it.pagopa.pn.radd.pojo.RaddRegistryImportStatus;
import it.pagopa.pn.radd.pojo.RaddRegistryOriginalRequest;
import it.pagopa.pn.radd.pojo.RaddRegistryRequest;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.pn.radd.pojo.RegistryRequestStatus.REJECTED;
import static it.pagopa.pn.radd.services.radd.fsu.v1.CsvService.ERROR_RADD_ALT_READING_CSV;

@Service
@CustomLog
@AllArgsConstructor
public class SafeStorageEventService {

    private final RaddRegistryImportDAO pnRaddRegistryImportDAO;
    private final RaddRegistryRequestDAO raddRegistryRequestDAO;
    private final PnSafeStorageClient safeStorageClient;
    private final DocumentDownloadClient documentDownloadClient;
    private final CsvService csvService;
    private final RaddRegistryRequestEntityMapper raddRegistryRequestEntityMapper;
    private final CorrelationIdEventsProducer correlationIdEventsProducer;

    private final String errorCSV = "Malformed CSV";

    public Mono<Void> handleSafeStorageResponse(FileDownloadResponseDto response) {
        String fileKey = response.getKey();
        return pnRaddRegistryImportDAO.getItemByFileKey(fileKey)
                .switchIfEmpty(Mono.error(new RaddImportException(String.format("Import request for FileKey [%s] does not exist", fileKey))))
                .flatMap(raddRegistryImportEntity -> pnRaddRegistryImportDAO.updateStatus(raddRegistryImportEntity, RaddRegistryImportStatus.TAKEN_CHARGE, null))
                .zipWhen(raddRegistryImportEntity -> retrieveAndProcessFile(fileKey))
                .flatMap(tuple -> {
                    if (CollectionUtils.isEmpty(tuple.getT2())) {
                        log.info("Error during import csv for fileKey [{}] - update importEntity with status REJECTED", fileKey);
                        return pnRaddRegistryImportDAO.updateStatus(tuple.getT1(), RaddRegistryImportStatus.REJECTED, errorCSV)
                                .flatMap(importEntity -> Mono.error(new RaddImportException(ERROR_RADD_ALT_READING_CSV)));
                    } else {
                        List<RaddRegistryOriginalRequest> originalRequests = raddRegistryRequestEntityMapper.retrieveOriginalRequest(tuple.getT2());
                        List<RaddRegistryRequestEntity> raddRegistryRequestEntities = raddRegistryRequestEntityMapper.retrieveRaddRegistryRequestEntity(originalRequests, tuple.getT1());
                        Map<String, List<RaddRegistryRequestEntity>> map = groupingRaddRegistryRequest(raddRegistryRequestEntities, 20);
                        return persistRaddRegistryRequest(map)
                                .thenReturn(tuple.getT1());
                    }
                })
                .flatMap(importEntity -> pnRaddRegistryImportDAO.updateStatus(importEntity, RaddRegistryImportStatus.PENDING, null))
                .doOnError(throwable -> log.error("Error during import csv for fileKey [{}] --> ", fileKey, throwable))
                .onErrorResume(RaddImportException.class, e -> Mono.empty())
                .then();

    }

    private Mono<List<RaddRegistryRequest>> retrieveAndProcessFile(String fileKey) {
        return getFile(fileKey)
                .flatMap(fileDownloadResponseDto -> {
                    if (fileDownloadResponseDto.getDownload() != null && StringUtils.isNotBlank(fileDownloadResponseDto.getDownload().getUrl())) {
                        return downloadCSV(fileDownloadResponseDto.getDownload().getUrl());
                    }
                    return Mono.error(new RaddGenericException(String.format("Error during download CSV for fileKey: [%s], Url is null", fileKey)));
                })
                .flatMap(this::readAndProcessCSV);
    }

    private Mono<List<RaddRegistryRequest>> readAndProcessCSV(byte[] bytes) {
        return csvService.readItemsFromCsv(RaddRegistryRequest.class, bytes, 1)
                .onErrorReturn(Collections.emptyList());

    }

    private Mono<Void> persistRaddRegistryRequest(Map<String, List<RaddRegistryRequestEntity>> raddRegistryRequestsMap) {
        return Flux.fromStream(raddRegistryRequestsMap.entrySet()
                        .stream()
                        .filter(stringListEntry -> !stringListEntry.getKey().equals(REJECTED.name())))
                .flatMap(entry -> persistItemsAndSendEvent(entry).thenReturn(raddRegistryRequestsMap))
                .map(stringListMap -> stringListMap.get(REJECTED.name()))
                .map(this::persisteRejectedItems)
                .then();
    }

    private Flux<RaddRegistryRequestEntity> persisteRejectedItems(List<RaddRegistryRequestEntity> raddRegistryRequestEntities) {
        if(!CollectionUtils.isEmpty(raddRegistryRequestEntities)){
            return Flux.fromIterable(raddRegistryRequestEntities)
                    .flatMap(raddRegistryRequestDAO::createEntity);
        }
        return Flux.empty();
    }

    private Mono<Void> persistItemsAndSendEvent(Map.Entry<String, List<RaddRegistryRequestEntity>> entry) {
        return raddRegistryRequestDAO.writeCsvAddresses(entry.getValue(), entry.getKey())
                .thenReturn(entry.getKey())
                .flatMap(correlationId -> Mono.fromRunnable(() -> correlationIdEventsProducer.sendCorrelationIdEvent(correlationId)));
    }

    private Map<String, List<RaddRegistryRequestEntity>> groupingRaddRegistryRequest(List<RaddRegistryRequestEntity> raddRegistryRequestEntities, int numberOfElements) {
        final AtomicInteger counter = new AtomicInteger();

        Map<String, List<RaddRegistryRequestEntity>> map = raddRegistryRequestEntities.stream()
                .filter(raddRegistryRequestEntity -> !REJECTED.name().equals(raddRegistryRequestEntity.getStatus()))
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / numberOfElements))
                .values()
                .stream()
                .collect(Collectors.toMap(item -> UUID.randomUUID().toString(), Function.identity()));

        log.info("groupingRaddRegistryRequest size: {}", map.size());

        var rejectedRequest = raddRegistryRequestEntities.stream()
                .filter(raddRegistryRequestEntity -> REJECTED.name().equals(raddRegistryRequestEntity.getStatus()))
                .toList();

        if (!CollectionUtils.isEmpty(rejectedRequest)) {
            map.put(REJECTED.name(), rejectedRequest);
        }

        return map;
    }

    private Mono<FileDownloadResponseDto> getFile(String fileKey) {
        return safeStorageClient.getFile(fileKey)
                .onErrorResume(RaddGenericException.class, error -> {
                    log.error("Error during getFile from safeStorage for fileKey [{}]", fileKey);
                    return Mono.error(new RaddImportException(String.format("Filekey [%s] not found", fileKey)));
                });
    }

    private Mono<byte[]> downloadCSV(String url) {
        return documentDownloadClient.downloadContent(url)
                .doOnNext(bytes -> log.debug("Downloaded CSV from: {}", url))
                .doOnError(throwable -> log.warn("Error in download CSV from url: [{}]", url, throwable));
    }
}
