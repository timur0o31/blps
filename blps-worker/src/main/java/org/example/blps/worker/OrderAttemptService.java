package org.example.blps.worker;

import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.entity.OrderAttempt;
import org.example.blps.enums.OrderAttemptStatus;
import org.example.blps.repository.OrderAttemptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderAttemptService {
    private final OrderAttemptRepository orderAttemptRepository;
    public OrderAttemptService(OrderAttemptRepository orderAttemptRepository) {
        this.orderAttemptRepository = orderAttemptRepository;
    }
    //Этот метод дублируется
    public Integer countAttemptsForOrder(Order order){
        return orderAttemptRepository.countOrderAttemptByOrderAndStatusIn(order, List.of(OrderAttemptStatus.REJECTED, OrderAttemptStatus.EXPIRED));
    }
    public OrderAttempt findById(Long id){
        return orderAttemptRepository.findById(id).orElseThrow(()-> new IllegalStateException("не найдено попытки с таким id"));
    }
    public List<Long> findCouriersIdByOrder(Order order){
        return orderAttemptRepository.findByOrder(order).stream()
                .map(attempt -> attempt.getCourier().getId())
                .toList();
    }
    public void addOrderAttempt(Courier courier, Order order, OrderAttemptStatus status){
        OrderAttempt orderAttempt = new OrderAttempt();
        orderAttempt.setCourier(courier);
        orderAttempt.setOrder(order);
        orderAttempt.setAssigmentAt(LocalDateTime.now());
        orderAttempt.setStatus(status);
        orderAttemptRepository.save(orderAttempt);
    }
    //Этот метод дублируется
    public void changeAttemptStatus(Courier courier, Order order, OrderAttemptStatus status){
        OrderAttempt orderAttempt = orderAttemptRepository.findByCourierAndOrderAndStatus(courier, order,OrderAttemptStatus.ASSIGNED)
                .orElseThrow(()->new IllegalStateException("Не найдено активной попытки"));
        orderAttempt.setStatus(status);
    }
}
