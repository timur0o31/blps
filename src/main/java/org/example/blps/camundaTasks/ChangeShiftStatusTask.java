package org.example.blps.camundaTasks;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ExternalTaskSubscription("courier-status-toggle")
public class ChangeShiftStatusTask implements ExternalTaskHandler {

    private CourierService courierService;

    @Autowired
    public ChangeShiftStatusTask(CourierService courierService) {
        this.courierService = courierService;
    }

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            String email = externalTask.getVariable("email");
            courierService.toggleCourierShiftStatus(email);
            externalTaskService.complete(externalTask);
        } catch (RuntimeException exception) {
            externalTaskService.handleFailure(
                    externalTask, exception.getMessage(), exception.toString(), 0, 0L
            );
        }
    }
}
