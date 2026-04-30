package org.example.blps.enums;

public enum Privilege {
    // Общие
    CLIENT_REGISTRATION,
    COURIER_REGISTRATION,
    SIGN_IN,

    // Админ
    APPROVE_COURIER,
    VIEW_COURIER_APLICATIONS,
    VIEW_COURIERS,
    DELETE_COURIER,

    // Клиент
    CREATE_ORDER,
    VIEW_STATUS_ORDER,
    VIEW_ORDER_HISTORY,

    // Курьер
    TOGGLE_SHIFT_STATUS,
    UPDATE_STATUS_ORDER,
    DECLINE_ORDER,
    ACCEPT_ORDER,
    VIEW_ORDER,

}
