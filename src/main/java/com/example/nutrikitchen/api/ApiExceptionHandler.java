package com.example.nutrikitchen.api;

import static com.example.nutrikitchen.api.ApiModels.ApiError;

import com.example.nutrikitchen.service.ApiException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.ResponseEntity;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException exception) {
        return response(exception.getStatus(), exception.getCode(), exception.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(ApiExceptionHandler::fieldMessage).distinct().toList();
        return response(HttpStatus.BAD_REQUEST, "validation_failed", "Some values are missing or invalid.", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception) {
        List<String> details = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage()).toList();
        return response(HttpStatus.BAD_REQUEST, "validation_failed", "Some values are missing or invalid.", details);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleUnreadableRequest(Exception exception) {
        return response(HttpStatus.BAD_REQUEST, "invalid_request",
                "The request contains an invalid value or is not valid JSON.", List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleConflict(DataIntegrityViolationException exception) {
        return response(HttpStatus.CONFLICT, "data_conflict",
                "The change conflicts with existing kitchen data.", List.of());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleConcurrentUpdate(OptimisticLockingFailureException exception) {
        return response(HttpStatus.CONFLICT, "concurrent_update",
                "This pantry item changed in another request. Reload it and try again.", List.of());
    }

    private static String fieldMessage(FieldError error) {
        return error.getField() + ": " + (error.getDefaultMessage() == null ? "is invalid" : error.getDefaultMessage());
    }

    private static ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
                                                     List<String> details) {
        return ResponseEntity.status(status).body(new ApiError(code, message, Instant.now(), details));
    }
}
