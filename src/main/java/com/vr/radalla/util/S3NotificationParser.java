package com.vr.radalla.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class S3NotificationParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static S3ObjectInfo parseS3Event(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode record = root.path("Records").get(0);

            String bucket = record.path("s3").path("bucket").path("name").asText();
            String key = record.path("s3").path("object").path("key").asText();

            return new S3ObjectInfo(bucket, key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse S3 event from SQS message", e);
        }
    }

    public record S3ObjectInfo(String bucket, String key) {}
}
