package org.example.blps.camundaTasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.entity.Admin;
import org.example.blps.entity.User;
import org.example.blps.enums.Role;
import org.example.blps.repository.AdminRepository;
import org.example.blps.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ExternalTaskSubscription("load-admin-options")
public class LoadAdminOptionsTask implements ExternalTaskHandler {

    private final AdminRepository adminRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public LoadAdminOptionsTask(AdminRepository adminRepository, UserService userService, ObjectMapper objectMapper) {
        this.adminRepository = adminRepository;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            User changedBy = resolveUser(task.getVariable("changedByCamundaUserId"));
            if (changedBy.getRole() != Role.ADMIN || !changedBy.isSuperUser()) {
                throw new AccessDeniedException("Управлять администраторами может только суперпользователь");
            }

            List<AdminOption> options = new ArrayList<>();
            for (Admin admin : adminRepository.findAll()) {
                User user = userService.findById(admin.getUserId());
                if (!user.isSuperUser()) {
                    options.add(new AdminOption(admin.getId(), user.getName(), user.getSurname(),
                            user.getEmail(), admin.isAccountState()));
                }
            }

            service.complete(task, Map.of("adminOptionsJson", objectMapper.writeValueAsString(options)));
        } catch (Exception exception) {
            service.handleFailure(task, exception.getMessage(), exception.toString(), 3, 5000L);
        }
    }

    private User resolveUser(String camundaUserId) {
        if (camundaUserId == null || !camundaUserId.startsWith("user")) {
            throw new IllegalStateException("Не удалось определить суперпользователя");
        }
        try {
            return userService.findById(Long.parseLong(camundaUserId.substring("user".length())));
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Некорректный Camunda user id: " + camundaUserId, exception);
        }
    }

    private record AdminOption(Long id, String name, String surname, String email, boolean enabled) {
    }
}
