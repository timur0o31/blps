package org.example.blps.dto.requestDto;
import lombok.Data;

// Реквизиты для входа
@Data
public class UserCredentialsRequestDto {
    private String email;
    private String password;
}
