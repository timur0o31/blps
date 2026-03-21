package org.example.blps.dto.requestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRequestDto {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String phoneNumber;
}
