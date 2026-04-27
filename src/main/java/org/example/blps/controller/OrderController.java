package org.example.blps.controller;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.requestDto.OrderStatusRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    public OrderController(OrderService orderService){
        this.orderService=orderService;
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid OrderRequestDto orderRequestDto) {
        OrderResponseDto responseDto = orderService.addOrder(userDetails.getUsername(), orderRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('COURIER')")
    @GetMapping(value="/active")
    public ResponseEntity<?> getOrder(@AuthenticationPrincipal CustomUserDetails userDetails){
        OrderResponseDto responseDto = orderService.getOrder(userDetails.getUsername());
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('COURIER')")
    @PatchMapping(value = "/{id}/status")
    public ResponseEntity<OrderResponseDto> updateStatusOrder(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @RequestBody @Valid OrderStatusRequestDto orderStatusRequestDto) {
        OrderResponseDto responseDto = orderService.updateOrder(id,orderStatusRequestDto, userDetails.getUsername());
        return ResponseEntity.ok(responseDto);
    }
    @PreAuthorize("hasRole('COURIER')")
    @PatchMapping(value ="/{id}/cancel")
    public ResponseEntity<?> cancelOrderByCourier(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        orderService.cancelOrderById(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("hasRole('COURIER')")
    @PatchMapping(value = "/{id}/accept")
    public ResponseEntity<?> acceptOrderByCourier(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id){
        orderService.acceptOrderByCourierId(id,userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping(value = "/{id}/status")
    public ResponseEntity<?> getStatusOrder(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponseDto status = orderService.getStatusOrder(id, userDetails.getUsername());
        log.info(status.toString());
        return ResponseEntity.ok(status);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping(value = "/history")
    public ResponseEntity<?> getOrderHistory(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam(defaultValue = "0") String page, @RequestParam(defaultValue="10") String size) {
        ResponsePaginationDto history = orderService.getOrderHistory(userDetails.getUsername(),page,size);
        return ResponseEntity.ok(history);
    }
}
