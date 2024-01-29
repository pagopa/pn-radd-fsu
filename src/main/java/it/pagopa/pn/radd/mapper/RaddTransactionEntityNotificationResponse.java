package it.pagopa.pn.radd.mapper;

import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.rest.radd.v1.dto.OperationActDetailResponse;
import it.pagopa.pn.radd.utils.DateUtils;
import org.springframework.stereotype.Component;

@Component
public class RaddTransactionEntityNotificationResponse implements BaseMapperInterface<OperationActDetailResponse, RaddTransactionEntity> {

    private RaddTransactionEntityNotificationResponse(){
        super();
    }

    @Override
    public RaddTransactionEntity toEntity(OperationActDetailResponse source) {
        return null;
    }

    @Override
    public OperationActDetailResponse toDto(RaddTransactionEntity source) {
        OperationActDetailResponse dto = new OperationActDetailResponse();
        dto.setIun(source.getIun());
        dto.setOperationId(source.getOperationId());
        dto.setRecipientTaxId(source.getRecipientId());
        dto.setRecipientType(source.getRecipientType());
        dto.setDelegateTaxId(source.getDelegateId());
        dto.setUid(source.getUid());
        dto.setFileKey(source.getFileKey());
        dto.setOperationEndDate(DateUtils.parseDateString(source.getOperationEndDate()));
        dto.setOperationStartDate(DateUtils.parseDateString(source.getOperationStartDate()));
        dto.setOperationStatus(source.getStatus());
        dto.setErrorReason(source.getErrorReason());
        dto.setQrCode(source.getQrCode());
        dto.setOperationType(source.getOperationType());
        return dto;
    }
}
