package br.pucpr.projeto.core;

import br.pucpr.projeto.auth.exception.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Modelo padrão de erro
    public record ApiError(
            LocalDateTime timestamp,
            int status,
            String code,
            String error,
            String message,
            String path,
            Map<String, ?> details
    ) {}

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message, HttpServletRequest req, Map<String, ?> details) {
        var err = new ApiError(
                LocalDateTime.now(),
                status.value(),
                code,
                status.getReasonPhrase(),
                message,
                req != null ? req.getRequestURI() : null,
                details
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Erro de validação nos campos", req, Map.of("fields", fieldErrors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg = "Parâmetro inválido: " + ex.getName();
        return build(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", msg, req, Map.of("requiredType", String.valueOf(ex.getRequiredType())));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_JSON", "Corpo da requisição inválido ou malformado", req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String msg = simplifySqlError(ex);
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY", msg, req, null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Acesso negado", req, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), req, null);
    }

    @ExceptionHandler({java.util.NoSuchElementException.class})
    public ResponseEntity<ApiError> handleNotFound(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "Recurso não encontrado", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Erro interno", req, Map.of("exception", ex.getClass().getSimpleName()));
    }

    private String simplifySqlError(DataIntegrityViolationException ex) {
        String s = String.valueOf(ex.getMessage());
        String lower = s.toLowerCase();
        if (lower.contains("duplicate") || lower.contains("unique")) return "Registro duplicado/violação de unicidade";
        if (lower.contains("foreign key")) return "Violação de chave estrangeira";
        return "Violação de integridade de dados";
    }
}
