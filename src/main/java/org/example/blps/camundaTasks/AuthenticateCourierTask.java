package org.example.blps.camundaTasks;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.dto.requestDto.UserCredentialsRequestDto;
import org.example.blps.dto.responseDto.JwtAuthificationResponceDto;
import org.example.blps.security.jwt.JwtService;
import org.example.blps.service.UserService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ExternalTaskSubscription("authenticate-user")
public class AuthenticateCourierTask implements ExternalTaskHandler {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthenticateCourierTask(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            String jwt = task.getVariable("jwt");
            String email;
            if (jwt != null && !jwt.isBlank() && jwtService.validateJwtToken(jwt)) {
                email = jwtService.getEmailFromToken(jwt);
            } else {
                email = task.getVariable("email");
                String password = task.getVariable("password");
                if (email == null || email.isBlank() || password == null || password.isBlank()) {
                    throw new IllegalStateException("JWT недействителен, email или пароль не указаны");
                }
                UserCredentialsRequestDto credentials = new UserCredentialsRequestDto();
                credentials.setEmail(email);
                credentials.setPassword(password);
                JwtAuthificationResponceDto authentication = userService.signIn(credentials);
                jwt = authentication.getToken();
            }
            Map<String, Object> variables = new HashMap<>();
            variables.put("email", email);
            variables.put("jwt", jwt);
            service.complete(task, variables);
        } catch (Exception exception) {
            service.handleFailure(task, exception.getMessage(), exception.toString(), 0, 0L);
        }
    }
}
