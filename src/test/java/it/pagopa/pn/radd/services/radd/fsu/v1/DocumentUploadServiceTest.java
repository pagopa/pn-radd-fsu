package it.pagopa.pn.radd.services.radd.fsu.v1;
import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.PnException;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileCreationRequestDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.*;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@Slf4j
class DocumentUploadServiceTest extends BaseTest {


    @InjectMocks
    DocumentUploadService documentUploadService;

    @Mock
    PnSafeStorageClient pnSafeStorageClient;

    @Test
    void testWhenIdAndBoundleIdERROR99(){
        String id="idTest";
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        bundleId.setBundleId("idTest");
        bundleId.setContentType("test");
        FileCreationRequestDto request = new FileCreationRequestDto();
        request.setContentType(bundleId.getContentType());
        Mockito.when(pnSafeStorageClient.createFile( bundleId.getContentType(), bundleId.getBundleId())
        ).thenReturn(Mono.error(new PnException("Errore", "99")));
        Mono<DocumentUploadResponse> response = documentUploadService.createFile(id, Mono.just(bundleId) );
        response.onErrorResume( ex ->{
                log.info(ex.getMessage());
                assertNotNull(ex);
                assertNotNull(ex.getMessage());
                assertEquals( ResponseStatus.CodeEnum.NUMBER_99.toString()  , ((PnException) ex).getDescription());
                return Mono.empty();
                }
        ).block();
    }

    @Test
    void testWhenIdAndBoundleId(){
        String id="idTest";
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        bundleId.setBundleId("idTest");
        bundleId.setContentType("test");
        FileCreationRequestDto request = new FileCreationRequestDto();
        request.setContentType(bundleId.getContentType());
        FileCreationResponseDto fileCreationResponseDto = mock(FileCreationResponseDto.class);
        fileCreationResponseDto.setUploadUrl("testUrl");
        Mockito.when(pnSafeStorageClient.createFile(Mockito.any(), Mockito.any())
        ).thenReturn( Mono.just(fileCreationResponseDto) );
        DocumentUploadResponse response = documentUploadService.createFile(id, Mono.just(bundleId) ).block();
        assertEquals(ResponseStatus.CodeEnum.NUMBER_0, response.getStatus().getCode());
    }


    @Test
    void testWhenIdAndBoundleIdIsEmpty(){
        String id="";
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        bundleId.setBundleId("");
        bundleId.setContentType("");
        Mono <DocumentUploadResponse> response = documentUploadService.createFile(id, Mono.just(bundleId) );
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Parametri non validi", exception.getMessage());
            return Mono.empty();
        }).block();

    }




}
