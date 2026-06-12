package org.example.blps.service.schedule;

import org.example.blps.entity.Order;
import org.example.blps.enums.OrderStatus;
import org.example.blps.repository.OrderRepository;
import org.example.blps.service.OrderAttemptService;
import org.example.blps.service.producer.OrderAssignmentProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduler {
    private final OrderAttemptService orderAttemptService;
    private final OrderAssignmentProducer orderAssignmentProducer;
    private final OrderRepository orderRepository;
    public OrderScheduler(OrderAttemptService orderAttemptService, OrderAssignmentProducer orderAssignmentProducer,
                          OrderRepository orderRepository){
        this.orderAttemptService = orderAttemptService;
        this.orderAssignmentProducer = orderAssignmentProducer;
        this.orderRepository = orderRepository;
    }
    @Scheduled(fixedDelay = 30000)
    public void processOrders(){
        refreshWaitingOrders();
        refreshAssignedOrders();
    }
    public void refreshAssignedOrders(){
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(2);
        List<Long> attemptsId = orderAttemptService.findAssignedAttempts(deadline);
        for (Long id: attemptsId) {
            orderAssignmentProducer.publishExpireAssignment(id);
        }
    }
    public void refreshWaitingOrders(){
        List<Long> waitingOrdersId = orderRepository.findTop10ByStatus(OrderStatus.WAITING)
                .stream().map((Order order)-> order.getId()).toList();
        for (Long id:waitingOrdersId){
            orderAssignmentProducer.publishAssignOrder(id);
        }
    }
}
