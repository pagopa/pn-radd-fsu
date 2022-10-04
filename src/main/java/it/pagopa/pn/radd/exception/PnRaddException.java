package it.pagopa.pn.radd.exception;


import org.springframework.web.reactive.function.client.WebClientResponseException;

public class PnRaddException extends Exception {

    private final WebClientResponseException webClientEx;

    public PnRaddException(WebClientResponseException webClientEx){
        this.webClientEx = webClientEx;
    }

    public WebClientResponseException getWebClientEx() {
        return webClientEx;
    }

}