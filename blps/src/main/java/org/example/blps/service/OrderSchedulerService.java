package org.example.blps.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderSchedulerService {
    private final OrderService orderService;
    private final OrderAttemptService orderAttemptService;

    public OrderSchedulerService(OrderService orderService, OrderAttemptService orderAttemptService){
        this.orderService = orderService;
        this.orderAttemptService = orderAttemptService;
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
            orderService.refreshAssignedOrder(id);
        }
    }
    public void refreshWaitingOrders(){
        List<Long> waitingOrdersId = orderService.getTop10WaitingOrders();
        for (Long id:waitingOrdersId){
            orderService.refreshWaitingOrder(id);
        }
    }
}
