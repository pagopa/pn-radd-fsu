package it.pagopa.pn.radd.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.pojo.PnSafeStoreExModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestExceptionHandlerTests {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RestExceptionHandler restExceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handlesPnExceptionCorrectly() {
        PnException pnException = new PnException("Test Exception", "Detailed message", HttpStatus.BAD_REQUEST.value());

        Mono<ResponseEntity<Problem>> result = restExceptionHandler.pnExceptionHandler(pnException);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST &&
                        response.getBody().getTitle().equals("Test Exception") &&
                        response.getBody().getDetail().equals("Detailed message"))
                .verifyComplete();
    }

    @Test
    void handlesPnRaddForbiddenExceptionCorrectly() {
        PnRaddForbiddenException exception = new PnRaddForbiddenException("forbidden message", HttpStatus.FORBIDDEN.value());

        Mono<ResponseEntity<Void>> result = restExceptionHandler.pnRaddForbiddenException(exception);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.FORBIDDEN)
                .verifyComplete();
    }

    @Test
    void handlesConstraintViolationExceptionWithNoViolations() {
        ConstraintViolationException exception = mock(ConstraintViolationException.class);
        ConstraintViolation mockViolation = mock(ConstraintViolation.class);
        Set mockSet = Set.of(mockViolation);
        when(mockViolation.getMessage()).thenReturn("Mock violation message");
        when(exception.getConstraintViolations()).thenReturn(mockSet);

        Mono<ResponseEntity<Problem>> result = restExceptionHandler.constraintViolationException(exception);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();
    }

    @Test
    void handlesWebClientExceptionWithInvalidJson() throws JsonProcessingException {
        PnSafeStorageException exception = new PnSafeStorageException(new WebClientResponseException("Bad Request", 400, "Bad Request", null, "Invalid JSON".getBytes(), null));
        when(objectMapper.readValue("Invalid JSON", PnSafeStoreExModel.class)).thenThrow(new JsonProcessingException("Error parsing JSON") {});

        Mono<ResponseEntity<Problem>> result = restExceptionHandler.webClientException(exception);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR &&
                        response.getBody().getTitle().equals("Errore generico"))
                .verifyComplete();
    }

    @Test
    void handlesWebClientExceptionWithValidJsonCorrectly() throws JsonProcessingException {
        String validJson = "{\"resultCode\":\"200 OK\",\"resultDescription\":\"Success\",\"errorList\":[\"Error1\",\"Error2\"]}";
        PnSafeStorageException exception = new PnSafeStorageException(new WebClientResponseException("OK", 200, "OK", null, validJson.getBytes(), null));
        PnSafeStoreExModel exModel = new PnSafeStoreExModel();
        exModel.setResultCode("200 OK");
        exModel.setResultDescription("Success");
        exModel.setErrorList(List.of("Error1", "Error2"));
        when(objectMapper.readValue(validJson, PnSafeStoreExModel.class)).thenReturn(exModel);

        Mono<ResponseEntity<Problem>> result = restExceptionHandler.webClientException(exception);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK &&
                        response.getBody().getTitle().equals("Success") &&
                        response.getBody().getErrors().size() == 2 &&
                        response.getBody().getErrors().get(0).getDetail().equals("Error1") &&
                        response.getBody().getErrors().get(1).getDetail().equals("Error2"))
                .verifyComplete();
    }

    @Test
    void handlesPnRaddBadRequestExceptionCorrectly() {
        PnRaddBadRequestException exception = new PnRaddBadRequestException("Bad request error message");

        Mono<ResponseEntity<Problem>> result = restExceptionHandler.pnRaddBadRequestException(exception);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST &&
                        response.getBody().getTitle().equals("Bad request error message") &&
                        response.getBody().getDetail().equals(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .verifyComplete();
    }

    @Test
    void handlesPnInvalidInputExceptionCorrectly() {
        PnInvalidInputException exception = new PnInvalidInputException("Invalid input error message");

        Mono<ResponseEntity<Problem>> result = restExceptionHandler.pnInvalidInputHandler(exception);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST &&
                        response.getBody().getTitle().equals("Invalid input error message") &&
                        response.getBody().getDetail() == null) // Assuming detail is not set by pnInvalidInputHandler
                .verifyComplete();
    }

    @Test
    void handlesPnRaddExceptionCorrectly() {
        PnRaddException exception = new PnRaddException(new WebClientResponseException("Error", 500, "Internal Server Error", null, "Error message".getBytes(), null));

        Mono<ResponseEntity<String>> result = restExceptionHandler.pnInvalidInputHandler(exception);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR &&
                        response.getBody().equals("Error message"))
                .verifyComplete();
    }

    @Test
    void handlesPnRaddGenericExceptionCorrectly() {
        RaddGenericException exception = new RaddGenericException(ExceptionTypeEnum.GENERIC_ERROR);

        Mono<ResponseEntity<Problem>> result = restExceptionHandler.pnRaddGenericException(exception);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST &&
                        response.getBody().getTitle().equals(ExceptionTypeEnum.GENERIC_ERROR.getTitle()))
                .verifyComplete();
    }

    @Test
    void testPnRaddImportException() {
        // Create a RaddImportException instance
        RaddImportException exception = new RaddImportException(ExceptionTypeEnum.GENERIC_ERROR, HttpStatus.BAD_REQUEST);

        // Call the method and get the result
        Mono<ResponseEntity<Problem>> result = restExceptionHandler.pnRaddImportException(exception);

        // Assert the response
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals(ExceptionTypeEnum.GENERIC_ERROR.getTitle(), response.getBody().getTitle());
                    assertEquals(ExceptionTypeEnum.GENERIC_ERROR.getMessage(), response.getBody().getDetail());
                })
                .verifyComplete();
    }

    @Test
    void handlesRaddImportExceptionWithGenericErrorCorrectly() {
        RaddImportException exception = new RaddImportException(ExceptionTypeEnum.GENERIC_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

        Mono<ResponseEntity<Problem>> result = restExceptionHandler.pnRaddImportException(exception);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR &&
                        response.getBody().getTitle().equals(ExceptionTypeEnum.GENERIC_ERROR.getTitle()) &&
                        response.getBody().getDetail().equals(ExceptionTypeEnum.GENERIC_ERROR.getMessage()))
                .verifyComplete();
    }

    @Test
    void handlesWebExchangeBindExceptionCorrectly() {
        WebExchangeBindException exception = mock(WebExchangeBindException.class);
        List<ObjectError> objectErrors = new ArrayList<>();
        objectErrors.add(new FieldError("objectName", "field", "defaultMessage"));
        objectErrors.add(new ObjectError("objectName", "defaultMessage"));

        when(exception.getAllErrors()).thenReturn(objectErrors);
        when(exception.getMessage()).thenReturn("Validation failed");

        Mono<ResponseEntity<Problem>> result = restExceptionHandler.pnRaddImportException(exception);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST &&
                        response.getBody().getTitle().contains("field defaultMessage"))
                .verifyComplete();
    }
}