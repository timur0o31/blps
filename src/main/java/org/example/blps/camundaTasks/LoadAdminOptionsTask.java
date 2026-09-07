package org.example.blps.camundaTasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.CamundaResponceProperties.CamundaAdminResponce;
import org.example.blps.entity.Admin;
import org.example.blps.entity.User;
import org.example.blps.enums.Role;
import org.example.blps.repository.AdminRepository;
import org.example.blps.service.AdminService;
import org.example.blps.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ExternalTaskSubscription("load-admin-options")
public class LoadAdminOptionsTask implements ExternalTaskHandler {

    private final AdminService adminService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public LoadAdminOptionsTask(AdminService adminService, UserService userService, ObjectMapper objectMapper) {
        this.adminService = adminService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            List<CamundaAdminResponce> options = adminService.getAdminChoise();
            service.complete(task, Map.of("adminOptionsJson", objectMapper.writeValueAsString(options)));
        } catch (Exception exception) {
            service.handleFailure(task, exception.getMessage(), exception.toString(), 0, 0L);
        }
    }
}
