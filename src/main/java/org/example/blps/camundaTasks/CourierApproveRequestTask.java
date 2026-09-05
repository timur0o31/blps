package org.example.blps.camundaTasks;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.entity.User;
import org.example.blps.service.CourierRequestService;
import org.example.blps.service.UserService;
import org.springframework.stereotype.Component;

@Component
@ExternalTaskSubscription("approve-courier-request")
public class CourierApproveRequestTask implements ExternalTaskHandler {

    private final CourierRequestService courierRequestService;
    private final UserService userService;

    public CourierApproveRequestTask(CourierRequestService courierRequestService, UserService userService) {
        this.courierRequestService = courierRequestService;
        this.userService = userService;
    }
    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            Long requestId = task.getVariable("courierRequestId");
            String reviewerCamundaUserId = task.getVariable("reviewerCamundaUserId");
            String reviewerEmail = resolveEmailByCamundaUserId(reviewerCamundaUserId);
            courierRequestService.approveRequestFromProcess(reviewerEmail, requestId);
            service.complete(task);
        } catch (RuntimeException exception) {
            service.handleFailure(task, exception.getMessage(), exception.toString(),
                    3, 5000L);
        }
    }

    private String resolveEmailByCamundaUserId(String camundaUserId) {
        if (camundaUserId == null || !camundaUserId.startsWith("user")) {
            throw new IllegalStateException("Не удалось определить администратора, рассмотревшего заявку");
        }
        try {
            Long userId = Long.parseLong(camundaUserId.substring("user".length()));
            User user = userService.findById(userId);
            return user.getEmail();
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Некорректный Camunda user id: " + camundaUserId, exception);
        }
    }
}
