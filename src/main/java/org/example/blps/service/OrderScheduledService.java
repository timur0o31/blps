package org.example.blps.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OrderScheduledService {
    private final OrderDispatchService orderDispatchService;
    @Autowired
    public OrderScheduledService(OrderDispatchService orderDispatchService){
        this.orderDispatchService=orderDispatchService;
    }
    @Scheduled(fixedDelay = 30000)
    public void scheduledTask(){
        orderDispatchService.processOrders();
    }
}
