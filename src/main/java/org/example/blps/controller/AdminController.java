package org.example.blps.controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.example.blps.CamundaRequestProperties.CamundaVariable;
import org.example.blps.annotations.isApprovedAdmin;
import org.example.blps.camundaRequest.CamundaProcessClient;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.entity.Admin;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admins")
@Validated
public class AdminController {
    private final AdminService adminService;
    private final CamundaProcessClient camundaProcessClient;

    public AdminController(AdminService adminService, CamundaProcessClient camundaProcessClient) {
        this.adminService = adminService;
        this.camundaProcessClient = camundaProcessClient;
    }

    @PatchMapping("/{id}/change-state")
    @PreAuthorize("hasAuthority('CHANGE_STATE')")
    public ResponseEntity<?> changeState(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable @Positive Long id, @RequestParam boolean state) {
        String email = userDetails.getUsername();
        adminService.changeState(email, id, state);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    @isApprovedAdmin
    public ResponseEntity<?> createAdmin(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @RequestBody @Valid UserRequestDto userRequestDto) {
        Map<String, CamundaVariable> variables = new HashMap<>();
        variables.put("adminCreatorCamundaUserId", new CamundaVariable("user" + userDetails.user().getId(), "String"));
        variables.put("name", new CamundaVariable(userRequestDto.getName(), "String"));
        variables.put("surname", new CamundaVariable(userRequestDto.getSurname(), "String"));
        variables.put("email", new CamundaVariable(userRequestDto.getEmail(), "String"));
        variables.put("password", new CamundaVariable(userRequestDto.getPassword(), "String"));
        variables.put("phoneNumber", new CamundaVariable(userRequestDto.getPhoneNumber(), "String"));

        Map<String, CamundaVariable> startVariables = new HashMap<>();
        startVariables.put("adminCreatorCamundaUserId", variables.get("adminCreatorCamundaUserId"));

        String processInstanceId = camundaProcessClient.startProcess("admin_registration_process", startVariables);
        camundaProcessClient.completeTask(processInstanceId, "Task_FillAdminRegistration", variables);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ADMINS')")
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "0") String page,
                                    @RequestParam(defaultValue = "10") String size) {
        ResponsePaginationDto<Admin> response = adminService.getAll(page, size);
        return ResponseEntity.ok(response);
    }
}
