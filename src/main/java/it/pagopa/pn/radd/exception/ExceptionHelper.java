package it.pagopa.pn.radd.exception;

import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.radd.exception.PnException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

@Slf4j
public class ExceptionHelper {

    public static final String MDC_TRACE_ID_KEY = "trace_id";

    private ExceptionHelper(){}

    public static HttpStatus getHttpStatusFromException(Throwable ex){
        if (ex instanceof PnException)
        {
            return HttpStatus.resolve(((PnException) ex).getStatus());
        }
        else
            return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public static Problem handleException(Throwable ex, HttpStatus statusError){
        // gestione exception e generazione fault
        Problem res = new Problem();
        res.setStatus(statusError.value());
        try {
            res.setTraceId(MDC.get(MDC_TRACE_ID_KEY));
        } catch (Exception e) {
            log.warn("Cannot get traceid", e);
        }

        if (ex instanceof PnException)
        {
            res.setTitle(ex.getMessage());
            res.setDetail(((PnException)ex).getDescription());
            res.setStatus(((PnException) ex).getStatus());
            log.warn("pn-exception catched", ex);
        }
        else
        {
            // nascondo all'utente l'errore
            res.title("Errore generico");
            res.detail("Qualcosa è andato storto, ritenta più tardi");
            log.error("exception catched", ex);
        }

        return res;
    }
}
