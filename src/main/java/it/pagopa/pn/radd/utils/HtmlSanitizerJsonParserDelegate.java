package it.pagopa.pn.radd.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import org.apache.commons.text.StringEscapeUtils;
import org.owasp.html.PolicyFactory;

import java.io.IOException;

import static it.pagopa.pn.commons.exceptions.PnExceptionsCodes.ERROR_CODE_PN_GENERIC_ERROR;

/**
 * Class that overrides the default behavior of JsonParserDelegate by sanitizing all fields of type String,
 * returned by the {@link #getText()} method.
 * <p>
 * It allows you to scroll through a JsonNode (via @{@link com.fasterxml.jackson.databind.JsonNode}.traverse),
 * and customize the parsing of each field.
 * <p>
 * Example usage:
 * <p>
 * JsonNode jsonNode = objectMapper.valueToTree(model);
 * JsonParser traverse = jsonNode.traverse();
 * HtmlSanitizerJsonParserDelegate htmlSanitizerJsonParserDelegate = new HtmlSanitizerJsonParserDelegate(traverse, policy);
 * Object sanitizedObject = objectMapper.readValue(htmlSanitizerJsonParserDelegate, model.getClass());
 */
public class HtmlSanitizerJsonParserDelegate extends JsonParserDelegate {

    private final PolicyFactory policy;

    private final HtmlSanitizer.SanitizeMode sanitizeMode;


    public HtmlSanitizerJsonParserDelegate(JsonParser d, PolicyFactory policy, HtmlSanitizer.SanitizeMode sanitizeMode) {
        super(d);
        this.policy = policy;
        this.sanitizeMode = sanitizeMode;
    }

    /**
     * @return a string sanitized from HTML elements by the PolicyFactory class.
     * <p>
     * The use of StringEscapeUtils.unescapeHtml4(sanitized) is necessary because PolicyFactory encodes special
     * characters such as apostrophe.
     * <p>
     * Example of the use of string with special characters:
     * String sanitized = policy.sanitize("via dell'Aquila"); //sanitized = "via dell&#39;Aquila"
     * StringEscapeUtils.unescapeHtml4("via dell&#39;Aquila"); // return "via dell'Aquila"
     * @throws IOException if an error occurs during parsing.
     */
    @Override
    public String getText() throws IOException {
        String text = this.delegate.getText();

        if (sanitizeMode == HtmlSanitizer.SanitizeMode.DELETE_HTML) {
            String sanitized = policy.sanitize(text);
            return StringEscapeUtils.unescapeHtml4(sanitized);
        } else if (sanitizeMode == HtmlSanitizer.SanitizeMode.ESCAPING) {
            return StringEscapeUtils.escapeHtml4(text);
        } else {
            throw new PnInternalException(String.format("No valid sanitizedMode found! SanitizeMode value: %s", sanitizeMode),
                    ERROR_CODE_PN_GENERIC_ERROR);
        }

    }


}
