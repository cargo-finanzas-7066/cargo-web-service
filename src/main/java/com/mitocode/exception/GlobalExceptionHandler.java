package com.mitocode.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail notFound(ResourceNotFoundException ex) { return problem(HttpStatus.NOT_FOUND, ex.getMessage()); }
    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail noResource(NoResourceFoundException ex) { return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado"); }
    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    ProblemDetail conflict(RuntimeException ex) { return problem(HttpStatus.CONFLICT, "El recurso entra en conflicto con datos existentes"); }
    @ExceptionHandler(UnprocessableEntityException.class)
    ProblemDetail unprocessable(UnprocessableEntityException ex) { return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage()); }
    @ExceptionHandler(TooManyRequestsException.class)
    ProblemDetail tooMany(TooManyRequestsException ex) { return problem(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage()); }
    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail forbidden(AccessDeniedException ex) { return problem(HttpStatus.FORBIDDEN, "No tiene permisos para esta operación"); }
    @ExceptionHandler(PropertyReferenceException.class)
    ProblemDetail invalidSort(PropertyReferenceException ex) { return problem(HttpStatus.BAD_REQUEST, "Parámetro de ordenamiento inválido: " + ex.getPropertyName()); }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail typeMismatch(MethodArgumentTypeMismatchException ex) {
        return problem(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' tiene un valor inválido: " + ex.getValue());
    }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ProblemDetail badRequest(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException validation) {
            var errors = validation.getBindingResult().getFieldErrors().stream()
                    .collect(java.util.stream.Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage() == null ? "inválido" : e.getDefaultMessage(), (a,b) -> a));
            var detail = problem(HttpStatus.BAD_REQUEST, "La solicitud contiene campos inválidos");
            detail.setProperty("errors", errors);
            return detail;
        }
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(Exception.class)
    ProblemDetail internal(Exception ex) {
        log.error("Error no controlado procesando la solicitud", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno");
    }

    private ProblemDetail problem(HttpStatus status, String message) {
        var detail = ProblemDetail.forStatusAndDetail(status, message == null ? status.getReasonPhrase() : message);
        detail.setTitle(status.getReasonPhrase());
        detail.setProperty("timestamp", LocalDateTime.now().toString());
        return detail;
    }
}
