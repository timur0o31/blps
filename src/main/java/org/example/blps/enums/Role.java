package org.example.blps.enums;
import lombok.Getter;
import java.util.Set;

import static org.example.blps.enums.Privilege.*;

@Getter
public enum Role {

    ADMIN(Set.of(APPROVE_COURIER,
            VIEW_COURIER_APLICATIONS,
            VIEW_COURIERS,
            DELETE_COURIER)),

    CLIENT(Set.of(CREATE_ORDER,
            VIEW_STATUS_ORDER,
            VIEW_ORDER_HISTORY)),


    COURIER(Set.of(TOGGLE_SHIFT_STATUS,
            UPDATE_STATUS_ORDER,
            DECLINE_ORDER,
            ACCEPT_ORDER,
            VIEW_ORDER));

    private final Set<Privilege> privileges;

    Role(Set<Privilege> privileges) {
        this.privileges = privileges;
    }

}
