package org.example.blps.camundaTasks;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.entity.User;
import org.example.blps.service.CourierService;
import org.example.blps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ExternalTaskSubscription("courier-status-toggle")
public class ChangeShiftStatusTask implements ExternalTaskHandler {

    private final CourierService courierService;
    private final UserService userService;

    @Autowired
    public ChangeShiftStatusTask(CourierService courierService, UserService userService) {
        this.courierService = courierService;
        this.userService = userService;
    }

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            String camundaUserId = externalTask.getVariable("courierCamundaUserId");
            String email = resolveEmailByCamundaUserId(camundaUserId);
            courierService.toggleCourierShiftStatus(email);
            externalTaskService.complete(externalTask, Map.of(
                    "shiftChangeSuccessful", true,
                    "shiftError", ""
            ));
        } catch (AccessDeniedException | IllegalStateException exception) {
            externalTaskService.complete(externalTask, Map.of(
                    "shiftChangeSuccessful", false,
                    "shiftError", exception.getMessage() == null ? "Не удалось изменить статус смены" : exception.getMessage()
            ));
        } catch (RuntimeException exception) {
            externalTaskService.handleFailure(
                    externalTask, exception.getMessage(), exception.toString(), 0, 0L
            );
        }
    }

    private String resolveEmailByCamundaUserId(String camundaUserId) {
        if (camundaUserId == null || !camundaUserId.startsWith("user")) {
            throw new IllegalStateException("Не удалось определить пользователя Camunda");
        }
        Long userId = Long.parseLong(camundaUserId.substring("user".length()));
        User user = userService.findById(userId);
        return user.getEmail();
    }
}
