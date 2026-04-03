package org.example.blps.dto.responseDto;

import lombok.Getter;
import org.example.blps.enums.OrderStatus;

@Getter
public class OrderResponseStatus {

    private OrderStatus orderStatus;

    public OrderResponseStatus(OrderStatus orderStatus){
        this.orderStatus = orderStatus;
    }

}
