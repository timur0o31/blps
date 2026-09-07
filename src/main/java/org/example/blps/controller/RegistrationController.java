package org.example.blps.controller;
import jakarta.validation.Valid;
import org.example.blps.CamundaRequestProperties.CamundaVariable;
import org.example.blps.camundaRequest.CamundaProcessClient;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/registration")
public class RegistrationController {

    private final CamundaProcessClient camundaProcessClient;

    @Autowired
    public RegistrationController(CamundaProcessClient camundaProcessClient) {
        this.camundaProcessClient = camundaProcessClient;
    }

    @PostMapping("/client")
    public ResponseEntity<?> createClient(@RequestBody @Valid UserRequestDto userDto) {
        startRegistrationProcess("client_registration_process", "Activity_0yc84sa", userDto);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/courier")
    public ResponseEntity<?> createCourier(@RequestBody @Valid UserRequestDto userDto) {
        startRegistrationProcess("courier_registration_process", "Activity_1w1d3vd", userDto);
        return ResponseEntity.accepted().build();
    }

    private void startRegistrationProcess(String processKey, String taskDefinitionKey, UserRequestDto userDto) {
        Map<String, CamundaVariable> variables = registrationVariables(userDto);
        String processInstanceId = camundaProcessClient.startProcess(processKey, variables);
        camundaProcessClient.completeTask(processInstanceId, taskDefinitionKey, Map.of());
    }

    private Map<String, CamundaVariable> registrationVariables(UserRequestDto userDto) {
        Map<String, CamundaVariable> variables = new HashMap<>();
        variables.put("name", new CamundaVariable(userDto.getName(), "String"));
        variables.put("surname", new CamundaVariable(userDto.getSurname(), "String"));
        variables.put("email", new CamundaVariable(userDto.getEmail(), "String"));
        variables.put("password", new CamundaVariable(userDto.getPassword(), "String"));
        variables.put("phoneNumber", new CamundaVariable(userDto.getPhoneNumber(), "String"));
        return variables;
    }
}
