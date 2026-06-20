package org.example.blps.camundaTasks;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ExternalTaskSubscription("mark-courier-accepting")
public class MarkCourierAcceptingTask implements ExternalTaskHandler {

    private final OrderService orderService;

    @Autowired
    public MarkCourierAcceptingTask(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            Long courierId = task.getVariable("courierId");
            orderService.markCourierAsAccepting(courierId);
            Map<String, Object> variables = new HashMap<>();
            variables.put("email", null);
            variables.put("accepted", null);
            service.complete(task, variables);
        } catch (RuntimeException exception) {
            service.handleFailure(task, exception.getMessage(), exception.toString(), 0, 0L);
        }
    }
}
