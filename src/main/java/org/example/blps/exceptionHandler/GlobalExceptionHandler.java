package org.example.blps.exceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.blps.dto.responseDto.ErrorResponceDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.naming.AuthenticationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponceDto> handleGenericException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponceDto errorResponceDto = new ErrorResponceDto(status.value(), ex.getMessage());
        log.warn(" handleGenericException сработал!");
        return ResponseEntity.status(status).body(errorResponceDto);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponceDto> handleBadRequestException(IllegalArgumentException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    ErrorResponceDto errorResponceDto = new ErrorResponceDto(status.value(), ex.getMessage());
    log.warn("handleBadRequestException сработал!");
    return ResponseEntity.status(status).body(errorResponceDto);
  }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponceDto> handleAuthenticationException(AuthenticationException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorResponceDto errorResponceDto = new ErrorResponceDto(status.value(), ex.getMessage());
        log.warn("handleAuthenticationException сработал!");
        return ResponseEntity.status(status).body(errorResponceDto);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponceDto> handleConflictExceptions(Exception ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponceDto errorResponceDto = new ErrorResponceDto(status.value(), ex.getMessage());
        log.warn("handleConflictExceptions сработал");
        return ResponseEntity.status(status).body(errorResponceDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponceDto> handleNotFoundException(Exception ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponceDto errorResponceDto = new ErrorResponceDto(status.value(), ex.getMessage());
        log.warn("handleNotFoundExceptin сработал");
        return ResponseEntity.status(status).body(errorResponceDto);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponceDto> handleForbiddenExceptions(Exception ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ErrorResponceDto errorResponceDto = new ErrorResponceDto(status.value(), ex.getMessage());
        log.warn("handleForbiddenExceptions сработал");
        return ResponseEntity.status(status).body(errorResponceDto);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponceDto> handleConstraintViolation(ConstraintViolationException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> "Ошибка валидации: " + violation.getMessage())
                .orElse("Ошибка валидации");
        ErrorResponceDto errorResponceDto = new ErrorResponceDto(status.value(), message);
        log.warn("handleConstraintViolation сработал!");
        return ResponseEntity.status(status).body(errorResponceDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponceDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> "Ошибка валидации: " + error.getField() + " - " + error.getDefaultMessage())
                .orElse("Ошибка валидации");
        ErrorResponceDto errorResponceDto = new ErrorResponceDto(status.value(), message);
        log.warn("handleMethodArgumentNotValidException сработал!");
        return ResponseEntity.status(status).body(errorResponceDto);
    }
}

