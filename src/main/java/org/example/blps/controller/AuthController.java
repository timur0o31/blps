package org.example.blps.controller;
import org.example.blps.dto.requestDto.JwtAuthificationRequestDto;
import org.example.blps.dto.requestDto.JwtRefreshRequestDto;
import org.example.blps.dto.requestDto.UserCredetionalDto;
import org.example.blps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sing-in")
    public ResponseEntity<JwtAuthificationRequestDto> singIn(@RequestBody UserCredetionalDto userCredentialsDto) {
        JwtAuthificationRequestDto jwtAuthenticationDto = userService.signIn(userCredentialsDto);
        return ResponseEntity.ok(jwtAuthenticationDto);
    }

    @PostMapping("/refresh")
    public JwtAuthificationRequestDto refresh(@RequestBody JwtRefreshRequestDto refreshTokenDto) throws Exception {
        return userService.refreshToken(refreshTokenDto);
    }
}