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
    public int statusNumber() {
        return switch (this) {
            case NEW -> 0;
            case WAITING -> 1;
            case PENDING -> 2;
            case ACCEPTED -> 3;
            case ON_THE_WAY -> 4;
            case PICKED_UP -> 5;
            case DELIVERED -> 6;
            case FAILED -> 7;
        };
    }

    public boolean isMovedBackTo(OrderStatus newStatus) {
        return newStatus.statusNumber() < this.statusNumber();
    }
}
