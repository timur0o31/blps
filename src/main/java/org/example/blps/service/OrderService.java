package org.example.blps.service;
import jakarta.transaction.Transactional;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.requestDto.OrderStatusRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.dto.responseDto.OrderResponseStatus;
import org.example.blps.entity.Client;
import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.entity.OrderAttempt;
import org.example.blps.enums.OrderAttemptStatus;
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
    private final UserService userService;
    private final ClientService clientService;
    private final OrderAttemptService orderAttemptService;
    private final CourierService courierService;
    private final Integer LIMIT = 3;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper,
                        UserService userService, ClientService clientService,
                        OrderAttemptService orderAttemptService,
                        CourierService courierService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.courierService = courierService;
        this.userService = userService;
        this.clientService = clientService;
        this.orderAttemptService = orderAttemptService;
    }

    // Получить заказ (для клиента)
    public OrderResponseDto getOrder(String email){
        Courier courier = courierService.findCourierByEmail(email);
        return orderMapper.fromEntityToDto(orderRepository.findByCourierAndStatus(courier, OrderStatus.PENDING));
    }

    // Заказать в доставке (для клиента)
    @Transactional
    public OrderResponseDto addOrder(String email, OrderRequestDto orderRequestDto) {
        Order newOrder = orderMapper.fromDtoToEntity(orderRequestDto);
        Client client = clientService.findByUser(userService.findByEmail(email));
        newOrder.setClient(client);
        Courier courier = courierService.findCourierWithOnlineStatus();
        if  (courier == null) {
            newOrder.setStatus(OrderStatus.WAITING);
            return orderMapper.fromEntityToDto(orderRepository.save(newOrder));
        }
        newOrder.setCourier(courier);
        newOrder.setStatus(OrderStatus.PENDING);
        orderAttemptService.addOrderAttempt(courier,newOrder, OrderAttemptStatus.ASSIGNED);
        courier.setStatus(CourierStatus.ACCEPTING_ORDER);
        return orderMapper.fromEntityToDto(orderRepository.save(newOrder));
    }


    // Обновить заказ (для клиента)
    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderStatusRequestDto orderRequestDto, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказа с данным id не существует"));
        Courier courier = courierService.findCourierByEmail(email);
        if (!order.getCourier().getId().equals(courier.getId())){
            throw new RuntimeException("Другой курьер не может менять статус заказа");
        }
        order.setStatus(orderRequestDto.getOrderStatus());
        if (orderRequestDto.getOrderStatus()==OrderStatus.DELIVERED){
            courier.setStatus(CourierStatus.ONLINE);
            courierService.saveCourier(courier);
        }
        return orderMapper.fromEntityToDto(orderRepository.save(order));
    }

    // Получить статус заказа для клиента
    public OrderResponseStatus getStatusOrder(Long id, String email){
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказа с данным id не существует"));
        Client client = clientService.findByUser(userService.findByEmail(email));
        if (!order.getClient().getId().equals(client.getId())){
            throw new RuntimeException("Клиент не может просматривать стутус чужого заказа");
        }
        return new OrderResponseStatus(order.getStatus());
    }
    @Transactional
    public void cancelOrderByCourierId(Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Заказ не найден"));
        Courier courier = courierService.findCourierByEmail(email);
        changeCourier(order, courier, OrderAttemptStatus.REJECTED);
    }

    private void changeCourier(Order order, Courier courier, OrderAttemptStatus status) {
        orderAttemptService.changeAttemptStatus(courier, order,status);
        if (order.getAttempts()+orderAttemptService.countAttemptsForOrder(order)>LIMIT){
            order.setStatus(OrderStatus.FAILED);
            order.setCourier(null);
            courier.setStatus(CourierStatus.ONLINE);
            return;
        }
        Courier newCourier = courierService.findOnlineCourier(orderAttemptService.findCouriersIdByOrder(order));
        if (newCourier == null) {
            order.setStatus(OrderStatus.WAITING);
            order.setCourier(null);
            courier.setStatus(CourierStatus.ONLINE);
            return;
        }
        courier.setStatus(CourierStatus.ONLINE);
        orderAttemptService.addOrderAttempt(newCourier, order, OrderAttemptStatus.ASSIGNED);
        order.setCourier(newCourier);
        order.setStatus(OrderStatus.PENDING);
        newCourier.setStatus(CourierStatus.ACCEPTING_ORDER);
    }

    @Transactional
    public void acceptOrderByCourierId(Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatus.ACCEPTED);
        Courier courier = courierService.findCourierByEmail(email);
        courier.setStatus(CourierStatus.BUSY);
        orderAttemptService.addOrderAttempt(courier, order, OrderAttemptStatus.ACCEPTED);
        orderRepository.save(order);
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processOrders() {
        List<Order> waitingOrders = orderRepository.findTop10ByStatus(OrderStatus.WAITING);
        for (Order order : waitingOrders) {
            if (order.getAttempts() + orderAttemptService.countAttemptsForOrder(order) > LIMIT) {
                order.setStatus(OrderStatus.FAILED);
                continue;
            }
            Courier courier = courierService.findOnlineCourier(
                    orderAttemptService.findCouriersIdByOrder(order)
            );
            if (courier == null){
                order.setAttempts(order.getAttempts()+1);
                if (order.getAttempts() + orderAttemptService.countAttemptsForOrder(order) > LIMIT) {
                    order.setStatus(OrderStatus.FAILED);
                } else {
                    order.setStatus(OrderStatus.WAITING);
                }
                return;
            }
            order.setCourier(courier);
            order.setStatus(OrderStatus.PENDING);
            courier.setStatus(CourierStatus.ACCEPTING_ORDER);
            orderAttemptService.addOrderAttempt(courier, order, OrderAttemptStatus.ASSIGNED);
        }
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(2);
        List<OrderAttempt> attempts = orderAttemptService.findAssignedAttempts(deadline);

        for (OrderAttempt attempt: attempts) {
            Order order = attempt.getOrder();
            if (order == null) continue;
            if (order.getStatus() == OrderStatus.PENDING){
                Courier oldCourier = attempt.getCourier();
                if (oldCourier != null) {
                    changeCourier(order, oldCourier, OrderAttemptStatus.EXPIRED);
                }
            }
        }
    }
}
