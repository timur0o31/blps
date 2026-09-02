package org.example.blps.camundaTasks;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.entity.User;
import org.example.blps.service.OrderService;
import org.example.blps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ExternalTaskSubscription("save-order")
public class SaveOrderTask implements ExternalTaskHandler {

    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public SaveOrderTask(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            String email = resolveClientEmail(task);
            String content = task.getVariable("content");
            String address = task.getVariable("address");

            OrderResponseDto order = orderService.addOrder(email, new OrderRequestDto(content, address));
            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", order.id());
            service.complete(task, variables);
        } catch (RuntimeException exception) {
            service.handleFailure(task, exception.getMessage(), exception.toString(), 0, 0L);
        }

    }

    private String resolveClientEmail(ExternalTask task) {
        String clientCamundaUserId = task.getVariable("clientCamundaUserId");
        if (clientCamundaUserId != null && clientCamundaUserId.startsWith("user")) {
            Long userId = Long.parseLong(clientCamundaUserId.substring("user".length()));
            User user = userService.findById(userId);
            return user.getEmail();
        }

        String email = task.getVariable("email");
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalStateException("Не удалось определить клиента для заказа");
        }
        return email;
    }
}
