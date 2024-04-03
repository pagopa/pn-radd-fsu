package it.pagopa.pn.radd.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.exceptions.PnInternalException;
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
}

