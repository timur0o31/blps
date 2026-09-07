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

import java.util.Map;

@Component
@ExternalTaskSubscription("decline-courier-request")
public class CourierDeclineRequestTask implements ExternalTaskHandler {

    private final CourierRequestService courierRequestService;
    private final UserService userService;

    public CourierDeclineRequestTask(CourierRequestService courierRequestService, UserService userService) {
        this.courierRequestService = courierRequestService;
        this.userService = userService;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            Long requestId = task.getVariable("courierRequestId");
            String reviewerCamundaUserId = task.getVariable("reviewerCamundaUserId");
            String reviewerEmail = resolveEmailByCamundaUserId(reviewerCamundaUserId);
            courierRequestService.declineRequest(reviewerEmail, requestId);
            service.complete(task, Map.of("requestActionSuccessful", true,
                    "requestActionError", ""
            ));
        } catch (AccessDeniedException | IllegalStateException exception) {
            service.complete(task, Map.of(
                    "requestActionSuccessful", false,
                    "requestActionError", exception.getMessage() == null
                            ? "Не удалось отклонить заявку курьера"
                            : exception.getMessage()
            ));
        } catch (RuntimeException exception) {
            service.handleFailure(task, exception.getMessage(), exception.toString(), 0, 0L);
        }
    }

    private String resolveEmailByCamundaUserId(String camundaUserId) {
        Long userId = Long.parseLong(camundaUserId.substring("user".length()));
        User user = userService.findById(userId);
        return user.getEmail();
    }
}
