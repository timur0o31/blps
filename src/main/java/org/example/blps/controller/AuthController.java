package org.example.blps.controller;
import org.example.blps.dto.responseDto.JwtAuthificationResponceDto;
import org.example.blps.dto.requestDto.UserCredentialsRequestDto;
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
    public ResponseEntity<JwtAuthificationResponceDto> singIn(@RequestBody UserCredentialsRequestDto userCredentialsDto) {
        JwtAuthificationResponceDto jwtAuthenticationDto = userService.signIn(userCredentialsDto);
        return ResponseEntity.ok(jwtAuthenticationDto);
    }
}