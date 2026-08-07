package com.yasarbilgi.ats.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuditPayloadSanitizer {
    private static final int MAX_LENGTH = 16_000;
    private static final Set<String> SENSITIVE_PARTS = Set.of(
            "password", "token", "secret", "authorization", "credential", "apikey", "api_key");
    private final ObjectMapper objectMapper;

    public String serialize(Object value) {
        if (value == null) return null;
        try {
            JsonNode tree = objectMapper.valueToTree(value);
            mask(tree);
            String json = objectMapper.writeValueAsString(tree);
            return json.length() <= MAX_LENGTH ? json : json.substring(0, MAX_LENGTH) + "...[truncated]";
        } catch (RuntimeException | JsonProcessingException exception) {
            return "{\"serializationError\":\"Payload could not be recorded\"}";
        }
    }

    private void mask(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            Iterator<String> fields = objectNode.fieldNames();
            while (fields.hasNext()) {
                String field = fields.next();
                if (isSensitive(field)) objectNode.put(field, "***");
                else mask(objectNode.get(field));
            }
        } else if (node instanceof ArrayNode arrayNode) {
            arrayNode.forEach(this::mask);
        }
    }

    private boolean isSensitive(String field) {
        String normalized = field.toLowerCase(Locale.ROOT);
        return SENSITIVE_PARTS.stream().anyMatch(normalized::contains);
    }
}
