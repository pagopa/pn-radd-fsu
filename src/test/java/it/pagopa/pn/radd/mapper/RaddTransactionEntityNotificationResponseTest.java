package it.pagopa.pn.radd.mapper;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.OperationActDetailResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {RaddTransactionEntityNotificationResponse.class})
@ExtendWith(SpringExtension.class)
class RaddTransactionEntityNotificationResponseTest {
    @Autowired
    private RaddTransactionEntityNotificationResponse raddTransactionEntityNotificationResponse;

    /**
     * Method under test:
     * {@link RaddTransactionEntityNotificationResponse#toEntity(OperationActDetailResponse)}
     */
    @Test
    void testToEntity() {
        // Arrange, Act and Assert
        assertNull(raddTransactionEntityNotificationResponse.toEntity(new OperationActDetailResponse()));
        assertNull(raddTransactionEntityNotificationResponse.toEntity(mock(OperationActDetailResponse.class)));
    }
}
