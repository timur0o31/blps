package org.example.blps.service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.requestDto.OrderStatusRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.entity.Client;
import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.mapper.OrderMapper;
import org.example.blps.repository.CourierRepository;
import org.example.blps.repository.OrderRepository;
import org.example.blps.enums.CourierStatus;
import org.example.blps.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CourierRepository courierRepository;
    private final UserService userService;
    private final ClientService clientService;
    private final Integer LIMIT = 3;
    @Autowired
    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, CourierRepository courierRepository,
                        UserService userService, ClientService clientService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.courierRepository = courierRepository;
        this.userService = userService;
        this.clientService = clientService;
    }
    public OrderResponseDto getOrder(String email){
        Courier courier = courierRepository.findByUserId(userService.findByEmail(email).getId())
                .orElseThrow(()->new RuntimeException("Курьера с данным email не существует"));
        return orderMapper.fromEntityToDto(orderRepository.findByCourierAndStatus(courier, OrderStatus.PENDING));
    }
    @Transactional
    public OrderResponseDto addOrder(String email, OrderRequestDto orderRequestDto) {
        Order newOrder = orderMapper.fromDtoToEntity(orderRequestDto);
        Client client = clientService.findByUser(userService.findByEmail(email));
        newOrder.setClient(client);
        Courier courier = courierRepository.findFirstByStatus(CourierStatus.ONLINE)
                .orElse(null);
        if  (courier == null) {
            newOrder.setStatus(OrderStatus.WAITING);
            return orderMapper.fromEntityToDto(orderRepository.save(newOrder));
        }
        newOrder.setCourier(courier);
        newOrder.setStatus(OrderStatus.PENDING);
        newOrder.setAssigmentAt(LocalDateTime.now());
        courier.setStatus(CourierStatus.ACCEPTING_ORDER);
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
    public void cancelOrderByCourierId(Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Заказ не найден"));
        Courier courier = courierRepository.findByUserId(userService.findByEmail(email).getId())
                .orElseThrow(()->new RuntimeException("Курьера с данным email не существует"));
        changeCourier(order, courier);
    }

    private void changeCourier(Order order, Courier courier) {
        order.setAttempts(order.getAttempts() + 1);
        if (order.getAttempts()>LIMIT){
            order.setStatus(OrderStatus.FAILED);
            order.setCourier(null);
            courier.setStatus(CourierStatus.ONLINE);
            return;
        }
        Courier newCourier = courierRepository.findFirstByStatusAndIdNot(CourierStatus.ONLINE, courier.getId())
                .orElse(null);
        if (newCourier == null) {
            order.setStatus(OrderStatus.WAITING);
            order.setCourier(null);
            courier.setStatus(CourierStatus.ONLINE);
            return;
        }
        courier.setStatus(CourierStatus.ONLINE);
        order.setCourier(newCourier);
        order.setAssigmentAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        newCourier.setStatus(CourierStatus.ACCEPTING_ORDER);
    }

    @Transactional
    public void acceptOrderByCourierId(Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatus.ACCEPTED);
        Courier courier = courierRepository.findByUserId(userService.findByEmail(email).getId())
                .orElseThrow(()->new RuntimeException("Курьера с данным email не существует"));
        courier.setStatus(CourierStatus.BUSY);
        orderRepository.save(order);
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processOrders() {
        List<Order> waitingOrders = orderRepository.findTop10ByStatus(OrderStatus.WAITING);
        for (Order order : waitingOrders) {
            if (order.getAttempts() > LIMIT) {
                order.setStatus(OrderStatus.FAILED);
                continue;
            }
            Courier courier = courierRepository
                    .findFirstByStatus(CourierStatus.ONLINE)
                    .orElse(null);
            if (courier == null) continue;
            order.setCourier(courier);
            order.setStatus(OrderStatus.PENDING);
            order.setAssigmentAt(LocalDateTime.now());
            courier.setStatus(CourierStatus.ACCEPTING_ORDER);
        }
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(2);
        List<Order> queueOrders = orderRepository.findTop10ByStatusAndAssigmentAtBefore(OrderStatus.PENDING, deadline);
        for (Order order : queueOrders) {
            if (order.getStatus() != OrderStatus.PENDING){
                continue;
            }
            Courier oldCourier = order.getCourier();
            if (oldCourier == null) continue;
            changeCourier(order, oldCourier);
        }
    }
}
