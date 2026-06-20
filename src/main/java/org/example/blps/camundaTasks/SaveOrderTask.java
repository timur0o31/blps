package org.example.blps.camundaTasks;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ExternalTaskSubscription("save-order")
public class SaveOrderTask implements ExternalTaskHandler {

    private final OrderService orderService;

    @Autowired
    public SaveOrderTask(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            String email = task.getVariable("email");
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
}
