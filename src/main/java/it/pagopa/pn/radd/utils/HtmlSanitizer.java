package it.pagopa.pn.radd.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static it.pagopa.pn.commons.exceptions.PnExceptionsCodes.ERROR_CODE_PN_GENERIC_ERROR;

/**
 * Class that performs via the {@link #sanitize(Object)} method a cleanup of input parameters,
 * escaping HTML element or deleting them, based on the {@link #sanitizeMode} property (ESCAPING or DELETE_HTML).
 * <p>
 * DELETE_HTML mode allows only determined HTML elements, based on the policies set in the constructor via the field {@link #policy}.
 * <p>
 * For example, if you wanted to allow only the HTML img element in the string, you could use:
 * PolicyFactory policy = Sanitizers.IMAGES;
 * or if you want no one HTML element: PolicyFactory policy = new HtmlPolicyBuilder().allowElements("").toFactory();
 * <p>
 * Example of usage with no HTML element policy:
 * <p>
 * String sanitized = policy.sanitize("<html><h1>SSRF WITH IMAGE POC</h1> <img src='https://prova.it'></img></html>");
 * // sanitized value is "SSRF WITH IMAGE POC".
 */

public class HtmlSanitizer {


    private final ObjectMapper objectMapper;

    private final SanitizeMode sanitizeMode;

    private final PolicyFactory policy;

    public HtmlSanitizer(ObjectMapper objectMapper, SanitizeMode sanitizeMode) {
        this.objectMapper = objectMapper;
        this.sanitizeMode = sanitizeMode;
        this.policy = new HtmlPolicyBuilder().allowElements("").toFactory();
    }

    public Object sanitize(Object model) {
        try {

            if (model instanceof Map) {
                return doSanitize((Map<Object, Object>) model);
            }
            if (model instanceof Collection) {
                return doSanitize((Collection<Object>) model);
            }

            return doSanitize(model);


        } catch (IOException e) {
            throw new PnInternalException(e.getMessage(), ERROR_CODE_PN_GENERIC_ERROR, e);
        }
    }

    public Object doSanitize(Object model) throws IOException {
        if (model == null) return null;

        JsonNode jsonNode = objectMapper.valueToTree(model);
        JsonParser traverse = jsonNode.traverse();
        HtmlSanitizerJsonParserDelegate htmlSanitizerJsonParserDelegate = new HtmlSanitizerJsonParserDelegate(traverse, policy, sanitizeMode);

        return objectMapper.readValue(htmlSanitizerJsonParserDelegate, model.getClass());
    }

    public Map<Object, Object> doSanitize(Map<Object, Object> modelMap) {
        if (CollectionUtils.isEmpty(modelMap)) {
            return modelMap;
        }

        Map<Object, Object> sanitizedMap = copyMap(modelMap);

        for (Map.Entry<Object, Object> entry : sanitizedMap.entrySet()) {
            Object sanitized = sanitize(entry.getValue());
            sanitizedMap.put(entry.getKey(), sanitized);
        }

        return sanitizedMap;

    }

    private Map<Object, Object> copyMap(Map<Object, Object> map) {
        if (map instanceof SortedMap) {
            return new TreeMap<>((SortedMap<Object, Object>) map);
        }
        if (map instanceof ConcurrentMap) {
            return new ConcurrentHashMap<>(map);
        }
        if (map instanceof LinkedHashMap) {
            return new LinkedHashMap<>(map);
        }
        return new HashMap<>(map);
    }

    public Collection<Object> doSanitize(Collection<Object> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return collection;
        }

        Collection<Object> sanitizedCollection = createCollectionInstance(collection);

        for (Object o : collection) {
            Object sanitized = sanitize(o);
            sanitizedCollection.add(sanitized);
        }
        return sanitizedCollection;
    }

    private Collection<Object> createCollectionInstance(Collection<Object> collection) {
        if (collection instanceof Set) {
            return createSetInstance((Set<Object>) collection);
        }

        return createListInstance((List<Object>) collection);
    }

    private Set<Object> createSetInstance(Set<Object> set) {
        if (set instanceof SortedSet) {
            return new TreeSet<>();
        }
        if (set instanceof LinkedHashSet) {
            return new LinkedHashSet<>();
        }
        return new HashSet<>();
    }

    private List<Object> createListInstance(List<Object> list) {
        if (list instanceof AbstractSequentialList) {
            return new LinkedList<>();
        }
        return new ArrayList<>();
    }

    public enum SanitizeMode {
        ESCAPING,
        DELETE_HTML
    }
}
