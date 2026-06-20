package org.example.blps.camundaTasks;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.enums.OrderStatus;
import org.example.blps.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ExternalTaskSubscription("update-order-delivery-status")
public class UpdateOrderDeliveryStatusTask implements ExternalTaskHandler {

    private final OrderService orderService;

    @Autowired
    public UpdateOrderDeliveryStatusTask(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            Long orderId = task.getVariable("orderId");
            Long courierId = task.getVariable("courierId");
            OrderStatus nextStatus = OrderStatus.valueOf(task.getVariable("nextOrderStatus"));
            boolean delivered = orderService.updateOrder(orderId, courierId, nextStatus);
            Map<String, Object> variables = new HashMap<>();
            variables.put("orderDelivered", delivered);
            variables.put("statusValidationError", null);
            service.complete(task, variables);
        } catch (RuntimeException exception) {
            service.handleFailure(task, exception.getMessage(), exception.toString(), 0, 0L);
        }
    }
}
