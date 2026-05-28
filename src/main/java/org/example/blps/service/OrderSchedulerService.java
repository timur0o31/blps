package org.example.blps.service;

import org.example.blps.service.consumers.OrderAssigmentService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("scheduler")
public class OrderSchedulerService {
    private final OrderAttemptService orderAttemptService;
    private final OrderAssignmentPublisherService orderAssignmentPublisherService;
    private final OrderAssigmentService orderAssigmentService;

    public OrderSchedulerService(OrderAttemptService orderAttemptService,
                                 OrderAssignmentPublisherService orderAssignmentPublisherService, OrderAssigmentService orderAssigmentService){
        this.orderAttemptService = orderAttemptService;
        this.orderAssignmentPublisherService = orderAssignmentPublisherService;
        this.orderAssigmentService = orderAssigmentService;
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
        List<Long> waitingOrdersId = orderAssigmentService.getTop10WaitingOrders();
        for (Long id:waitingOrdersId){
            orderAssignmentPublisherService.publishAssignOrder(id);
        }
    }
}
