package org.example.blps.enums;
import lombok.Getter;
import java.util.Set;

import static org.example.blps.enums.Privilege.*;

@Getter
public enum Role {

    ADMIN(Set.of(VIEW_COURIER_APPLICATIONS,
            VIEW_COURIERS,
            BLOCK_COURIER,
            CREATE_ADMIN,
            CHANGE_STATE,
            APPROVE_REQUEST,
            DECLINE_REQUEST)),

    CLIENT(Set.of(CREATE_ORDER,
            VIEW_STATUS_ORDER,
            VIEW_ORDER_HISTORY)),


    COURIER(Set.of(TOGGLE_SHIFT_STATUS,
            UPDATE_STATUS_ORDER,
            CANCEL_ORDER,
            ACCEPT_ORDER,
            VIEW_ORDER,
            SUBMIT_REQUEST));

    private final Set<Privilege> privileges;

    Role(Set<Privilege> privileges) {
        this.privileges = privileges;
    }

}
