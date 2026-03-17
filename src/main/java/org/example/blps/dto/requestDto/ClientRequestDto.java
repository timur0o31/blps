package org.example.blps.dto.requestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ClientRequestDto {
    private String name;
    private String surname;
    private String phoneNumber;
}
