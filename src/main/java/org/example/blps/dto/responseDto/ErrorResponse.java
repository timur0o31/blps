package org.example.blps.dto.responseDto;
import java.util.Map;

public class ErrorResponse {
    private String message;
    private int status;
    private Map<String, String> errors;

    public ErrorResponse(String message, int status, Map<String, String> errors) {
        this.message = message;
        this.status = status;
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}