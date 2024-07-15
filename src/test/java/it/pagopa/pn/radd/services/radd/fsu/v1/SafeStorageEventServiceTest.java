package it.pagopa.pn.radd.services.radd.fsu.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileDownloadInfoDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.pnsafestorage.v1.dto.FileDownloadResponseDto;
import it.pagopa.pn.radd.mapper.RaddRegistryRequestEntityMapper;
import it.pagopa.pn.radd.middleware.db.RaddRegistryImportDAO;
import it.pagopa.pn.radd.middleware.db.RaddRegistryRequestDAO;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryImportEntity;
import it.pagopa.pn.radd.middleware.msclient.DocumentDownloadClient;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.middleware.queue.producer.CorrelationIdEventsProducer;
import it.pagopa.pn.radd.pojo.RaddRegistryImportStatus;
import it.pagopa.pn.radd.utils.ObjectMapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SafeStorageEventServiceTest {

    @Mock
    private RaddRegistryImportDAO pnRaddRegistryImportDAO;

    @Mock
    private RaddRegistryRequestDAO raddRegistryRequestDAO;

    @Mock
    private PnSafeStorageClient safeStorageClient;

    @Mock
    private DocumentDownloadClient documentDownloadClient;

    @Mock
    private CorrelationIdEventsProducer correlationIdEventsProducer;

    private SafeStorageEventService safeStorageEventService;


    @BeforeEach
    public void setup() {
        RaddRegistryRequestEntityMapper raddRegistryRequestEntityMapper = new RaddRegistryRequestEntityMapper(new ObjectMapperUtil(new ObjectMapper()));
        CsvService csvService = new CsvService();
        safeStorageEventService = new SafeStorageEventService(pnRaddRegistryImportDAO, raddRegistryRequestDAO,
                safeStorageClient, documentDownloadClient, csvService, raddRegistryRequestEntityMapper, correlationIdEventsProducer);
    }

    @Test
    void shouldHandleSafeStorageResponseSuccessfully() throws IOException {
        File file = new File("src/test/resources", "radd-registry.csv");
        InputStream inputStream = new FileInputStream(file);
        FileDownloadResponseDto response = new FileDownloadResponseDto();
        response.setKey("testKey");
        FileDownloadInfoDto fileDownloadInfoDto = new FileDownloadInfoDto();
        fileDownloadInfoDto.setUrl("testUrl");
        response.setDownload(fileDownloadInfoDto);
        RaddRegistryImportEntity raddRegistryImportEntity = new RaddRegistryImportEntity();
        raddRegistryImportEntity.setFileKey("testKey");
        raddRegistryImportEntity.setRequestId("RequestId");
        raddRegistryImportEntity.setCxId("CxId");
        raddRegistryImportEntity.setStatus("TO_PROCESS");
        when(pnRaddRegistryImportDAO.getItemByFileKey(any())).thenReturn(Mono.just(raddRegistryImportEntity));
        when(pnRaddRegistryImportDAO.updateStatus(any(), any(), any())).thenReturn(Mono.just(raddRegistryImportEntity));
        when(raddRegistryRequestDAO.persistCsvAddresses(any(), any())).thenReturn(Mono.empty());
        when(raddRegistryRequestDAO.createEntity(any())).thenReturn(Mono.empty());
        when(safeStorageClient.getFile("testKey")).thenReturn(Mono.just(response));
        when(documentDownloadClient.downloadContent("testUrl")).thenReturn(Mono.just(inputStream.readAllBytes()));
        Mono<Void> result = safeStorageEventService.handleSafeStorageResponse(response);

        StepVerifier.create(result)
                .verifyComplete();

        verify(pnRaddRegistryImportDAO, times(1)).updateStatus(raddRegistryImportEntity, RaddRegistryImportStatus.PENDING, null);

    }

    @Test
    public void shouldHandleSafeStorageResponseImportRequestNotFound() {
        FileDownloadResponseDto response = new FileDownloadResponseDto();
        response.setKey("testKey");
        when(pnRaddRegistryImportDAO.getItemByFileKey(any())).thenReturn(Mono.empty());
        Mono<Void> result = safeStorageEventService.handleSafeStorageResponse(response);

        StepVerifier.create(result).verifyComplete();
    }


    @Test
    public void shouldHandleSafeStorageResponseWithMalformedCSV() throws IOException {
        File file = new File("src/test/resources", "radd-registry-error.csv");
        InputStream inputStream = new FileInputStream(file);
        FileDownloadResponseDto response = new FileDownloadResponseDto();
        response.setKey("testKey");
        FileDownloadInfoDto fileDownloadInfoDto = new FileDownloadInfoDto();
        fileDownloadInfoDto.setUrl("testUrl");
        response.setDownload(fileDownloadInfoDto);

        when(safeStorageClient.getFile("testKey")).thenReturn(Mono.just(response));
        when(documentDownloadClient.downloadContent("testUrl")).thenReturn(Mono.just(inputStream.readAllBytes()));

        RaddRegistryImportEntity raddRegistryImportEntity = new RaddRegistryImportEntity();
        raddRegistryImportEntity.setFileKey("testKey");
        raddRegistryImportEntity.setRequestId("RequestId");
        raddRegistryImportEntity.setCxId("CxId");
        raddRegistryImportEntity.setStatus("TO_PROCESS");

        when(pnRaddRegistryImportDAO.getItemByFileKey(any())).thenReturn(Mono.just(raddRegistryImportEntity));


        when(pnRaddRegistryImportDAO.updateStatus(raddRegistryImportEntity, RaddRegistryImportStatus.REJECTED, "Malformed CSV")).thenReturn(Mono.just(raddRegistryImportEntity));

        Mono<Void> result = safeStorageEventService.handleSafeStorageResponse(response);

        StepVerifier.create(result).verifyComplete();
        verify(pnRaddRegistryImportDAO, times(1)).updateStatus(raddRegistryImportEntity, RaddRegistryImportStatus.REJECTED, "Malformed CSV");
    }

    @Test
    public void shouldHandleSafeStorageResponseErrorAndRollback() throws IOException {
        File file = new File("src/test/resources", "radd-registry.csv");
        InputStream inputStream = new FileInputStream(file);
        FileDownloadResponseDto response = new FileDownloadResponseDto();
        response.setKey("testKey");
        FileDownloadInfoDto fileDownloadInfoDto = new FileDownloadInfoDto();
        fileDownloadInfoDto.setUrl("testUrl");
        response.setDownload(fileDownloadInfoDto);

        when(safeStorageClient.getFile("testKey")).thenReturn(Mono.just(response));
        when(documentDownloadClient.downloadContent("testUrl")).thenReturn(Mono.just(inputStream.readAllBytes()));

        RaddRegistryImportEntity raddRegistryImportEntity = new RaddRegistryImportEntity();
        raddRegistryImportEntity.setFileKey("testKey");
        raddRegistryImportEntity.setRequestId("RequestId");
        raddRegistryImportEntity.setCxId("CxId");
        raddRegistryImportEntity.setStatus("TO_PROCESS");

        when(pnRaddRegistryImportDAO.getItemByFileKey(any())).thenReturn(Mono.just(raddRegistryImportEntity));
        when(raddRegistryRequestDAO.persistCsvAddresses(any(), any())).thenReturn(Mono.empty());
        when(raddRegistryRequestDAO.createEntity(any())).thenReturn(Mono.empty());

        when(pnRaddRegistryImportDAO.updateStatus(raddRegistryImportEntity, RaddRegistryImportStatus.PENDING, null)).thenThrow(new RuntimeException());

        Mono<Void> result = safeStorageEventService.handleSafeStorageResponse(response);

        StepVerifier.create(result).verifyError(RuntimeException.class);
        verify(pnRaddRegistryImportDAO, times(1)).updateStatus(any(), eq(RaddRegistryImportStatus.PENDING), any());
    }
}
