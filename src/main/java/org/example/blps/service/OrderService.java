package org.example.blps.service;
import org.springframework.transaction.annotation.Transactional;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.requestDto.OrderStatusRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.entity.*;
import org.example.blps.enums.OrderAttemptStatus;
import org.example.blps.mapper.OrderMapper;
import org.example.blps.repository.OrderRepository;
import org.example.blps.enums.CourierStatus;
import org.example.blps.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserService userService;
    private final ClientService clientService;
    private final OrderAttemptService orderAttemptService;
    private final CourierService courierService;
    private final OrderDispatchService orderDispatchService;
    private final Integer LIMIT = 3;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper,
                        UserService userService, ClientService clientService,
                        OrderAttemptService orderAttemptService,
                        CourierService courierService,
                        OrderDispatchService orderDispatchService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.courierService = courierService;
        this.userService = userService;
        this.clientService = clientService;
        this.orderAttemptService = orderAttemptService;
        this.orderDispatchService = orderDispatchService;
    }

    // Получить активные заказы (для курьера)
    public OrderResponseDto getOrder(String email){
        Courier courier = courierService.findCourierByEmail(email);
        return orderMapper.fromEntityToDto(orderRepository.findByCourierAndStatus(courier, OrderStatus.PENDING));
    }

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
        orderRepository.save(newOrder);
        orderAttemptService.addOrderAttempt(courier,newOrder, OrderAttemptStatus.ASSIGNED);
        courier.setStatus(CourierStatus.ACCEPTING_ORDER);
        return orderMapper.fromEntityToDto(newOrder);
    }


    // Обновить заказ (для курьера)
    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderStatusRequestDto orderRequestDto, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказа с данным id не существует"));
        Courier courier = courierService.findCourierByEmail(email);
        if (order.getCourier() == null){
            if (order.getStatus()==OrderStatus.FAILED) {
                throw new RuntimeException("Такой заказ уже был завершен!");
            }else if (order.getStatus() == OrderStatus.WAITING){
                throw new RuntimeException("Данный заказ не назначен вам!");
            }
        }
        if (!order.getCourier().getId().equals(courier.getId())){
            throw new RuntimeException("Другой курьер не может менять статус заказа");
        }
        OrderStatus prevOrderStatus = order.getStatus();
        if (!prevOrderStatus.canSwitchTo(orderRequestDto.getOrderStatus())){
            throw new RuntimeException("Из состояния "+prevOrderStatus +" нельзя перейти в "+ orderRequestDto.getOrderStatus());
        }
        order.setStatus(orderRequestDto.getOrderStatus());
        if (orderRequestDto.getOrderStatus()==OrderStatus.DELIVERED){
            if (courier.getStatus()==CourierStatus.END_SHIFT){
               courier.setStatus(CourierStatus.OFF_SHIFT);
            }else {
                courier.setStatus(CourierStatus.ON_SHIFT);
                courierService.saveCourier(courier);
            }
        }
        return orderMapper.fromEntityToDto(orderRepository.save(order));
    }

    public List<OrderResponseDto> getOrderHistory(String email,long page,long size) {
        List<OrderResponseDto> orderHistory = new ArrayList<>();
        User user = userService.findByEmail(email);
        Client client = clientService.findByUser(user);
        List<Order> orders = orderRepository.findOrdersByClientId(client.getId(), size, page*size);
        for (Order order:orders) {
            orderHistory.add(orderMapper.fromEntityToDto(order));
        }
        return orderHistory;
    }

    public OrderResponseDto getStatusOrder(Long id, String email){
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказа с данным id не существует"));
        Client client = clientService.findByUser(userService.findByEmail(email));
        if (!order.getClient().getId().equals(client.getId())){
            throw new RuntimeException("Клиент не может просматривать стутус чужого заказа");
        }
        return orderMapper.fromEntityToDto(order);
    }


    @Transactional
    public void cancelOrderByCourierId(Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Заказ не найден"));
        Courier courier = courierService.findCourierByEmail(email);
        if (order.getCourier() == null){
            if (order.getStatus()==OrderStatus.FAILED) {
                throw new RuntimeException("Такой заказ уже был завершен!");
            }else if (order.getStatus() == OrderStatus.WAITING){
                throw new RuntimeException("Данный заказ не назначен вам!");
            }
        }
        if (!order.getCourier().getId().equals(courier.getId())){
            throw new RuntimeException("Курьер не может отменять чужие заказы");
        }
        orderDispatchService.changeCourier(order, courier, OrderAttemptStatus.REJECTED);
    }

    @Transactional
    public void acceptOrderByCourierId(Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Заказ не найден"));
        Courier courier = courierService.findCourierByEmail(email);
        if (order.getCourier() == null){
            if (order.getStatus()==OrderStatus.FAILED) {
                throw new RuntimeException("Такой заказ уже был завершен!");
            }else if (order.getStatus() == OrderStatus.WAITING){
                throw new RuntimeException("Данный заказ не назначен вам!");
            }
        }
        if (!order.getCourier().getId().equals(courier.getId())){
            throw new RuntimeException("Курьер не может принимать чужие заказы!");
        }
        if (order.getStatus()!=OrderStatus.PENDING){
            throw new RuntimeException("Только из состояния PENDING можно принять заказ!");
        }
        order.setStatus(OrderStatus.ACCEPTED);
        if (courier.getStatus()!=CourierStatus.END_SHIFT) courier.setStatus(CourierStatus.BUSY);
        else courier.setStatus(CourierStatus.END_SHIFT);
        orderAttemptService.changeAttemptStatus(courier, order, OrderAttemptStatus.ACCEPTED);
        orderRepository.save(order);
    }
}
