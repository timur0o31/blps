package org.example.blps.service;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.requestDto.OrderStatusRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.entity.Order;
import org.example.blps.mapper.OrderMapper;
import org.example.blps.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    public OrderResponseDto addOrder(OrderRequestDto orderRequestDto) {
        return orderMapper.fromEntityToDto(orderRepository.save(orderMapper.fromDtoToEntity(orderRequestDto)));
    }
    public OrderResponseDto updateOrder(Long id, OrderStatusRequestDto orderRequestDto){
        Order order = orderRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Order not found"));
        order.setStatus(orderRequestDto.getOrderStatus());
        return orderMapper.fromEntityToDto(orderRepository.save(order));
    }
}
