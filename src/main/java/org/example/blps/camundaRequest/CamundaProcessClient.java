package org.example.blps.camundaRequest;
import org.example.blps.CamundaRequestProperties.CamundaVariable;
import org.example.blps.CamundaRequestProperties.ProcessStartRequest;
import org.example.blps.dto.responseDto.ProcessResponseDto;
import org.example.blps.dto.responseDto.TaskResponseDto;
import org.example.blps.dto.responseDto.VariableQueryDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.HashMap;
import java.util.Map;

@Service
public class CamundaProcessClient {

    private final RestClient camundaRestClient;

    public CamundaProcessClient(RestClient camundaRestClient) {
        this.camundaRestClient = camundaRestClient;
    }

    public String startProcess(String processKey, Map<String, CamundaVariable> variables) {
        ProcessStartRequest request = new ProcessStartRequest(variables, false);
        ProcessResponseDto response = camundaRestClient.post()
                .uri("/process-definition/key/{key}/start", processKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ProcessResponseDto.class);
        if (response == null || response.id() == null) {
            throw new IllegalStateException("Camunda не вернула id экземпляра процесса: " + processKey);
        }
        return response.id();
    }

    public void completeTask(String processInstanceId, String taskDefinitionKey,
            Map<String, CamundaVariable> variables
    ) {
        Map<String, Object> query = new HashMap<>();
        query.put("processInstanceId", processInstanceId);
        query.put("taskDefinitionKey", taskDefinitionKey);
        query.put("active", true);
        TaskResponseDto[] tasks = camundaRestClient.post()
                .uri("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .body(query)
                .retrieve()
                .body(TaskResponseDto[].class);
        completeFirstTask(tasks, taskDefinitionKey, variables);
    }


    public void correlateMessage(String messageName, Map<String, CamundaVariable> variables) {
        Map<String, Object> request = new HashMap<>();
        request.put("messageName", messageName);
        request.put("processVariables", variables);
        request.put("resultEnabled", false);

        camundaRestClient.post()
                .uri("/message")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void completeTask(String processDefinitionKey, String taskDefinitionKey, Long orderId, Map<String, CamundaVariable> variables) {
        VariableQueryDto[] variableQueries = {new VariableQueryDto("orderId", "eq", orderId)};
        Map<String, Object> query = new HashMap<>();
        query.put("processDefinitionKey", processDefinitionKey);
        query.put("taskDefinitionKey", taskDefinitionKey);
        query.put("active", true);
        query.put("processVariables", variableQueries);
        TaskResponseDto[] tasks = camundaRestClient.post()
                .uri("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .body(query)
                .retrieve()
                .body(TaskResponseDto[].class);
        completeFirstTask(tasks, taskDefinitionKey, variables);
    }

    private void completeFirstTask(TaskResponseDto[] tasks, String taskDefinitionKey, Map<String, CamundaVariable> variables) {
        if (tasks == null || tasks.length == 0) {
            throw new IllegalStateException("Активная задача Camunda не найдена: " + taskDefinitionKey);
        }
        if (tasks[0] == null || tasks[0].id() == null) {
            throw new IllegalStateException("Camunda вернула задачу без id: " + taskDefinitionKey);
        }
        String taskId = tasks[0].id();
        Map<String, Object> request = new HashMap<>();
        request.put("variables", variables);
        camundaRestClient.post()
                .uri("/task/{id}/complete", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
