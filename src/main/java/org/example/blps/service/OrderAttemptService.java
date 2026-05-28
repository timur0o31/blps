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
    public List<Long> findAssignedAttempts(LocalDateTime deadline) {
        return orderAttemptRepository.findTop10ByStatusAndAssigmentAtBefore(OrderAttemptStatus.ASSIGNED, deadline)
                .stream().map((OrderAttempt orderAttempt) -> orderAttempt.getId()).toList();
    }
    public void changeAttemptStatus(Courier courier, Order order, OrderAttemptStatus status){
        OrderAttempt orderAttempt = orderAttemptRepository.findByCourierAndOrderAndStatus(courier, order,OrderAttemptStatus.ASSIGNED)
                .orElseThrow(()->new IllegalStateException("Не найдено активной попытки"));
        orderAttempt.setStatus(status);
    }
}
