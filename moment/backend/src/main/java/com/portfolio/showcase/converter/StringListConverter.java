package com.portfolio.showcase.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Attribute Converter for converting List<String> to JSON string and vice versa.
 * Used for storing the 'tools' array field in the database as JSON.
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final Logger log = LoggerFactory.getLogger(StringListConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting list to JSON: {}", attribute, e);
            return "[]";
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to list: {}", dbData, e);
            return new ArrayList<>();
        }
    }
}
