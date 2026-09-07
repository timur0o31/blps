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
            Long adminId = readAdminId(task.getVariable("adminId"));
            boolean state = readState(task.getVariable("state"));
            String changedByCamundaUserId = task.getVariable("changedByCamundaUserId");
            String changedByEmail = resolveEmailByCamundaUserId(changedByCamundaUserId);
            adminService.changeState(changedByEmail, adminId, state);
            service.complete(task);
        } catch (RuntimeException exception) {
            service.handleFailure(task, exception.getMessage(), exception.toString(), 3, 5000L);
        }
    }

    private Long readAdminId(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Некорректный id администратора: " + value, exception);
        }
    }

    private boolean readState(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if ("true".equalsIgnoreCase(String.valueOf(value))) {
            return true;
        }
        if ("false".equalsIgnoreCase(String.valueOf(value))) {
            return false;
        }
        throw new IllegalStateException("Некорректное состояние администратора: " + value);
    }

    private String resolveEmailByCamundaUserId(String camundaUserId) {
        if (camundaUserId == null || !camundaUserId.startsWith("user")) {
            throw new IllegalStateException("Не удалось определить суперпользователя");
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
