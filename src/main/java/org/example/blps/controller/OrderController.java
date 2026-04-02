package org.example.blps.controller;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.requestDto.OrderStatusRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(value = "/order")
    public ResponseEntity<OrderResponseDto> createOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @RequestBody OrderRequestDto orderRequestDto) {
        OrderResponseDto responseDto = orderService.addOrder(userDetails.getUsername(), orderRequestDto);
        return ResponseEntity.ok(responseDto);
    }
    @GetMapping(value="/active")
    public ResponseEntity<?> getOrder(@AuthenticationPrincipal CustomUserDetails userDetails){
        OrderResponseDto responseDto = orderService.getOrder(userDetails.getUsername());
        if (responseDto == null){
            return ResponseEntity.ok("В данный момент нет назначенных заказов");
        }
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping(value = "/{id}/status")
    public ResponseEntity<OrderResponseDto> updateStatusOrder(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @RequestBody OrderStatusRequestDto orderStatusRequestDto) {
        OrderResponseDto responseDto = orderService.updateOrder(id,orderStatusRequestDto, userDetails.getUsername());
        return ResponseEntity.ok(responseDto);
    }
    @PatchMapping(value ="/{id}/cancel-order")
    public ResponseEntity<?> cancelOrderByCourier(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        orderService.cancelOrderByCourierId(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    @PatchMapping(value = "/{id}/accept-order")
    public ResponseEntity<?> acceptOrderByCourier(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id){
        orderService.acceptOrderByCourierId(id,userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

//    @GetMapping
//    public ResponseEntity<?> getOrders() {}

}
