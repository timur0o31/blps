package org.example.blps.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OrderSchedulerService {
    private final OrderService orderService;

    public OrderSchedulerService(OrderService orderService){
        this.orderService = orderService;
    }
    @Scheduled(fixedDelay = 30000)
    public void processOrders(){
        orderService.processOrders();
    }
}
