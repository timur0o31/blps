package org.example.blps.dto.responseDto;

import org.example.blps.enums.OrderStatus;

public class OrderResponseStatus {

    private OrderStatus orderStatus;

    public OrderResponseStatus(OrderStatus orderStatus){
        this.orderStatus = orderStatus;
    }
    public OrderStatus getOrderStatus(){
        return orderStatus;
    }
}
