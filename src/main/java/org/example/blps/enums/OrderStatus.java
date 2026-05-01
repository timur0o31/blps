package org.example.blps.enums;

public enum OrderStatus {
    NEW,
    WAITING,
    PENDING,
    ACCEPTED,
    ON_THE_WAY,
    PICKED_UP,
    DELIVERED,
    FAILED;
    public boolean canSwitchTo(OrderStatus status){
        switch (this){
            case ACCEPTED:
                return status == ON_THE_WAY;
            case ON_THE_WAY:
                return status == PICKED_UP;
            case PICKED_UP:
                return status == DELIVERED;
            default:
                return false;
        }
    }
}
