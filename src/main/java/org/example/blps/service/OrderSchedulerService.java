package org.example.blps.service;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("scheduler")
public class OrderSchedulerService {
    private final OrderService orderService;
    private final OrderAttemptService orderAttemptService;
    private final OrderAssignmentPublisherService orderAssignmentPublisherService;

    public OrderSchedulerService(OrderService orderService, OrderAttemptService orderAttemptService,
                                  OrderAssignmentPublisherService orderAssignmentPublisherService){
        this.orderService = orderService;
        this.orderAttemptService = orderAttemptService;
        this.orderAssignmentPublisherService = orderAssignmentPublisherService;
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
        List<Long> waitingOrdersId = orderService.getTop10WaitingOrders();
        for (Long id:waitingOrdersId){
            orderAssignmentPublisherService.publishAssignOrder(id);
        }
    }
}
