package org.example.blps.controller;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void createClient(@RequestBody UserRequestDto userDto) {
        userService.createUser(userDto);
    }

    @PostMapping("/courier")
    public void createCourier(@RequestBody UserRequestDto userDto) {
        userService.createUser(userDto);
    }

}
