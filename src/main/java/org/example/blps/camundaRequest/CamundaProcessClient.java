package org.example.blps.camundaRequest;
import org.example.blps.CamundaRequestProperties.CamundaVariable;
import org.example.blps.CamundaRequestProperties.ProcessStartRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

// класс отвечающий за отправку запроса к камунда
// пример запроса к камуна
// POST http://localhost:8080/engine-rest/process-definition/key/changeShiftStatusProcess/start
//Content-Type: application/json
// {
//  "variables": { // обьекты которые передаем
//    "email": {
//      "value": "courier@mail.com",
//      "type": "String"
//    }
//  },
//  "withVariablesInReturn": false
//}

@Service
public class CamundaProcessClient {

    private final RestClient camundaRestClient;

    public CamundaProcessClient(RestClient camundaRestClient) {
        this.camundaRestClient = camundaRestClient;
    }

    public void startProcess(
            String processKey,
            Map<String, CamundaVariable> variables
    ) {
        ProcessStartRequest request = new ProcessStartRequest(
                variables,
                false
        );

        camundaRestClient.post()
                .uri("/process-definition/key/{key}/start", processKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}