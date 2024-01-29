package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.PnException;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileCreationRequestDto;
import it.pagopa.pn.radd.microservice.msclient.generated.pnsafestorage.v1.dto.FileCreationResponseDto;
import it.pagopa.pn.radd.middleware.msclient.PnSafeStorageClient;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadRequest;
import it.pagopa.pn.radd.rest.radd.v1.dto.DocumentUploadResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.ResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DOCUMENT_UPLOAD_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

// TODO: Test disabilitati da riparare in fase di aggiornamento rispettiva API


@Slf4j
class DocumentUploadServiceTest extends BaseTest {


    @InjectMocks
    DocumentUploadService documentUploadService;

    @Mock
    PnSafeStorageClient pnSafeStorageClient;

    @Test
    @Disabled
    void testWhenIdAndBoundleIdERROR99(){
        String id="idTest";
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;

        FileCreationRequestDto request = new FileCreationRequestDto();
        request.setContentType("zip");
        Mockito.when(pnSafeStorageClient.createFile( any(), anyString())
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
    void testWhenIdAndBoundleKO(){
        String id="idTest";
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        Mockito.when(pnSafeStorageClient.createFile( any(), any())
        ).thenReturn(Mono.error( new RaddGenericException(DOCUMENT_UPLOAD_ERROR)));
        Mono<DocumentUploadResponse> response = documentUploadService.createFile(id, Mono.just(bundleId) );
        response.onErrorResume(ex ->{
            if (ex instanceof RaddGenericException){
                log.info(((RaddGenericException) ex).getExceptionType().getMessage());
                assertNotNull(ex);
                assertNotNull(((RaddGenericException) ex).getExceptionType());
                assertEquals( DOCUMENT_UPLOAD_ERROR  , ((RaddGenericException) ex).getExceptionType());
                return Mono.empty();
            }
            fail("Other exception");
            return null;
        }).block();
    }


    @Test
    void testWhenIdAndBoundleId(){
        String id="idTest";
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        FileCreationResponseDto fileCreationResponseDto = mock(FileCreationResponseDto.class);
        fileCreationResponseDto.setUploadUrl("testUrl");
        Mockito.when(pnSafeStorageClient.createFile(Mockito.any(), Mockito.any())
        ).thenReturn( Mono.just(fileCreationResponseDto) );
        DocumentUploadResponse response = documentUploadService.createFile(id, Mono.just(bundleId) ).block();
        assertNotNull(response);
        assertEquals(ResponseStatus.CodeEnum.NUMBER_0, response.getStatus().getCode());
    }


    @Test
    @Disabled
    void testWhenIdAndBoundleIdIsEmpty(){
        String id="";
        DocumentUploadRequest bundleId = new DocumentUploadRequest() ;
        Mono <DocumentUploadResponse> response = documentUploadService.createFile(id, Mono.just(bundleId) );
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni valori non sono valorizzati", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    @Disabled
    void testWhenContentTypeIsNull(){
        DocumentUploadRequest documentUploadRequest=new DocumentUploadRequest();
        Mono <DocumentUploadResponse> response = documentUploadService.createFile("test", Mono.just(documentUploadRequest));
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni valori non sono valorizzati", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    @Disabled
    void testWhenBundleIdIsNull(){
        DocumentUploadRequest documentUploadRequest=new DocumentUploadRequest();
        Mono <DocumentUploadResponse> response = documentUploadService.createFile("test", Mono.just(documentUploadRequest));
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni valori non sono valorizzati", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    @Disabled
    void testWhenContentTypeIsEmpty(){
        DocumentUploadRequest documentUploadRequest=new DocumentUploadRequest();
        Mono <DocumentUploadResponse> response = documentUploadService.createFile("test", Mono.just(documentUploadRequest));
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni valori non sono valorizzati", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    @Disabled
    void testWhenBundleIdIsEmpty(){
        DocumentUploadRequest documentUploadRequest=new DocumentUploadRequest();
        Mono <DocumentUploadResponse> response = documentUploadService.createFile("test", Mono.just(documentUploadRequest));
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Alcuni valori non sono valorizzati", exception.getMessage());
            return Mono.empty();
        }).block();

    }

    @Test
    void testWhenRequestIsNull(){

        Mono <DocumentUploadResponse> response = documentUploadService.createFile("test", null);
        response.onErrorResume( PnInvalidInputException.class, exception ->{
            assertEquals("Body non valido", exception.getMessage());
            return Mono.empty();
        }).block();

    }

}
