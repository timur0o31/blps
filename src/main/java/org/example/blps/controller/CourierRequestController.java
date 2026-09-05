package org.example.blps.controller;


import jakarta.validation.constraints.Positive;
import org.example.blps.CamundaRequestProperties.CamundaVariable;
import org.example.blps.camundaRequest.CamundaProcessClient;
import org.example.blps.dto.responseDto.CourierApplicationsResponseDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.CourierRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@Validated
@RequestMapping("/courier-requests")
public class CourierRequestController {

    private final CourierRequestService courierRequestService;
    private final CamundaProcessClient camundaProcessClient;

    @Autowired
    public CourierRequestController(CourierRequestService courierRequestService,
                                    CamundaProcessClient camundaProcessClient){
        this.courierRequestService =courierRequestService;
        this.camundaProcessClient = camundaProcessClient;
    }

    @PreAuthorize("hasAuthority('VIEW_COURIER_APPLICATIONS')")
    @GetMapping
    public ResponseEntity<?> getRequests(@RequestParam(defaultValue = "0") String page, @RequestParam(defaultValue="10") String size,
                                         @RequestParam(required = false) @Positive Long courierId, @RequestParam(required = false) String status){
        ResponsePaginationDto<CourierApplicationsResponseDto> response = courierRequestService.getAll(page,size, courierId, status);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('SUBMIT_REQUEST')")
    @PostMapping("/submit")
    public ResponseEntity<?> submitRequest(@AuthenticationPrincipal CustomUserDetails userDetails){
        Map<String, CamundaVariable> variables = new HashMap<>();
        variables.put("courierCamundaUserId", new CamundaVariable("user" + userDetails.user().getId(), "String"));
        String processInstanceId = camundaProcessClient.startProcess("courier_account_submit", variables);
        return ResponseEntity.accepted().body(Map.of("processInstanceId", processInstanceId));
    }

    @PreAuthorize("hasAuthority('APPROVE_REQUEST')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable @Positive Long id){
        String email = userDetails.getUsername();
        courierRequestService.approveRequest(email, id);
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("hasAuthority('DECLINE_REQUEST')")
    @PatchMapping("/{id}/decline")
    public ResponseEntity<?> declineRequest(@AuthenticationPrincipal CustomUserDetails userDetails,@PathVariable @Positive Long id){
        String email = userDetails.getUsername();
        courierRequestService.declineRequest(email,id);
        return ResponseEntity.ok().build();
    }
}
