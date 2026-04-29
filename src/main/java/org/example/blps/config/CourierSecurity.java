package org.example.blps.config;
import lombok.RequiredArgsConstructor;
import org.example.blps.entity.User;
import org.example.blps.enums.CourierAccountState;
import org.example.blps.enums.Role;
import org.example.blps.repository.CourierRepository;
import org.example.blps.repository.UserRepository;
import org.example.blps.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("courierSecurity")
@RequiredArgsConstructor
public class CourierSecurity {

    private final CourierRepository courierRepository;
    private final UserRepository userRepository;

    public boolean isApprovedCourier(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return false;
        }

        if (userDetails.getRole() != Role.COURIER) {
            return false;
        }

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return courierRepository.findByUserId(user.getId())
                .map(courier -> courier.getAccountState() == CourierAccountState.ACTIVE)
                .orElse(false);
    }
}