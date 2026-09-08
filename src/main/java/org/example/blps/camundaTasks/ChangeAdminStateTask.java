package org.example.blps.camundaTasks;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.entity.User;
import org.example.blps.service.AdminService;
import org.example.blps.service.UserService;
import org.springframework.stereotype.Component;

@Component
@ExternalTaskSubscription("change-admin-state")
public class ChangeAdminStateTask implements ExternalTaskHandler {

    private final AdminService adminService;
    private final UserService userService;

    public ChangeAdminStateTask(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            Long adminId =  task.getVariable("adminId");
            boolean state = task.getVariable("state");
            String changedByCamundaUserId = task.getVariable("changedByCamundaUserId");
            String changedByEmail = resolveEmailByCamundaUserId(changedByCamundaUserId);
            adminService.changeState(changedByEmail, adminId, state);
            service.complete(task);
        } catch (RuntimeException exception) {
            service.handleFailure(task, exception.getMessage(), exception.toString(), 0, 0L);
        }
    }

    private String resolveEmailByCamundaUserId(String camundaUserId) {
        Long userId = Long.parseLong(camundaUserId.substring("user".length()));
        return userService.findById(userId).getEmail();
    }

}
