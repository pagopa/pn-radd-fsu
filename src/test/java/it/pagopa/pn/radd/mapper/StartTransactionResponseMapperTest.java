package it.pagopa.pn.radd.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.DownloadUrl;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.StartTransactionResponse;
import it.pagopa.pn.radd.alt.generated.openapi.server.v1.dto.StartTransactionResponseStatus;
import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.exception.IunAlreadyExistsException;
import it.pagopa.pn.radd.exception.PaperNotificationFailedEmptyException;
import it.pagopa.pn.radd.exception.RaddGenericException;

import java.util.ArrayList;
import java.util.List;



import org.junit.jupiter.api.Test;

class StartTransactionResponseMapperTest {

    @Test
    void testFromResult() {
        // Arrange and Act
        StartTransactionResponse actualFromResultResult = StartTransactionResponseMapper.fromResult(new ArrayList<>(), "PF", "1", "", new ArrayList<>());

        // Assert
        StartTransactionResponseStatus status = actualFromResultResult.getStatus();
        assertEquals("OK", status.getMessage());
        List<DownloadUrl> downloadUrlList = actualFromResultResult.getDownloadUrlList();
        assertEquals(1, downloadUrlList.size());
        DownloadUrl getResult = downloadUrlList.get(0);
        assertEquals("/radd-net/api/v1/download/PF/1", getResult.getUrl());
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_0, status.getCode());
        assertTrue(getResult.getNeedAuthentication());
    }


    @Test
    void testFromResult2() {
        // Arrange
        ArrayList<String> result = new ArrayList<>();
        result.add("new api");

        // Act
        StartTransactionResponse actualFromResultResult = StartTransactionResponseMapper.fromResult(StartTransactionResponseMapper.getDownloadUrls(result), "PF", "1", "", new ArrayList<>());

        // Assert
        StartTransactionResponseStatus status = actualFromResultResult.getStatus();
        assertEquals("OK", status.getMessage());
        List<DownloadUrl> downloadUrlList = actualFromResultResult.getDownloadUrlList();
        assertEquals(2, downloadUrlList.size());
        DownloadUrl getResult = downloadUrlList.get(0);
        assertEquals("/radd-net/api/v1/download/PF/1", getResult.getUrl());
        DownloadUrl getResult2 = downloadUrlList.get(1);
        assertEquals("new api", getResult2.getUrl());
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_0, status.getCode());
        assertFalse(getResult2.getNeedAuthentication());
        assertTrue(getResult.getNeedAuthentication());
    }

    @Test
    void testFromResult3() {
        // Arrange
        ArrayList<String> result = new ArrayList<>();
        result.add("OK");
        result.add("new api");

        // Act
        StartTransactionResponse actualFromResultResult = StartTransactionResponseMapper.fromResult(StartTransactionResponseMapper.getDownloadUrls(result), "PF", "1", "", new ArrayList<>());

        // Assert
        List<DownloadUrl> downloadUrlList = actualFromResultResult.getDownloadUrlList();
        assertEquals(3, downloadUrlList.size());
        DownloadUrl getResult = downloadUrlList.get(1);
        assertEquals("OK", getResult.getUrl());
        StartTransactionResponseStatus status = actualFromResultResult.getStatus();
        assertEquals("OK", status.getMessage());
        DownloadUrl getResult2 = downloadUrlList.get(0);
        assertEquals("/radd-net/api/v1/download/PF/1", getResult2.getUrl());
        DownloadUrl getResult3 = downloadUrlList.get(2);
        assertEquals("new api", getResult3.getUrl());
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_0, status.getCode());
        assertFalse(getResult.getNeedAuthentication());
        assertFalse(getResult3.getNeedAuthentication());
        assertTrue(getResult2.getNeedAuthentication());
    }


    @Test
    void testFromException() {
        // Arrange and Act
        StartTransactionResponse actualFromExceptionResult = StartTransactionResponseMapper
                .fromException(new RaddGenericException(ExceptionTypeEnum.RETRY_AFTER, 10000));

        // Assert
        StartTransactionResponseStatus status = actualFromExceptionResult.getStatus();
        assertEquals("Documento non disponibile per il download", status.getMessage());
        assertNull(actualFromExceptionResult.getDownloadUrlList());
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_2, status.getCode());
    }

    /**
     * Method under test: {@link StartTransactionResponseMapper#fromException(RaddGenericException)}
     */
    @Test
    void testFromException2() {
        StartTransactionResponse actualFromExceptionResult = StartTransactionResponseMapper
                .fromException(new RaddGenericException(ExceptionTypeEnum.IUN_NOT_FOUND));
        assertNull(actualFromExceptionResult.getDownloadUrlList());
        StartTransactionResponseStatus status = actualFromExceptionResult.getStatus();
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_99, status.getCode());
        assertEquals("Iun not found with params", status.getMessage());
    }

    /**
     * Method under test: {@link StartTransactionResponseMapper#fromException(RaddGenericException)}
     */
    @Test
    void testFromException3() {
        StartTransactionResponse actualFromExceptionResult = StartTransactionResponseMapper
                .fromException(new RaddGenericException(ExceptionTypeEnum.TRANSACTION_ALREADY_EXIST));
        assertNull(actualFromExceptionResult.getDownloadUrlList());
        StartTransactionResponseStatus status = actualFromExceptionResult.getStatus();
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_5, status.getCode());
        assertEquals("Transazione già esistente o con stato completed o aborted", status.getMessage());
    }

    /**
     * Method under test: {@link StartTransactionResponseMapper#fromException(RaddGenericException)}
     */
    @Test
    void testFromException4() {
        StartTransactionResponse actualFromExceptionResult = StartTransactionResponseMapper
                .fromException(new IunAlreadyExistsException());
        assertNull(actualFromExceptionResult.getDownloadUrlList());
        StartTransactionResponseStatus status = actualFromExceptionResult.getStatus();
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_3, status.getCode());
        assertEquals("Stampa già eseguita", status.getMessage());
    }

    /**
     * Method under test: {@link StartTransactionResponseMapper#fromException(RaddGenericException)}
     */
    @Test
    void testFromException5() {
        StartTransactionResponse actualFromExceptionResult = StartTransactionResponseMapper
                .fromException(new PaperNotificationFailedEmptyException());
        assertNull(actualFromExceptionResult.getDownloadUrlList());
        StartTransactionResponseStatus status = actualFromExceptionResult.getStatus();
        assertEquals(StartTransactionResponseStatus.CodeEnum.NUMBER_10, status.getCode());
        assertEquals("Non ci sono notifiche non consegnate per questo codice fiscale", status.getMessage());
    }
}
