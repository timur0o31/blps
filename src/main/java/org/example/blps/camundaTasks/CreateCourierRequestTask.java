package org.example.blps.camundaTasks;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.entity.User;
import org.example.blps.service.CourierRequestService;
import org.example.blps.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ExternalTaskSubscription("create-courier-request")
public class CreateCourierRequestTask implements ExternalTaskHandler {

    private final CourierRequestService courierRequestService;
    private final UserService userService;

    public CreateCourierRequestTask(CourierRequestService courierRequestService, UserService userService) {
        this.courierRequestService = courierRequestService;
        this.userService = userService;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            String camundaUserId = task.getVariable("courierCamundaUserId");
            if (camundaUserId == null || camundaUserId.isBlank()) {
                camundaUserId = task.getVariable("initiatorCamundaUserId");
            }
            User courierUser = resolveUser(camundaUserId);
            Long requestId = courierRequestService.submitRequest(courierUser.getEmail());
            Map<String, Object> variables = new HashMap<>();
            variables.put("courierRequestId", requestId);
            variables.put("courierName", courierUser.getName());
            variables.put("courierSurname", courierUser.getSurname());
            variables.put("requestCreationSuccessful", true);
            variables.put("requestCreationError", "");
            service.complete(task, variables);
        } catch (AccessDeniedException | IllegalStateException exception) {
            service.complete(task, Map.of(
                    "requestCreationSuccessful", false,
                    "requestCreationError", exception.getMessage() == null
                            ? "Не удалось отправить заявку"
                            : exception.getMessage()
            ));
        } catch (RuntimeException exception) {
            service.handleFailure(task, exception.getMessage(), exception.toString(), 0, 0L);
        }
    }

    private User resolveUser(String camundaUserId) {
        if (camundaUserId == null || !camundaUserId.startsWith("user")) {
            throw new IllegalStateException("Не удалось определить курьера, отправившего заявку");
        }
        Long userId = Long.parseLong(camundaUserId.substring("user".length()));
        return userService.findById(userId);
    }
}
