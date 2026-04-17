package org.example.blps.exceptionHandler;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.blps.dto.responseDto.ErrorResponceDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.naming.AuthenticationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Глобальный обработчик ошибки, плохой запрос - ловит все ошибки, что мы не поймали.

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponceDto> handleGenericException(Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponceDto errorResponceDto = new ErrorResponceDto(status.value(), ex.getMessage());
        return ResponseEntity.status(status).body(errorResponceDto);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponceDto> handleBadRequestException(IllegalArgumentException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    ErrorResponceDto errorResponceDto = new ErrorResponceDto(status.value(), ex.getMessage());
    return ResponseEntity.status(status).body(errorResponceDto);
  }


    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponceDto> handleAuntificationException(AuthenticationException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponceDto errorResponceDto = new ErrorResponceDto(status.value(), ex.getMessage());
        return ResponseEntity.status(status).body(errorResponceDto);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponceDto> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponceDto errorResponceDto = new ErrorResponceDto(status.value(), ex.getMessage());
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
        return ResponseEntity.status(status).body(errorResponceDto);
    }
}

