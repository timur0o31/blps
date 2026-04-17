package org.example.blps.service;

import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.entity.OrderAttempt;
import org.example.blps.enums.CourierStatus;
import org.example.blps.enums.OrderAttemptStatus;
import org.example.blps.enums.OrderStatus;
import org.example.blps.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderDispatchService {
    private final OrderAttemptService orderAttemptService;
    private final CourierService courierService;
    private final OrderRepository orderRepository;

    public OrderDispatchService(OrderAttemptService orderAttemptService, CourierService courierService,
                                OrderRepository orderRepository){
        this.courierService=courierService;
        this.orderAttemptService=orderAttemptService;
        this.orderRepository = orderRepository;
    }
    public void changeCourier(Order order, Courier courier, OrderAttemptStatus status) {
        order.setCourier(null);
        if (courier.getStatus()!= CourierStatus.END_SHIFT) {
            courier.setStatus(CourierStatus.ON_SHIFT);
        }else{
            courier.setStatus(CourierStatus.OFF_SHIFT);
        }
        orderAttemptService.changeAttemptStatus(courier, order,status);
        if (order.getWaitingCycles()+orderAttemptService.countAttemptsForOrder(order)>=3){
            order.setStatus(OrderStatus.FAILED);
            return;
        }
        Courier newCourier = courierService.findOnlineCourier(orderAttemptService.findCouriersIdByOrder(order));
        if (newCourier == null) {
            order.setStatus(OrderStatus.WAITING);
            return;
        }
        orderAttemptService.addOrderAttempt(newCourier, order, OrderAttemptStatus.ASSIGNED);
        order.setCourier(newCourier);
        order.setStatus(OrderStatus.PENDING);
        newCourier.setStatus(CourierStatus.ACCEPTING_ORDER);
    }
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processOrders() {
        List<Order> waitingOrders = orderRepository.findTop10ByStatus(OrderStatus.WAITING);
        for (Order order : waitingOrders) {
            if (order.getWaitingCycles() + orderAttemptService.countAttemptsForOrder(order) >= 3) {
                order.setStatus(OrderStatus.FAILED);
                continue;
            }
            Courier courier = courierService.findOnlineCourier(
                    orderAttemptService.findCouriersIdByOrder(order)
            );
            if (courier == null){
                order.setWaitingCycles(order.getWaitingCycles()+1);
                if (order.getWaitingCycles() + orderAttemptService.countAttemptsForOrder(order) >= 3) {
                    order.setStatus(OrderStatus.FAILED);
                } else {
                    order.setStatus(OrderStatus.WAITING);
                }
                continue;
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
            if (order.getStatus() == OrderStatus.PENDING){
                Courier oldCourier = attempt.getCourier();
                if (oldCourier != null) {
                    changeCourier(order, oldCourier, OrderAttemptStatus.EXPIRED);
                }
            }
        }
    }

}
