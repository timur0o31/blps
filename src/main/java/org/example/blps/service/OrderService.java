package org.example.blps.service;
import jakarta.transaction.Transactional;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.requestDto.OrderStatusRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.mapper.OrderMapper;
import org.example.blps.repository.CourierRepository;
import org.example.blps.repository.OrderRepository;
import org.example.blps.status.CourierStatus;
import org.example.blps.status.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CourierRepository courierRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, CourierRepository courierRepository) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.courierRepository = courierRepository;
    }

    @Transactional
    public OrderResponseDto addOrder(OrderRequestDto orderRequestDto) {
        Order newOrder = orderMapper.fromDtoToEntity(orderRequestDto);
        Courier courier = courierRepository.findFirstByStatus(CourierStatus.ONLINE)
                .orElseThrow(() -> new RuntimeException("Все курьеры заняты"));
        newOrder.setCourier(courier);
        newOrder.setStatus(OrderStatus.PENDING);
        newOrder.setAssigmentAt(LocalDateTime.now());
        courier.setStatus(CourierStatus.BUSY);
        return orderMapper.fromEntityToDto(orderRepository.save(newOrder));
    }

    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderStatusRequestDto orderRequestDto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(orderRequestDto.getOrderStatus());
        return orderMapper.fromEntityToDto(orderRepository.save(order));

    }

    @Transactional
    public void cancelOrderByCourierId(Long orderId, Long courierId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Заказ не найден"));
        courierRepository.findById(courierId).orElseThrow(() -> new RuntimeException("Курьер с данным id не найден."));
        changeCourier(order, courierId);
    }

    private void changeCourier(Order order, Long courierid) {
        Courier newCourier = courierRepository.findFirstByStatusAndIdNot(CourierStatus.ONLINE, courierid)
                .orElseThrow(() -> new RuntimeException("В данный момент все курьеры заняты"));
        order.setCourier(newCourier);
        order.setAssigmentAt(LocalDateTime.now());
        orderRepository.save(order);
        newCourier.setStatus(CourierStatus.BUSY);
    }

    @Transactional
    public void acceptOrderByCourierId(Long orderId, Long courierId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatus.ACCEPTED);
        orderRepository.save(order);
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void reassignStuckOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(2);

        List<Order> orders = orderRepository
                .findTop10ByStatusAndAssigmentAtBefore(OrderStatus.PENDING, deadline);

        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.PENDING) {
                continue;
            }


            Courier oldCourier = order.getCourier();
            if (oldCourier == null) {
                continue;
            }

            Optional<Courier> newCourierOpt =
                    courierRepository.findFirstByStatusAndIdNot(CourierStatus.ONLINE, oldCourier.getId());

            oldCourier.setStatus(CourierStatus.ONLINE);
            if (newCourierOpt.isEmpty()) {
                order.setCourier(null);
                order.setStatus(OrderStatus.NEW);
                order.setAssigmentAt(null);
                continue;
            }

            Courier newCourier = newCourierOpt.get();
            newCourier.setStatus(CourierStatus.BUSY);

            order.setCourier(newCourier);
            order.setStatus(OrderStatus.PENDING);
            order.setAssigmentAt(LocalDateTime.now());


        }
    }
}
