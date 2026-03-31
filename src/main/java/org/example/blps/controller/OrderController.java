package org.example.blps.controller;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @PostMapping(value = "/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestDto orderRequestDto) {
        orderService.addOrder(orderRequestDto);
        return ResponseEntity.ok().build();
    }

}
