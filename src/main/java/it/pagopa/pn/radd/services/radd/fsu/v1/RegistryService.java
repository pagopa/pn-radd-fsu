package it.pagopa.pn.radd.services.radd.fsu.v1;

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
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DUPLICATE_REQUEST;
import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.PENDING_REQUEST;
import static it.pagopa.pn.radd.pojo.RaddRegistryImportStatus.PENDING;
import static it.pagopa.pn.radd.pojo.RaddRegistryImportStatus.TO_PROCESS;

@Service
@RequiredArgsConstructor
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
                        .map(entities -> checkImportRequest(request, entities))
                        .flatMap(o -> pnSafeStorageClient.createFile(TEXT_CSV, request.getChecksum()))
                        .flatMap(fileCreationResponseDto -> saveImportRequest(xPagopaPnCxId, request, fileCreationResponseDto, requestId).thenReturn(fileCreationResponseDto))
                        .map(fileCreationResponseDto -> mapUploadResponse(fileCreationResponseDto, requestId))
        );
    }

    private RegistryUploadRequest checkImportRequest(RegistryUploadRequest request, List<PnRaddRegistryImportEntity> entities) {
        for(PnRaddRegistryImportEntity entity : entities) {
            if (request.getChecksum().equals(entity.getChecksum()))
                throw new RaddGenericException(ExceptionTypeEnum.valueOf(DUPLICATE_REQUEST.name()), HttpStatus.CONFLICT);
            else if (PENDING.name().equals(entity.getStatus()))
                throw new RaddGenericException(ExceptionTypeEnum.valueOf(PENDING_REQUEST.name()), HttpStatus.BAD_REQUEST);
        }
        return request;
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

        RaddRegistryImportConfig raddRegistryImportConfig = new RaddRegistryImportConfig();
        raddRegistryImportConfig.setDeleteRole(pnRaddFsuConfig.getDeleteRole());
        raddRegistryImportConfig.setDefaultEndValidity(pnRaddFsuConfig.getDefaultEndValidity());
        pnRaddRegistryImportEntity.setConfig(objectMapper.toJson(raddRegistryImportConfig));

        return pnRaddRegistryImportEntity;
    }
}
