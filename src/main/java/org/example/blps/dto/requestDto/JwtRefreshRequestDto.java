package org.example.blps.dto.requestDto;

import lombok.Data;

@Data
public class JwtRefreshRequestDto {
    private String refreshToken;
}
