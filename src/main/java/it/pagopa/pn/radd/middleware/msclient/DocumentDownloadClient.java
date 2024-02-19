package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.exception.RaddGenericException;
import lombok.CustomLog;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;


@Component
@CustomLog
public class DocumentDownloadClient {

    private final WebClient webClient;

    public DocumentDownloadClient() {
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector())
                .build();
    }


    public Mono<byte[]> downloadContent(String downloadUrl) {
        log.info("start to download file to: {}", downloadUrl);
        try {
            Flux<DataBuffer> dataBufferFlux = WebClient.create()
                    .get()
                    .uri(new URI(downloadUrl))
                    .retrieve()
                    .bodyToFlux(DataBuffer.class)
                    .doOnError(ex -> log.error("Error in WebClient", ex));

            return DataBufferUtils.join(dataBufferFlux)
                    .map(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        return bytes;
                    })
                    .onErrorMap(ex -> {
                        log.error("downloadContent Exception downloading content", ex);
                        return new RaddGenericException(ex.getMessage());
                    });
        } catch (URISyntaxException ex) {
            log.error("error in URI ", ex);
            return Mono.error(new RaddGenericException(ex.getMessage()));
        }
    }
}
