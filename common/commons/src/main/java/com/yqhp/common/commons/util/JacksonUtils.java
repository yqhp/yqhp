package com.yqhp.common.commons.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;

/**
 * @author jiangyitao
 */
public class JacksonUtils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModules(new JavaTimeModule());

    public static JsonNode readTree(String value) {
        try {
            return OBJECT_MAPPER.readTree(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T readValue(String value, TypeReference<T> valueTypeRef) {
        try {
            return OBJECT_MAPPER.readValue(value, valueTypeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T readValue(String value, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(value, valueType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String writeValueAsString(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T treeToValue(JsonNode node, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.treeToValue(node, valueType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String pretty(Object value) {
        try {
            return OBJECT_MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(
                            value instanceof String
                                    ? readValue((String) value, Map.class)
                                    : value
                    );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
