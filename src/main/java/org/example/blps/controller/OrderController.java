package org.example.blps.controller;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.requestDto.OrderStatusRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @PostMapping(value = "/order")
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto orderRequestDto) {
        OrderResponseDto responseDto = orderService.addOrder(orderRequestDto);
        return ResponseEntity.ok(responseDto);
    }
    @PutMapping(value = "/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrder(@PathVariable Long id,@RequestBody OrderStatusRequestDto orderStatusRequestDto) {
        OrderResponseDto responseDto = orderService.updateOrder(id,orderStatusRequestDto);
        return ResponseEntity.ok(responseDto);
    }
    @PatchMapping(value ="/{id}/cancel-order")
    public ResponseEntity<?> cancelOrderByCourier(@PathVariable Long id,@RequestParam Long courierId, @RequestBody OrderRequestDto orderRequestDto) {
        orderService.cancelOrderByCourierId(id, courierId);
        return ResponseEntity.ok().build();
    }
    @PatchMapping(value = "/{id}/accept-order")
    public ResponseEntity<?> acceptOrderByCourier(@PathVariable Long id, @RequestParam Long courierId, @RequestBody OrderRequestDto orderRequestDto){
        orderService.acceptOrderByCourierId(id,courierId);
        return ResponseEntity.ok().build();
    }
//    @GetMapping
//    public ResponseEntity<?> getOrders() {}

}
