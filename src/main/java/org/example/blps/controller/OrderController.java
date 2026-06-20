package org.example.blps.controller;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.blps.CamundaRequestProperties.CamundaVariable;
import org.example.blps.camundaRequest.CamundaProcessClient;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.requestDto.OrderStatusRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    private final CamundaProcessClient camundaProcessClient;

    @Autowired
    public OrderController(OrderService orderService, CamundaProcessClient camundaProcessClient){
        this.orderService=orderService;
        this.camundaProcessClient = camundaProcessClient;
    }

    @PreAuthorize("hasAuthority('CREATE_ORDER')")
    @PostMapping
    public ResponseEntity<Void> createOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                            @RequestBody @Valid OrderRequestDto orderRequestDto) {
        Map<String, CamundaVariable> variables = new HashMap<>();
        variables.put("email", new CamundaVariable(userDetails.getUsername(), "String"));
        variables.put("content", new CamundaVariable(orderRequestDto.getContent(), "String"));
        variables.put("address", new CamundaVariable(orderRequestDto.getAddress(), "String"));
        variables.put("jwt", new CamundaVariable(bearerToken(authorizationHeader), "String"));
        Map<String, CamundaVariable> startVariables = new HashMap<>();
        startVariables.put("email", new CamundaVariable(userDetails.getUsername(), "String"));
        startVariables.put("jwt", new CamundaVariable(bearerToken(authorizationHeader), "String"));
        String processInstanceId = camundaProcessClient.startProcess("create_order_process", startVariables);
        camundaProcessClient.completeTask(processInstanceId, "Task_FillOrder", variables);
        return ResponseEntity.accepted().build();
    }

    @PreAuthorize("hasAuthority('VIEW_ORDER')")
    @GetMapping(value="/active")
    public ResponseEntity<?> getOrder(@AuthenticationPrincipal CustomUserDetails userDetails){
        OrderResponseDto responseDto = orderService.getOrder(userDetails.getUsername());
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasAuthority('UPDATE_STATUS_ORDER')")
    @PatchMapping(value = "/{id}/status")
    public ResponseEntity<OrderResponseDto> updateStatusOrder(@PathVariable Long id,
                                                               @AuthenticationPrincipal CustomUserDetails userDetails,
                                                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                               @RequestBody @Valid OrderStatusRequestDto orderStatusRequestDto) {
        orderService.ensureOrderAssignedToCourier(id, userDetails.getUsername());
        Map<String, CamundaVariable> variables = new HashMap<>();
        variables.put("nextOrderStatus", new CamundaVariable(orderStatusRequestDto.getOrderStatus().name(), "String"));
        variables.put("jwt", new CamundaVariable(bearerToken(authorizationHeader), "String"));
        camundaProcessClient.completeTask("process_order_assignment", "Task_UpdateOrderStatus", id, variables);
        return ResponseEntity.accepted().build();
    }

    @PreAuthorize("hasAuthority('CANCEL_ORDER')")
    @PatchMapping(value ="/{id}/cancel")
    public ResponseEntity<?> cancelOrderByCourier(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                   @PathVariable Long id) {
        orderService.ensureOrderAssignedToCourier(id, userDetails.getUsername());
        Map<String, CamundaVariable> variables = new HashMap<>();
        variables.put("accepted", new CamundaVariable(false, "Boolean"));
        variables.put("jwt", new CamundaVariable(bearerToken(authorizationHeader), "String"));
        camundaProcessClient.completeTask("process_order_assignment", "Task_CourierDecisionLogin", id, variables);
        return ResponseEntity.accepted().build();
    }

    @PreAuthorize("hasAuthority('ACCEPT_ORDER')")
    @PatchMapping(value = "/{id}/accept")
    public ResponseEntity<?> acceptOrderByCourier(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                   @PathVariable Long id){
        orderService.ensureOrderAssignedToCourier(id, userDetails.getUsername());
        Map<String, CamundaVariable> variables = new HashMap<>();
        variables.put("accepted", new CamundaVariable(true, "Boolean"));
        variables.put("jwt", new CamundaVariable(bearerToken(authorizationHeader), "String"));
        camundaProcessClient.completeTask("process_order_assignment", "Task_CourierDecisionLogin", id, variables);
        return ResponseEntity.accepted().build();
    }

    @PreAuthorize("hasAuthority('VIEW_STATUS_ORDER')")
    @GetMapping(value = "/{id}/status")
    public ResponseEntity<?> getStatusOrder(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponseDto status = orderService.getStatusOrder(id, userDetails.getUsername());
        log.info(status.toString());
        return ResponseEntity.ok(status);
    }

    @PreAuthorize("hasAuthority('VIEW_ORDER_HISTORY')")
    @GetMapping(value = "/history")
    public ResponseEntity<?> getOrderHistory(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam(defaultValue = "0") String page, @RequestParam(defaultValue="10") String size) {
        ResponsePaginationDto<OrderResponseDto> history = orderService.getOrderHistory(userDetails.getUsername(),page,size);
        return ResponseEntity.ok(history);
    }

    private String bearerToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }
}
