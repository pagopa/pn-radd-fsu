package it.pagopa.pn.radd.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObjectMapperUtil {

    private static final String ERROR_MESSAGE_RADD_ALT_JSON_PROCESSING = "Errore durante il processo di mapping json.";
    private static final String ERROR_CODE_RADD_ALT_JSON_PROCESSING = "RADD_ALT_JSON_PROCESSING";

    private final ObjectMapper objectMapper;
    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new PnInternalException(ERROR_MESSAGE_RADD_ALT_JSON_PROCESSING, ERROR_CODE_RADD_ALT_JSON_PROCESSING, e);
        }
    }

    public <T> T toObject(String json, Class<T> newClass) {
        try {
            return objectMapper.readValue(json, newClass);
        } catch (JsonProcessingException e) {
            throw new PnInternalException(ERROR_MESSAGE_RADD_ALT_JSON_PROCESSING, ERROR_CODE_RADD_ALT_JSON_PROCESSING, e);
        }
    }

    public <T> T convert(Object toConvert, Class<T> newClass) {
        try {
            return objectMapper.convertValue(toConvert, newClass);
        } catch (Exception e) {
            throw new PnInternalException(ERROR_MESSAGE_RADD_ALT_JSON_PROCESSING, ERROR_CODE_RADD_ALT_JSON_PROCESSING, e);

        }
    }
}
