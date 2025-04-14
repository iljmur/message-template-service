package com.vr.radalla.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TestResourceUtils {

    public static String load(String path) {
        try (var input = Objects.requireNonNull(TestResourceUtils.class.getClassLoader().getResourceAsStream(path))) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not load test resource: " + path, e);
        }
    }
}
