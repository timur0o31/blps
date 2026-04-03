package org.example.blps.controller;
import jakarta.validation.Valid;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/registration")
public class RegistrationController {

    private final UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/client")
    public ResponseEntity<String> createClient(@RequestBody UserRequestDto userDto) {
        userService.createClient(userDto);
        return ResponseEntity.ok("Регистрация клиента произошла успешно!");
    }

    @PostMapping("/courier")
    public ResponseEntity<String> createCourier(@RequestBody @Valid UserRequestDto userDto) throws DataIntegrityViolationException {
        userService.createCourier(userDto);
        return ResponseEntity.ok("Регистрация курьера произошла успешно!");
    }
}
