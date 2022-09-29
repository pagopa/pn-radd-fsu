package it.pagopa.pn.radd.services.radd.fsu.v1;
import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.rest.radd.v1.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Slf4j
class DocumentUploadServiceTest extends BaseTest {


    @InjectMocks
    DocumentUploadService documentUploadService;


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
