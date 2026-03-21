package org.example.blps.dto.requestDto;

import lombok.Data;

@Data
public class JwtAuthificationRequestDto {
    private String token;
    private String refreshToken;
}
