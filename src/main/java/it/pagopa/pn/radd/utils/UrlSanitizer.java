package it.pagopa.pn.radd.utils;

import it.pagopa.pn.radd.exception.UrlSanitizeException;
import lombok.CustomLog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

@CustomLog
public class UrlSanitizer {

    private static final Pattern SAFE_CHARS = Pattern.compile("^[a-zA-Z0-9:/?#\\[\\]@!$&'()*+,;=_\\-.~%]*$");
    private static final Pattern SCHEME_REGEX = Pattern.compile("^[a-z][a-z0-9+.-]*://.*");
    private static final Pattern VALID_TLD_PATTERN = Pattern.compile(".*\\.[a-zA-Z]{2,}$");

    public static String sanitizeUrl(String inputUrl) {
        log.debug("Sanitizing URL: {}", inputUrl);
        validateInput(inputUrl);

        String url = formatUrl(inputUrl.trim().toLowerCase());
        return normalizeUrl(url);
    }

    private static void validateInput(String inputUrl) {
        if (inputUrl == null || inputUrl.trim().isEmpty()) {
            throw new UrlSanitizeException("L'URL non può essere vuoto o nullo.");
        }
        if (!SAFE_CHARS.matcher(inputUrl).matches()) {
            throw new UrlSanitizeException("URL contiene caratteri non validi: " + inputUrl);
        }
    }

    private static String formatUrl(String url) {
        if (SCHEME_REGEX.matcher(url).matches() && !url.startsWith("https://")) {
            throw new UrlSanitizeException("Protocollo non supportato per l'URL: " + url);
        }
        return url.startsWith("https://") ? url : "https://" + url;
    }

    private static String normalizeUrl(String url) {
        try {
            URI uri = new URI(url).normalize();
            String sanitizedUrl = uri.toString();

            String domainName = sanitizedUrl.replaceAll("http(s)?://|www\\.|/.*", "");

            if (!VALID_TLD_PATTERN.matcher(domainName).matches() && domainName.lastIndexOf(":")<0) {
                throw new UrlSanitizeException("URL contiene un TLD non valido: " + url);
            }

            log.debug("URL sanitizzato: {}", sanitizedUrl);
            return sanitizedUrl;
        } catch (URISyntaxException e) {
            throw new UrlSanitizeException("Errore nella sanitizzazione dell'URL: " + e.getMessage());
        }
    }
}