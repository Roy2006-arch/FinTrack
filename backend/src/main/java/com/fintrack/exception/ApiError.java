package com.fintrack.exception;
import java.time.Instant;
import java.util.Map;
public record ApiError(Instant timestamp, int status, String message, String path, Map<String, String> errors) {
    public ApiError(int status, String message, String path) { this(Instant.now(), status, message, path, Map.of()); }
}
