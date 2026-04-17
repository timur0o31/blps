package org.example.blps.dto.responseDto;
import java.time.LocalDateTime;

public class ErrorResponceDto {
    private final int status;
    private final String message;
    private final LocalDateTime timestamp;

    public ErrorResponceDto(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp=LocalDateTime.now();
    }

}
