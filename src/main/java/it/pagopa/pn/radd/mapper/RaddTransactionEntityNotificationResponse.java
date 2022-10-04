package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationDetailResponse;
import it.pagopa.pn.radd.utils.DateUtils;
import org.springframework.stereotype.Component;

@Component
public class RaddTransactionEntityNotificationResponse implements BaseMapperInterface<OperationDetailResponse, RaddTransactionEntity> {

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
        dto.setRecipientTaxId(source.getRecipientId());
        dto.setDelegateTaxId(source.getDelegateId());
        dto.setFileKey(source.getFileKey());
        dto.setUid(source.getUid());
        dto.setOperationEndDate(DateUtils.parseDateString(source.getOperationEndDate()));
        dto.setOperationStartDate(DateUtils.parseDateString(source.getOperationStartDate()));
        dto.setOperationStatus(source.getStatus());
        dto.setErrorReason(source.getErrorReason());
        dto.setQrCode(source.getQrCode());
        dto.setOperationType(source.getOperationType());
        return dto;
    }
}
