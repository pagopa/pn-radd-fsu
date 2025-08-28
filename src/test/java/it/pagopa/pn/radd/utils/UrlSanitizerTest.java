package it.pagopa.pn.radd.utils;

import it.pagopa.pn.radd.exception.UrlSanitizeException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UrlSanitizerTest {

    @Test
    void sanitizeUrl_withValidHttpsUrl_shouldReturnSanitizedUrl() {
        String input = "https://example.com/test";
        String expected = "https://example.com/test";
        String result = UrlSanitizer.sanitizeUrl(input);
        assertEquals(expected, result);
    }

    @Test
    void sanitizeUrl_withValidUrlWithoutProtocol_shouldAddHttps() {
        String input = "example.com/test";
        String expected = "https://example.com/test";
        String result = UrlSanitizer.sanitizeUrl(input);
        assertEquals(expected, result);
    }

    @Test
    void sanitizeUrl_withPort_shouldPreservePort() {
        String input = "https://example.com:8443/test";
        String expected = "https://example.com:8443/test";
        String result = UrlSanitizer.sanitizeUrl(input);
        assertEquals(expected, result);
    }

    @Test
    void sanitizeUrl_withQueryString_shouldPreserveQuery() {
        String input = "example.com/test?foo=bar";
        String expected = "https://example.com/test?foo=bar";
        String result = UrlSanitizer.sanitizeUrl(input);
        assertEquals(expected, result);
    }

    @Test
    void sanitizeUrl_withNull_shouldThrowException() {
        UrlSanitizeException ex = assertThrows(
                UrlSanitizeException.class,
                () -> UrlSanitizer.sanitizeUrl(null)
                                              );
        assertTrue(ex.getMessage().contains("vuoto o nullo"));
    }

    @Test
    void sanitizeUrl_withEmptyString_shouldThrowException() {
        UrlSanitizeException ex = assertThrows(
                UrlSanitizeException.class,
                () -> UrlSanitizer.sanitizeUrl("   ")
                                              );
        assertTrue(ex.getMessage().contains("vuoto o nullo"));
    }

    @Test
    void sanitizeUrl_withDangerousCharacters_shouldThrowException() {
        UrlSanitizeException ex = assertThrows(
                UrlSanitizeException.class,
                () -> UrlSanitizer.sanitizeUrl("example.com/<script>")
                                              );
        assertTrue(ex.getMessage().contains("caratteri non validi"));
    }

    @Test
    void sanitizeUrl_withUnsupportedProtocol_shouldThrowException() {
        UrlSanitizeException ex = assertThrows(
                UrlSanitizeException.class,
                () -> UrlSanitizer.sanitizeUrl("http://example.com")
                                              );
        assertTrue(ex.getMessage().contains("Protocollo non supportato"));
    }

    @Test
    void sanitizeUrl_withInvalidUriSyntax_shouldThrowException() {
        UrlSanitizeException ex = assertThrows(
                UrlSanitizeException.class,
                () -> UrlSanitizer.sanitizeUrl("https://")
                                              );
        assertTrue(ex.getMessage().contains("Errore nella sanitizzazione"));
    }

    @Test
    void sanitizeUrl_withoutTLD_shouldThrowException() {
        UrlSanitizeException ex = assertThrows(
                UrlSanitizeException.class,
                () -> UrlSanitizer.sanitizeUrl("www.esempio")
                                              );
        assertTrue(ex.getMessage().contains("TLD non valido"));
    }

}
