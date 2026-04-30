package org.example.blps.controller;
import jakarta.validation.Valid;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/registration")
public class RegistrationController {

    private final UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/client")
    public ResponseEntity<?> createClient(@RequestBody UserRequestDto userDto) throws IOException {
        userService.createClient(userDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/courier")
    public ResponseEntity<?> createCourier(@RequestBody @Valid UserRequestDto userDto) throws DataIntegrityViolationException, IOException {
        userService.createCourier(userDto);
        return ResponseEntity.ok().build();
    }
}
