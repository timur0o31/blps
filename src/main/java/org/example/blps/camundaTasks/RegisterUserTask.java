package org.example.blps.camundaTasks;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ExternalTaskSubscription("register-user")
public class RegisterUserTask implements ExternalTaskHandler {

    private final UserService userService;
    private final Validator validator;

    public RegisterUserTask(UserService userService, Validator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            UserRequestDto user = new UserRequestDto();
            user.setName(task.getVariable("name"));
            user.setSurname(task.getVariable("surname"));
            user.setEmail(task.getVariable("email"));
            user.setPassword(task.getVariable("password"));
            user.setPhoneNumber(task.getVariable("phoneNumber"));

            validate(user);

            String registrationRole = task.getVariable("registrationRole");
            if ("CLIENT".equals(registrationRole)) {
                userService.createClient(user);
            } else if ("COURIER".equals(registrationRole)) {
                userService.createCourier(user);
            } else {
                throw new IllegalStateException("Неизвестный тип регистрируемого пользователя");
            }

            service.complete(task, registrationResult(true, ""));
        } catch (DataIntegrityViolationException | IllegalArgumentException exception) {
            service.complete(task, registrationResult(false, exception.getMessage()));
        } catch (Exception exception) {
            service.handleFailure(task, exception.getMessage(), exception.toString(), 0, 0L);
        }
    }

    private Map<String, Object> registrationResult(boolean successful, String error) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("registrationSuccessful", successful);
        variables.put("registrationError", error == null ? "Ошибка регистрации" : error);
        return variables;
    }

    private void validate(UserRequestDto user) {
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(message);
        }
    }
}
