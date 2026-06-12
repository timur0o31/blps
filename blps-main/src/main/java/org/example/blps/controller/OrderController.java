package org.example.blps.controller;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.blps.dto.requestDto.BitrixRequestDto;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.requestDto.OrderStatusRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.enums.OrderStatus;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    @Value("${bitrix.secret.token}")
    private String bitrixWebhookSecret;

    @Autowired
    public OrderController(OrderService orderService){
        this.orderService=orderService;
    }

    @PreAuthorize("hasAuthority('CREATE_ORDER')")
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid OrderRequestDto orderRequestDto) {
        OrderResponseDto responseDto = orderService.addOrder(userDetails.getUsername(), orderRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasAuthority('VIEW_ORDER')")
    @GetMapping(value="/active")
    public ResponseEntity<?> getOrder(@AuthenticationPrincipal CustomUserDetails userDetails){
        OrderResponseDto responseDto = orderService.getOrder(userDetails.getUsername());
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasAuthority('UPDATE_STATUS_ORDER')")
    @PatchMapping(value = "/{id}/status")
    public ResponseEntity<OrderResponseDto> updateStatusOrder(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @RequestBody @Valid OrderStatusRequestDto orderStatusRequestDto) {
        OrderResponseDto responseDto = orderService.updateOrder(id,orderStatusRequestDto, userDetails.getUsername());
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasAuthority('CANCEL_ORDER')")
    @PatchMapping(value ="/{id}/cancel")
    public ResponseEntity<?> cancelOrderByCourier(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        orderService.cancelOrderById(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ACCEPT_ORDER')")
    @PatchMapping(value = "/{id}/accept")
    public ResponseEntity<?> acceptOrderByCourier(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id){
        orderService.acceptOrderByCourierId(id,userDetails.getUsername());
        return ResponseEntity.ok().build();
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


    @PostMapping("/bitrix/update")
    public ResponseEntity<?> updateOrderFromBitrix(@RequestHeader("bitrix.secret.token") String token, @RequestBody BitrixRequestDto dto) {
        if (!bitrixWebhookSecret.equals(token)) {
            throw  new AccessDeniedException("Неверный Bitrix token атата!");
        }
        Long backendId = dto.getBackendId();
        OrderStatus status = dto.getStatus();
        orderService.updateOrderFromBitrix(backendId, status);
        return ResponseEntity.ok().build();
    }
}
