package org.example.blps.service.consumers;

import jakarta.persistence.EntityNotFoundException;
import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.entity.OrderAttempt;
import org.example.blps.enums.CourierStatus;
import org.example.blps.enums.OrderAttemptStatus;
import org.example.blps.enums.OrderStatus;
import org.example.blps.repository.OrderRepository;
import org.example.blps.service.consumers.Repository.OrderConsumerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderAssigmentService {
    private final Integer LIMIT = 3;
    private final OrderAttemptConsumerService orderAttemptConsumerService;
    private final OrderConsumerRepository orderConsumerRepository;
    private final CourierConsumerService courierConsumerService;

    public OrderAssigmentService(OrderConsumerRepository orderConsumerRepository,
                                 CourierConsumerService courierConsumer,OrderAttemptConsumerService orderAttemptConsumerService){
        this.orderConsumerRepository = orderConsumerRepository;
        this.courierConsumerService= courierConsumer;
        this.orderAttemptConsumerService = orderAttemptConsumerService;
    }
    @Transactional
    public void refreshWaitingOrder(Long id){
        Order order = orderConsumerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказа с данным id не существует"));
        if (order.getWaitingCycles() + orderAttemptConsumerService.countAttemptsForOrder(order) >= LIMIT) {
            order.setStatus(OrderStatus.FAILED);
            return;
        }
        Courier courier = courierConsumerService.findOnlineCourier(orderAttemptConsumerService.findCouriersIdByOrder(order));
        if (courier == null){
            order.setWaitingCycles(order.getWaitingCycles()+1);
            if (order.getWaitingCycles() + orderAttemptConsumerService.countAttemptsForOrder(order) >= LIMIT) order.setStatus(OrderStatus.FAILED);
            else order.setStatus(OrderStatus.WAITING);
            return;
        }
        order.setCourier(courier);
        order.setStatus(OrderStatus.PENDING);
        courier.setStatus(CourierStatus.ACCEPTING_ORDER);
        orderAttemptConsumerService.addOrderAttempt(courier, order, OrderAttemptStatus.ASSIGNED);
    }

    @Transactional
    public void refreshAssignedOrder(Long id){
        OrderAttempt orderAttempt = orderAttemptConsumerService.findById(id);
        Order order = orderAttempt.getOrder();
        if (order.getStatus() == OrderStatus.PENDING){
            Courier oldCourier = orderAttempt.getCourier();
            if (oldCourier != null)
                changeCourier(order, oldCourier, OrderAttemptStatus.EXPIRED);
        }
    }
    public void changeCourier(Order order, Courier courier, OrderAttemptStatus status) {
        order.setCourier(null);
        if (courier.getStatus()!=CourierStatus.END_SHIFT) courier.setStatus(CourierStatus.ON_SHIFT);
        else courier.setStatus(CourierStatus.OFF_SHIFT);
        orderAttemptConsumerService.changeAttemptStatus(courier, order,status);
        if (order.getWaitingCycles()+orderAttemptConsumerService.countAttemptsForOrder(order)>=LIMIT){
            order.setStatus(OrderStatus.FAILED);
            return;
        }
        Courier newCourier = courierConsumerService.findOnlineCourier(orderAttemptConsumerService.findCouriersIdByOrder(order));
        if (newCourier == null) {
            order.setStatus(OrderStatus.WAITING);
            return;
        }
        orderAttemptConsumerService.addOrderAttempt(newCourier, order, OrderAttemptStatus.ASSIGNED);
        order.setCourier(newCourier);
        order.setStatus(OrderStatus.PENDING);
        newCourier.setStatus(CourierStatus.ACCEPTING_ORDER);
    }
    public List<Long> getTop10WaitingOrders(){
        return orderConsumerRepository.findTop10ByStatus(OrderStatus.WAITING)
                .stream().map((Order order)-> order.getId()).toList();
    }
}
