package it.pagopa.pn.radd.utils.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import it.pagopa.pn.radd.alt.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.ActInquiryResponseStatus;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.ResponseStatus;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.TransactionResponseStatus;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PnRaddAltLogContextDiffblueTest {
    /**
     * Method under test: {@link PnRaddAltLogContext#addUid(String)}
     */
    @Test
    void testAddUid() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddUidResult = pnRaddAltLogContext.addUid("1234");

        // Assert
        assertEquals("uid=1234 ", actualAddUidResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddUidResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#addCxId(String)}
     */
    @Test
    void testAddCxId() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddCxIdResult = pnRaddAltLogContext.addCxId("42");

        // Assert
        assertEquals("cxId=42 ", actualAddCxIdResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddCxIdResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#addCxType(String)}
     */
    @Test
    void testAddCxType() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddCxTypeResult = pnRaddAltLogContext.addCxType("Cx Type");

        // Assert
        assertEquals("cxType=Cx Type ", actualAddCxTypeResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddCxTypeResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#addRecipientInternalId(String)}
     */
    @Test
    void testAddRecipientInternalId() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddRecipientInternalIdResult = pnRaddAltLogContext.addRecipientInternalId("42");

        // Assert
        assertEquals("recipientInternalId=42 ", actualAddRecipientInternalIdResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddRecipientInternalIdResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#addDelegateInternalId(String)}
     */
    @Test
    void testAddDelegateInternalId() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddDelegateInternalIdResult = pnRaddAltLogContext.addDelegateInternalId("42");

        // Assert
        assertEquals("delegateInternalId=42 ", actualAddDelegateInternalIdResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddDelegateInternalIdResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#addTransactionId(String)}
     */
    @Test
    void testAddTransactionId() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddTransactionIdResult = pnRaddAltLogContext.addTransactionId("42");

        // Assert
        assertEquals("transactionId=42 ", actualAddTransactionIdResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddTransactionIdResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#addRequestFileKey(String)}
     */
    @Test
    void testAddRequestFileKey() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddRequestFileKeyResult = pnRaddAltLogContext.addRequestFileKey("Request File Key");

        // Assert
        assertEquals("requestFileKey=Request File Key ", actualAddRequestFileKeyResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddRequestFileKeyResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#addDownloadFilekeys(List)}
     */
    @Test
    void testAddDownloadFilekeys() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddDownloadFilekeysResult = pnRaddAltLogContext.addDownloadFilekeys(new ArrayList<>());

        // Assert
        assertEquals("downloadedFilekeys=[  ] ", actualAddDownloadFilekeysResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddDownloadFilekeysResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#addAarFilekeys(List)}
     */
    @Test
    void testAddAarFilekeys() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddAarFilekeysResult = pnRaddAltLogContext.addAarFilekeys(new ArrayList<>());

        // Assert
        assertEquals("aarFilekeys=[  ] ", actualAddAarFilekeysResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddAarFilekeysResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#addResponseResult(Boolean)}
     */
    @Test
    void testAddResponseResult() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddResponseResultResult = pnRaddAltLogContext.addResponseResult(true);

        // Assert
        assertEquals("result=true ", actualAddResponseResultResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddResponseResultResult);
    }

    /**
     * Method under test:
     * {@link PnRaddAltLogContext#addResponseStatus(ActInquiryResponseStatus)}
     */
    @Test
    void testAddResponseStatus() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddResponseStatusResult = pnRaddAltLogContext
                .addResponseStatus(new ActInquiryResponseStatus().toString());

        // Assert
        assertEquals("status=ActInquiryResponseStatus(code=null, message=null) ",
                actualAddResponseStatusResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddResponseStatusResult);
    }

    /**
     * Method under test:
     * {@link PnRaddAltLogContext#addResponseStatus(ResponseStatus)}
     */
    @Test
    void testAddResponseStatus2() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddResponseStatusResult = pnRaddAltLogContext
                .addResponseStatus(new ResponseStatus().toString());

        // Assert
        assertEquals("status=ResponseStatus(code=null, message=null) ", actualAddResponseStatusResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddResponseStatusResult);
    }

    /**
     * Method under test:
     * {@link PnRaddAltLogContext#addResponseStatus(TransactionResponseStatus)}
     */
    @Test
    void testAddResponseStatus3() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddResponseStatusResult = pnRaddAltLogContext
                .addResponseStatus(new TransactionResponseStatus().toString());

        // Assert
        assertEquals("status=TransactionResponseStatus(code=null, message=null) ",
                actualAddResponseStatusResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddResponseStatusResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#addOperationId(String)}
     */
    @Test
    void testAddOperationId() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddOperationIdResult = pnRaddAltLogContext.addOperationId("42");

        // Assert
        assertEquals("operationId=42 ", actualAddOperationIdResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddOperationIdResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#addIun(String)}
     */
    @Test
    void testAddIun() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddIunResult = pnRaddAltLogContext.addIun("Iun");

        // Assert
        assertEquals("iun=Iun ", actualAddIunResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddIunResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#addIuns(List)}
     */
    @Test
    void testAddIuns() {
        // Arrange
        PnRaddAltLogContext pnRaddAltLogContext = new PnRaddAltLogContext();

        // Act
        PnRaddAltLogContext actualAddIunsResult = pnRaddAltLogContext.addIuns(new ArrayList<>());

        // Assert
        assertEquals("iuns=[  ] ", actualAddIunsResult.logContext());
        assertSame(pnRaddAltLogContext, actualAddIunsResult);
    }

    /**
     * Method under test: {@link PnRaddAltLogContext#logContext()}
     */
    @Test
    void testLogContext() {
        // Arrange, Act and Assert
        assertEquals("", (new PnRaddAltLogContext()).logContext());
    }
}
