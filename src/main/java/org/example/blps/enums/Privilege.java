package org.example.blps.enums;

public enum Privilege {

    // Админ
    VIEW_COURIER_APPLICATIONS, //d
    VIEW_COURIERS, //d
    BLOCK_COURIER, //d
    CREATE_ADMIN,  //d
    CHANGE_STATE, //d
    APPROVE_REQUEST, //d
    DECLINE_REQUEST, //d
    VIEW_ADMINS,

    // Клиент
    CREATE_ORDER, //d
    VIEW_STATUS_ORDER, //d
    VIEW_ORDER_HISTORY, //d

    // Курьер
    TOGGLE_SHIFT_STATUS,//d
    UPDATE_STATUS_ORDER, //d
    CANCEL_ORDER, //d
    ACCEPT_ORDER, //d
    VIEW_ORDER, //d
    SUBMIT_REQUEST, //d

}
