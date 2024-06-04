package it.pagopa.pn.radd.exception;



import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class PnRaddException extends Exception {

    private final WebClientResponseException webClientEx;

    public PnRaddException(WebClientResponseException webClientEx){
        super(StringUtils.hasText(webClientEx.getMessage()) ? webClientEx.getMessage() : "Web Client Generic Error");
        this.webClientEx = webClientEx;
    }

    public WebClientResponseException getWebClientEx() {
        return webClientEx;
    }

}