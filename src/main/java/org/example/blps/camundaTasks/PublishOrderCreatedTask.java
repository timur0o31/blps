package org.example.blps.camundaTasks;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.CamundaRequestProperties.CamundaVariable;
import org.example.blps.camundaRequest.CamundaProcessClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ExternalTaskSubscription("publish-order-created")
public class PublishOrderCreatedTask implements ExternalTaskHandler {

    private final CamundaProcessClient camundaProcessClient;

    public PublishOrderCreatedTask(CamundaProcessClient camundaProcessClient) {
        this.camundaProcessClient = camundaProcessClient;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        Long orderId = task.getVariable("orderId");

        Map<String, CamundaVariable> variables = new HashMap<>();
        variables.put("orderId", new CamundaVariable(orderId, "Long"));
        camundaProcessClient.correlateMessage("order-created", variables);
        service.complete(task);
    }
}
