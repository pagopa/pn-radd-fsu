package it.pagopa.pn.radd.pojo;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import lombok.Data;

@Data
public class NotificationPayment {
    public NotificationPayment(PaymentType paymentType, NotificationAttachmentDownloadMetadataResponseDto notificationAttachmentDownloadMetadataResponseDto) {
        this.tipo = paymentType;
        this.notificationMetadata = notificationAttachmentDownloadMetadataResponseDto;
    }

    public enum PaymentType {
        PAGOPA,
        F24,
        DOCUMENT
    }

    private PaymentType tipo;

    private NotificationAttachmentDownloadMetadataResponseDto notificationMetadata;

    public PaymentType getTipo() {
        return tipo;
    }

    public void setTipo(PaymentType tipo) {
        this.tipo = tipo;
    }
}
