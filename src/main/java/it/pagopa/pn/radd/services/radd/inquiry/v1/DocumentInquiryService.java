package it.pagopa.pn.radd.services.radd.inquiry.v1;

import it.pagopa.pn.radd.middleware.db.DocumentInquiryDao;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponse;
import it.pagopa.pn.radd.rest.radd.v1.dto.ActInquiryResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DocumentInquiryService {

    private final DocumentInquiryDao documentInquiryDao;

    public DocumentInquiryService(DocumentInquiryDao documentInquiryDao) {
        this.documentInquiryDao = documentInquiryDao;
    }

    public Mono<ActInquiryResponse> actInquiry() {
        ActInquiryResponse response = new ActInquiryResponse();
        response.setResult(true);
        Mono<ActInquiryResponse> result = Mono.just(response);
        return result;
    }

}
