package org.example.blps.dto.requestDto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRequestDto {
    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String surname;

    @NotBlank
    @Email
    @Pattern(regexp = "(?i)^[A-Za-z0-9._%+-]+@(yandex\\\\.ru|gmail\\\\.com|mail\\\\.ru)$")
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(max = 255)
    private String password;

    @NotNull
    @Size(max = 20)
    @Pattern(regexp = "^(\\+7|8)9\\d{9}$")
    private String phone;
    private String phoneNumber;
}


