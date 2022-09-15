package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationDetailResponse;
import org.springframework.stereotype.Component;

@Component
public class RaddTransactionEntityNotificationResponse  implements BaseMapperInterface<OperationDetailResponse, RaddTransactionEntity> {

    private RaddTransactionEntityNotificationResponse(){
        super();
    }

    @Override
    public RaddTransactionEntity toEntity(OperationDetailResponse source) {
        return null;
    }

    @Override
    public OperationDetailResponse toDto(RaddTransactionEntity source) {
        OperationDetailResponse dto = new OperationDetailResponse();
        dto.setIun(source.getIun());
        dto.setOperationId(source.getOperationId());
        return dto;
    }
}
