package it.pagopa.pn.radd.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {ObjectMapperUtil.class})
@ExtendWith(SpringExtension.class)
class ObjectMapperUtilTest {
    @MockBean
    private ObjectMapper objectMapper;

    @Autowired
    private ObjectMapperUtil objectMapperUtil;

    /**
     * Method under test: {@link ObjectMapperUtil#toJson(Object)}
     */
    @Test
    void testToJson() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(Mockito.<Object>any())).thenReturn("42");
        assertEquals("42", objectMapperUtil.toJson("Obj"));
        verify(objectMapper).writeValueAsString(Mockito.<Object>any());
    }

    /**
     * Method under test: {@link ObjectMapperUtil#toJson(Object)}
     */
    @Test
    void testToJson2() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(Mockito.<Object>any())).thenThrow(mock(JsonProcessingException.class));
        assertThrows(PnInternalException.class, () -> objectMapperUtil.toJson("Obj"));
        verify(objectMapper).writeValueAsString(Mockito.<Object>any());
    }

    /**
     * Method under test: {@link ObjectMapperUtil#toJson(Object)}
     */
    @Test
    void testToJson3() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(Mockito.<Object>any())).thenThrow(mock(JsonProcessingException.class));
        assertThrows(PnInternalException.class, () -> objectMapperUtil.toJson(2));
        verify(objectMapper).writeValueAsString(Mockito.<Object>any());
    }

    /**
     * Method under test: {@link ObjectMapperUtil#toJson(Object)}
     */
    @Test
    void testToJson4() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(Mockito.<Object>any()))
                .thenThrow(new PnInternalException("An error occurred"));
        assertThrows(PnInternalException.class, () -> objectMapperUtil.toJson("Obj"));
        verify(objectMapper).writeValueAsString(Mockito.<Object>any());
    }

    @Test
    void toObject_shouldReturnObject_whenJsonIsValid() throws JsonProcessingException {
        String json = "{\"key\":\"value\"}";
        TestObj expected = new TestObj("value");
        when(objectMapper.readValue(json, TestObj.class)).thenReturn(expected);

        TestObj result = objectMapperUtil.toObject(json, TestObj.class);

        assertEquals(expected.getKey(), result.getKey());
    }

    @Test
    void toObject_shouldThrowPnInternalException_whenJsonProcessingFails() throws JsonProcessingException {
        String json = "{\"key\":\"value\"}";
        when(objectMapper.readValue(json, TestObj.class)).thenThrow(new JsonProcessingException("error") {});

        PnInternalException exception = assertThrows(PnInternalException.class, () -> objectMapperUtil.toObject(json, TestObj.class));
        assertEquals(500, exception.getProblem().getStatus());
    }

    @Test
    void convert_shouldReturnConvertedObject_whenInputIsValid() {
        TestObj input = new TestObj("value");
        TestObj expected = new TestObj("value");
        when(objectMapper.convertValue(input, TestObj.class)).thenReturn(expected);

        TestObj result = objectMapperUtil.convert(input, TestObj.class);

        assertEquals(expected.getKey(), result.getKey());
    }

    @Test
    void convert_shouldThrowPnInternalException_whenConversionFails() {
        TestObj input = new TestObj("value");
        when(objectMapper.convertValue(input, TestObj.class)).thenThrow(new RuntimeException("conversion error"));

        PnInternalException exception = assertThrows(PnInternalException.class, () -> objectMapperUtil.convert(input, TestObj.class));
        assertEquals(500, exception.getProblem().getStatus());
    }

    @Setter
    @Getter
    private static class TestObj {
        private String key;
        public TestObj(String key) {
            this.key = key;
        }
    }

}