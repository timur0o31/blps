package org.example.blps.service;

import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.entity.OrderAttempt;
import org.example.blps.enums.OrderAttemptStatus;
import org.example.blps.repository.OrderAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderAttemptService {
    private final OrderAttemptRepository orderAttemptRepository;
    @Autowired
    public OrderAttemptService(OrderAttemptRepository orderAttemptRepository){
        this.orderAttemptRepository = orderAttemptRepository;
    }
    public void addOrderAttempt(Courier courier, Order order, OrderAttemptStatus status){
        OrderAttempt orderAttempt = new OrderAttempt();
        orderAttempt.setCourier(courier);
        orderAttempt.setOrder(order);
        orderAttempt.setAssigmentAt(LocalDateTime.now());
        orderAttempt.setStatus(status);
        orderAttemptRepository.save(orderAttempt);
    }
    public Integer countAttemptsForOrder(Order order){
        return orderAttemptRepository.countOrderAttemptByOrderAndStatusIn(order, List.of(OrderAttemptStatus.REJECTED, OrderAttemptStatus.EXPIRED));
    }
    public void changeAttemptStatus(Courier courier, Order order, OrderAttemptStatus status){
        OrderAttempt orderAttempt = orderAttemptRepository.findByCourierAndOrder(courier, order);
        orderAttempt.setStatus(status);
    }
    public List<Long> findCouriersIdByOrder(Order order){
        return orderAttemptRepository.findCourierIdsByOrOrder(order);
    }
    public List<OrderAttempt> findAssignedAttempts(LocalDateTime deadline) {
        return orderAttemptRepository.findTop10ByStatusAndAssigmentAtBefore(
                OrderAttemptStatus.ASSIGNED,
                deadline
        );
    }
}
