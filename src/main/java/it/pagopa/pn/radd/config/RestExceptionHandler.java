package it.pagopa.pn.radd.config;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.common.rest.error.v1.dto.ProblemError;
import it.pagopa.pn.radd.exception.PnException;
import it.pagopa.pn.radd.exception.PnInvalidInputException;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.PnSafeStorageException;
import it.pagopa.pn.radd.pojo.PnSafeStoreExModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;


@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler {

    public static final String MDC_TRACE_ID_KEY = "trace_id";
    @Autowired
    private ObjectMapper objectMapper;


    @ExceptionHandler(PnException.class)
    public ResponseEntity<Mono<Problem>> pnExceptionHandler(PnException ex){
        Problem rs = new Problem();
        rs.setStatus(ex.getStatus());
        rs.setTitle(ex.getMessage());
        rs.setDetail(ex.getDescription());
        rs.setTimestamp(OffsetDateTime.now());
        settingTraceId(rs);
        log.error(ex.getDescription());
        return ResponseEntity.status(HttpStatus.valueOf(ex.getStatus()))
                .body(Mono.just(rs));
    }

    @ExceptionHandler(PnInvalidInputException.class)
    public ResponseEntity<Mono<Problem>> pnInvalidInputHandler(PnInvalidInputException ex){
        log.error(ex.getReason());
        Problem rs = new Problem();
        rs.setStatus(HttpStatus.BAD_REQUEST.value());
        rs.setTitle(ex.getReason());
        rs.setTimestamp(OffsetDateTime.now());
        settingTraceId(rs);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Mono.just(rs));
    }

    @ExceptionHandler(PnRaddException.class)
    public ResponseEntity<Mono<String>> pnInvalidInputHandler(PnRaddException ex){
        log.error(ex.getWebClientEx().getResponseBodyAsString());
        return ResponseEntity.status(ex.getWebClientEx().getStatusCode())
                .body(Mono.just(ex.getWebClientEx().getResponseBodyAsString()));
    }

    @ExceptionHandler(PnSafeStorageException.class)
    public ResponseEntity<Mono<Problem>> webClientException(PnSafeStorageException ex) {
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
            return ResponseEntity.status(extractStatus(model.getResultCode()))
                    .body(Mono.just(rs));
        } catch (JsonProcessingException e) {
            rs.title("Errore generico");
            rs.detail("Qualcosa è andato storto, ritenta più tardi");
            log.error("exception catched", ex);
            rs.setStatus(500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Mono.just(rs));
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
