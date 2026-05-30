package org.example.blps.service;

import org.example.blps.entity.Order;
import org.example.blps.enums.OrderStatus;
import org.example.blps.repository.OrderRepository;
import org.example.blps.service.producer.OrderAssignmentPublisherService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderSchedulerService {
    private final OrderAttemptService orderAttemptService;
    private final OrderAssignmentPublisherService orderAssignmentPublisherService;
    private final OrderRepository orderRepository;
    public OrderSchedulerService(OrderAttemptService orderAttemptService, OrderAssignmentPublisherService orderAssignmentPublisherService,
                                 OrderRepository orderRepository){
        this.orderAttemptService = orderAttemptService;
        this.orderAssignmentPublisherService = orderAssignmentPublisherService;
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
            orderAssignmentPublisherService.publishExpireAssignment(id);
        }
    }
    public void refreshWaitingOrders(){
        List<Long> waitingOrdersId = orderRepository.findTop10ByStatus(OrderStatus.WAITING)
                .stream().map((Order order)-> order.getId()).toList();
        for (Long id:waitingOrdersId){
            orderAssignmentPublisherService.publishAssignOrder(id);
        }
    }
}
