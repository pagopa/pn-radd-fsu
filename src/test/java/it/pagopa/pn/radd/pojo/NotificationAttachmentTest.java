package it.pagopa.pn.radd.pojo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import org.junit.jupiter.api.Test;

class NotificationAttachmentTest {
    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link NotificationAttachment#NotificationAttachment(NotificationAttachment.AttachmentType, NotificationAttachmentDownloadMetadataResponseDto)}
     *   <li>{@link NotificationAttachment#setType(NotificationAttachment.AttachmentType)}
     *   <li>{@link NotificationAttachment#getType()}
     * </ul>
     */
    @Test
    void testConstructor() {
        NotificationAttachment actualNotificationAttachment = new NotificationAttachment(
                NotificationAttachment.AttachmentType.PAGOPA, new NotificationAttachmentDownloadMetadataResponseDto());
        actualNotificationAttachment.setType(NotificationAttachment.AttachmentType.PAGOPA);
        assertEquals(NotificationAttachment.AttachmentType.PAGOPA, actualNotificationAttachment.getType());
    }
}

