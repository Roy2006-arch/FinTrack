package com.fintrack.exception;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ResponseEntity<ApiError> response(int status, String message, HttpServletRequest req) { return ResponseEntity.status(status).body(new ApiError(status, message, req.getRequestURI())); }
    @ExceptionHandler(ResourceNotFoundException.class) public ResponseEntity<ApiError> missing(ResourceNotFoundException ex, HttpServletRequest req) { return response(404, ex.getMessage(), req); }
    @ExceptionHandler(ConflictException.class) public ResponseEntity<ApiError> conflict(ConflictException ex, HttpServletRequest req) { return response(409, ex.getMessage(), req); }
    @ExceptionHandler(BadRequestException.class) public ResponseEntity<ApiError> badRequest(BadRequestException ex, HttpServletRequest req) { return response(400, ex.getMessage(), req); }
    @ExceptionHandler(BadCredentialsException.class) public ResponseEntity<ApiError> credentials(BadCredentialsException ex, HttpServletRequest req) { return response(401, "Email or password is incorrect", req); }
    @ExceptionHandler(BindException.class) public ResponseEntity<ApiError> validation(BindException ex, HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>(); ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError(Instant.now(), 400, "Please correct the highlighted values", req.getRequestURI(), errors));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiError> malformed(Exception ex, HttpServletRequest req) { return response(400, "Invalid request values, dates, or enum names", req); }
    @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<ApiError> integrity(DataIntegrityViolationException ex, HttpServletRequest req) { return response(409, "Duplicate value or resource is still in use", req); }
    @ExceptionHandler(NoResourceFoundException.class) public ResponseEntity<ApiError> route(NoResourceFoundException ex, HttpServletRequest req) { return response(404, "Endpoint not found", req); }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class) public ResponseEntity<ApiError> method(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) { return response(405, "HTTP method is not supported", req); }
    @ExceptionHandler(Exception.class) public ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest req) { log.error("Unexpected request failure at {}", req.getRequestURI(), ex); return response(500, "An unexpected error occurred. Please try again.", req); }
}
