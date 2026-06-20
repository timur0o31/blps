package org.example.blps.camundaTasks;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.example.blps.security.jwt.JwtService;
import org.example.blps.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ExternalTaskSubscription("courier-status-toggle")
public class ChangeShiftStatusTask implements ExternalTaskHandler {

    private CourierService courierService;
    private final JwtService jwtService;

    @Autowired
    public ChangeShiftStatusTask(CourierService courierService, JwtService jwtService) {
        this.courierService = courierService;
        this.jwtService = jwtService;
    }

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            String jwt = externalTask.getVariable("jwt");
            if (jwt == null || !jwtService.validateJwtToken(jwt)) {
                throw new IllegalStateException("JWT отсутствует или недействителен");
            }
            String email = jwtService.getEmailFromToken(jwt);
            courierService.toggleCourierShiftStatus(email);
            externalTaskService.complete(externalTask);
        } catch (RuntimeException exception) {
            externalTaskService.handleFailure(
                    externalTask, exception.getMessage(), exception.toString(), 0, 0L
            );
        }
    }
}
