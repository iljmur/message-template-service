package com.vr.radalla.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.radalla.model.MessageTemplate;

import java.util.HashMap;
import java.util.Map;

public class CmsTemplateTransformer {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static MessageTemplate transform(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode fields = root.path("items").get(0).path("fields");

            // Build includes map (id -> key)
            Map<String, String> includesMap = buildIncludesMap(root.path("includes").path("Entry"));

            String id = fields.path("key").asText();
            if (id == null || id.isBlank()) {
                throw new IllegalStateException("Missing or empty 'key' field in CMS template");
            }
            String name = fields.path("name").asText();
            String trafficType = fields.path("trafficType").path("sys").path("id").asText();
            String subject = flattenDocument(fields.path("subject"), includesMap);
            String body = flattenDocument(fields.path("body"), includesMap);

            return MessageTemplate.builder()
                    .id(id)
                    .name(name)
                    .trafficType(trafficType)
                    .subject(subject)
                    .body(body)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to transform CMS template", e);
        }
    }

    private static Map<String, String> buildIncludesMap(JsonNode entries) {
        Map<String, String> map = new HashMap<>();
        for (JsonNode entry : entries) {
            String id = entry.path("sys").path("id").asText();
            String key = entry.path("fields").path("key").asText();
            map.put(id, key);
        }
        return map;
    }

    private static String flattenDocument(JsonNode documentNode, Map<String, String> includes) {
        StringBuilder result = new StringBuilder();

        for (JsonNode paragraph : documentNode.path("content")) {
            for (JsonNode node : paragraph.path("content")) {
                // Literal text
                if (node.has("value")) {
                    result.append(node.get("value").asText());
                }
                // Embedded token
                else if (node.has("data")) {
                    String id = node.path("data").path("target").path("sys").path("id").asText();
                    String key = includes.getOrDefault(id, id); // fallback to ID if key not found
                    result.append("{").append(key).append("}");
                }
            }
            result.append("\n");
        }

        return result.toString().trim();
    }
}
