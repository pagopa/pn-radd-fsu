package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadRequest;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.RegistryUploadResponse;
import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.RegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.entities.PnRaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.pojo.RaddRegistryImportConfig;
import it.pagopa.pn.radd.pojo.RaddRegistryImportStatus;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DUPLICATE_REQUEST;
import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.PENDING_REQUEST;
import static it.pagopa.pn.radd.pojo.RaddRegistryImportStatus.TO_PROCESS;

@Service
@RequiredArgsConstructor
@CustomLog
public class RegistryService {

    public static final String TEXT_CSV = "text/csv";

    private final RegistryImportDAO registryImportDAO;
    private final PnSafeStorageClient pnSafeStorageClient;
    private final PnRaddFsuConfig pnRaddFsuConfig;
    private final ObjectMapperUtil objectMapper;

    public Mono<RegistryUploadResponse> uploadRegistryRequests(String xPagopaPnCxId, Mono<RegistryUploadRequest> registryUploadRequest) {
        String requestId = UUID.randomUUID().toString();
        return registryUploadRequest.flatMap(request ->
                registryImportDAO.getRegistryImportByCxId(xPagopaPnCxId)
                        .collectList()
                        .flatMap(entities -> checkImportRequest(request, entities))
                        .flatMap(o -> pnSafeStorageClient.createFile(getFileCreationRequestDto(), request.getChecksum()))
                        .flatMap(fileCreationResponseDto -> saveImportRequest(xPagopaPnCxId, request, fileCreationResponseDto, requestId).thenReturn(fileCreationResponseDto))
                        .map(fileCreationResponseDto -> mapUploadResponse(fileCreationResponseDto, requestId))
                        .doOnError(throwable -> log.error("Error uploading registry requests for cxId: {} ->", xPagopaPnCxId, throwable))
        );
    }

    @NotNull
    private FileCreationRequestDto getFileCreationRequestDto() {
        FileCreationRequestDto request = new FileCreationRequestDto();
        request.setStatus(Const.SAVED);
        request.setContentType(TEXT_CSV);
        request.setDocumentType(this.pnRaddFsuConfig.getRegistrySafeStorageDocType());
        return request;
    }

    private Mono<RegistryUploadRequest> checkImportRequest(RegistryUploadRequest request, List<PnRaddRegistryImportEntity> entities) {
        for (PnRaddRegistryImportEntity entity : entities) {
            if (request.getChecksum().equalsIgnoreCase(entity.getChecksum()) &&
                    (RaddRegistryImportStatus.PENDING.name().equalsIgnoreCase(entity.getStatus())
                            || (RaddRegistryImportStatus.TO_PROCESS.name().equalsIgnoreCase(entity.getStatus()) && Instant.now().isBefore(entity.getFileUploadDueDate())))) {
                return Mono.error(new RaddGenericException(ExceptionTypeEnum.valueOf(DUPLICATE_REQUEST.name()), HttpStatus.CONFLICT));
            } else if (RaddRegistryImportStatus.PENDING.name().equalsIgnoreCase(entity.getStatus())
                    || (RaddRegistryImportStatus.TO_PROCESS.name().equalsIgnoreCase(entity.getStatus()) && Instant.now().isBefore(entity.getFileUploadDueDate()))) {
                return Mono.error(new RaddGenericException(ExceptionTypeEnum.valueOf(PENDING_REQUEST.name()), HttpStatus.BAD_REQUEST));
            }
        }
        return Mono.just(request);
    }

    private RegistryUploadResponse mapUploadResponse(FileCreationResponseDto fileCreationResponseDto, String requestId) {
        RegistryUploadResponse registryUploadResponse = new RegistryUploadResponse();
        registryUploadResponse.setRequestId(requestId);
        registryUploadResponse.setFileKey(fileCreationResponseDto.getKey());
        registryUploadResponse.setUrl(fileCreationResponseDto.getUploadUrl());
        registryUploadResponse.setSecret(fileCreationResponseDto.getSecret());
        return registryUploadResponse;
    }

    private Mono<PnRaddRegistryImportEntity> saveImportRequest(String xPagopaPnCxId, RegistryUploadRequest request, FileCreationResponseDto fileCreationResponseDto, String requestId) {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = getPnRaddRegistryImportEntity(xPagopaPnCxId, request, fileCreationResponseDto, requestId);
        return registryImportDAO.putRaddRegistryImportEntity(pnRaddRegistryImportEntity);
    }

    private PnRaddRegistryImportEntity getPnRaddRegistryImportEntity(String xPagopaPnCxId, RegistryUploadRequest request, FileCreationResponseDto fileCreationResponseDto, String requestId) {
        PnRaddRegistryImportEntity pnRaddRegistryImportEntity = new PnRaddRegistryImportEntity();
        pnRaddRegistryImportEntity.setRequestId(requestId);
        pnRaddRegistryImportEntity.setStatus(TO_PROCESS.name());
        pnRaddRegistryImportEntity.setChecksum(request.getChecksum());
        pnRaddRegistryImportEntity.setCxId(xPagopaPnCxId);
        pnRaddRegistryImportEntity.setFileKey(fileCreationResponseDto.getKey());
        pnRaddRegistryImportEntity.setCreatedAt(Instant.now());
        pnRaddRegistryImportEntity.setUpdatedAt(Instant.now());
        pnRaddRegistryImportEntity.setFileUploadDueDate(Instant.now().plus(pnRaddFsuConfig.getRegitryImportUploadFileTtl(), ChronoUnit.SECONDS));

        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDeleteRole(pnRaddFsuConfig.getRegistryDefaultDeleteRule());
        raddRegistryImportConfig.setDefaultEndValidity(pnRaddFsuConfig.getRegistryDefaultEndValidity());
        pnRaddRegistryImportEntity.setConfig(objectMapper.toJson(raddRegistryImportConfig));

        return pnRaddRegistryImportEntity;
    }
}
