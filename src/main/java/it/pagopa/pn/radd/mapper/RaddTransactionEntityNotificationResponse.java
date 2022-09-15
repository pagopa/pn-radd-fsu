package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.rest.radd.v1.dto.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class RaddTransactionEntityNotificationResponse  implements BaseMapperInterface<NotificationResponse, RaddTransactionEntity> {

    private RaddTransactionEntityNotificationResponse(){
        super();
    }

    @Override
    public RaddTransactionEntity toEntity(NotificationResponse source) {
        return null;
    }

    @Override
    public NotificationResponse toDto(RaddTransactionEntity source) {
        NotificationResponse response = new NotificationResponse();
        response.setIun(source.getIun());
        response.setIdPractice(source.getOperationId());
        return response;
    }
}
