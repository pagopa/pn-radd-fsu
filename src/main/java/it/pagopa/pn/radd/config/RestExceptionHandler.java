package it.pagopa.pn.radd.config;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.common.rest.error.v1.dto.ProblemError;
import it.pagopa.pn.radd.exception.*;
import it.pagopa.pn.radd.pojo.PnSafeStoreExModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.stream.Collectors;


@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler {

    public static final String MDC_TRACE_ID_KEY = "trace_id";
    @Autowired
    private ObjectMapper objectMapper;


    @ExceptionHandler(PnException.class)
    public Mono<ResponseEntity<Problem>> pnExceptionHandler(PnException ex){
        Problem rs = new Problem();
        rs.setStatus(ex.getStatus());
        rs.setTitle(ex.getMessage());
        rs.setDetail(ex.getDescription());
        rs.setTimestamp(OffsetDateTime.now());
        settingTraceId(rs);
        log.error(ex.getDescription());
        return Mono.just(ResponseEntity.status(HttpStatus.valueOf(ex.getStatus()))
                .body(rs));
    }

    @ExceptionHandler(PnRaddForbiddenException.class)
    public Mono<ResponseEntity<Void>> pnRaddForbiddenException(PnRaddForbiddenException ex){
        return Mono.just(ResponseEntity.status(ex.getStatus())
                .build());
    }

    @ExceptionHandler(PnInvalidInputException.class)
    public Mono<ResponseEntity<Problem>> pnInvalidInputHandler(PnInvalidInputException ex){
        log.error(ex.getReason());
        Problem rs = new Problem();
        rs.setStatus(HttpStatus.BAD_REQUEST.value());
        rs.setTitle(ex.getReason());
        rs.setTimestamp(OffsetDateTime.now());
        settingTraceId(rs);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(rs));
    }

    @ExceptionHandler(PnRaddException.class)
    public Mono<ResponseEntity<String>> pnInvalidInputHandler(PnRaddException ex){
        log.error(ex.getWebClientEx().getResponseBodyAsString());
        return Mono.just(ResponseEntity.status(ex.getWebClientEx().getStatusCode())
                .body((ex.getWebClientEx().getResponseBodyAsString())));
    }

    @ExceptionHandler(RaddGenericException.class)
    public Mono<ResponseEntity<Problem>> pnRaddGenericException(RaddGenericException ex){
        log.error(ex.getMessage());
        Problem problem = new Problem();
        problem.setType(ex.getStatus().getReasonPhrase());
        problem.setStatus(ex.getStatus().value());
        problem.setTitle(ex.getExceptionType().getTitle());
        problem.setDetail(ex.getExceptionType().getMessage());
        problem.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        problem.setTraceId(MDC.get(MDC_TRACE_ID_KEY));
        return Mono.just(ResponseEntity.status(ex.getStatus())
                .body(problem));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<Problem>> constraintViolationException(ConstraintViolationException ex){
        log.error(ex.getMessage());
        Problem problem = new Problem();
        problem.setType(ex.getMessage());
        problem.setStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle(ex.getMessage());
        if(CollectionUtils.isEmpty(ex.getConstraintViolations())) {
            problem.setDetail(ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining()));
        }
        problem.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        problem.setTraceId(MDC.get(MDC_TRACE_ID_KEY));
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(problem));
    }

    @ExceptionHandler(RaddImportException.class)
    public Mono<ResponseEntity<Problem>> pnRaddImportException(RaddImportException ex){
        log.error(ex.getMessage());
        Problem problem = new Problem();
        problem.setType(ex.getStatus().getReasonPhrase());
        problem.setStatus(ex.getStatus().value());
        problem.setTitle(ex.getExceptionType().getTitle());
        problem.setDetail(ex.getExceptionType().getMessage());
        problem.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        problem.setTraceId(MDC.get(MDC_TRACE_ID_KEY));
        return Mono.just(ResponseEntity.status(ex.getStatus())
                .body(problem));
    }

    @ExceptionHandler(PnSafeStorageException.class)
    public Mono<ResponseEntity<Problem>> webClientException(PnSafeStorageException ex) {
        Problem rs = new Problem();
        try {
            PnSafeStoreExModel model = this.objectMapper.readValue(ex.getWebClientEx().getResponseBodyAsString(), PnSafeStoreExModel.class);
            rs.title(model.getResultDescription());
            if (model.getErrorList() != null){
                rs.setErrors(new ArrayList<>());
                model.getErrorList().forEach(item -> {
                    ProblemError error = new ProblemError();
                    error.setDetail(item);
                    rs.getErrors().add(error);
                });
            }
            settingTraceId(rs);
            rs.setTimestamp(OffsetDateTime.now());
            return Mono.just(ResponseEntity.status(extractStatus(model.getResultCode()))
                    .body(rs));
        } catch (JsonProcessingException e) {
            rs.title("Errore generico");
            rs.detail("Qualcosa è andato storto, ritenta più tardi");
            log.error("exception catched", ex);
            rs.setStatus(500);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(rs));
        }

    }

    private HttpStatus extractStatus(String value){
        if (value != null && !Strings.isBlank(value)){
            String maybeNumber = value.substring(0, 3);
            try {
                return HttpStatus.valueOf(Integer.parseInt(maybeNumber));
            }catch (NumberFormatException ex){
                log.debug("Not number");
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private void settingTraceId(Problem problem){
        try {
            problem.setTraceId(MDC.get(MDC_TRACE_ID_KEY));
        } catch (Exception e) {
            log.warn("Cannot get traceid", e);
        }
    }

}
