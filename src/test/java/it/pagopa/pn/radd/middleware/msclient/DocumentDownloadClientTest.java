package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.exception.RaddGenericException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.mockito.Mockito.*;

@ContextConfiguration (classes = {DocumentDownloadClient.class})
@ExtendWith (SpringExtension.class)
class DocumentDownloadClientTest {
	@Autowired
	private DocumentDownloadClient documentDownloadClient;
	
	@Test
	void testDownloadContentWithError () {
		DocumentDownloadClient uploadDownloadClient = new DocumentDownloadClient();
		WebClientResponseException webClientResponseException = mock(WebClientResponseException.class);
		when(webClientResponseException.getMessage()).thenReturn("Error message");
		when(webClientResponseException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
		Mono<byte[]> resultMono = uploadDownloadClient.downloadContent("http://localhost:808098");
		StepVerifier.create(resultMono)
				.expectError(RaddGenericException.class)
				.verify();
	}
}

