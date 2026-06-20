package org.example.blps.camundaTasks;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ExternalTaskSubscription("expire-order-assignment")
public class ExpireAssignmentTask implements ExternalTaskHandler {

    private final OrderService orderService;

    @Autowired
    public ExpireAssignmentTask(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        Long attemptId = task.getVariable("attemptId");
        orderService.expireAssignment(attemptId);
        service.complete(task);
    }
}
