package org.example.blps.camundaTasks;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ExternalTaskSubscription("assign-courier-to-order")
public class AssignCourierToOrderTask implements ExternalTaskHandler {

    private final OrderService orderService;

    @Autowired
    public AssignCourierToOrderTask(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        Long orderId = task.getVariable("orderId");
        Long courierId = task.getVariable("courierId");
        orderService.assignCourierToOrder(orderId, courierId);
        service.complete(task);
    }
}
