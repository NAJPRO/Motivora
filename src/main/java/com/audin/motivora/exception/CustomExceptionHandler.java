package com.audin.motivora.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.audin.motivora.entity.ApiError;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {

    // Erreurs de validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    // Ressource non trouvée
    @ExceptionHandler({EntityNotFoundException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex) {
        ApiError apiError = new ApiError();
        apiError.setCode(HttpStatus.NOT_FOUND.value());
        apiError.setMessage("Resource not found: " + ex.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    // Accès refusé (Spring Security)
    @ExceptionHandler({AccessDeniedException.class, IllegalStateException.class})
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        ApiError apiError = new ApiError();
        apiError.setCode(HttpStatus.FORBIDDEN.value());
        apiError.setMessage("Access denied: " + ex.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    // Authentification échouée (ex: JWT invalide)
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiError> handleSecurityException(SecurityException ex) {
        ApiError apiError = new ApiError();
        apiError.setCode(HttpStatus.UNAUTHORIZED.value());
        apiError.setMessage("Authentication failed: " + ex.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    // Violation de contrainte en base (clé unique, FK, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ApiError apiError = new ApiError(
                "Database constraint violated: " + extractSqlMessage(ex),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    // Mauvais type de paramètre (ex: String au lieu d’un Long)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ApiError apiError = new ApiError(
                String.format("Invalid value for parameter '%s': expected type %s",
                        ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    // Paramètre manquant dans la requête
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParams(MissingServletRequestParameterException ex) {
        ApiError apiError = new ApiError(
                String.format("Missing required parameter: %s", ex.getParameterName()),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    // Méthode HTTP non supportée
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ApiError apiError = new ApiError(
                "HTTP method not supported for this endpoint",
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(apiError);
    }

    // Exception métier personnalisée
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex) {
        ApiError apiError = new ApiError(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(NoResourceFoundException ex) {
        ApiError apiError = new ApiError(
                "The requested endpoint does not exist: " + ex.getResourcePath(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    // Catch-all (tout le reste)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex) {
        ApiError apiError = new ApiError(
                "Internal server error: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    // Helper pour extraire les messages SQL utiles
    private String extractSqlMessage(DataIntegrityViolationException ex) {
        if (ex.getRootCause() != null) {
            return ex.getRootCause().getMessage();
        }
        return ex.getMessage();
    }
}
