package org.example.blps.service;
import org.example.blps.dto.requestDto.OrderRequestDto;
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

    public void addOrder(OrderRequestDto orderRequestDto) {
        orderRepository.save(orderMapper.fromDtoToEntity(orderRequestDto));
    }
}
